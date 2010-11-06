package sizzle.compiler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

import sizzle.aggregators.AggregatorSpec;
import sizzle.aggregators.IntSumAggregator;
import sizzle.functions.FunctionSpec;
import sizzle.types.SizzleAny;
import sizzle.types.SizzleArray;
import sizzle.types.SizzleBool;
import sizzle.types.SizzleBytes;
import sizzle.types.SizzleFingerprint;
import sizzle.types.SizzleFloat;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleInt;
import sizzle.types.SizzleMap;
import sizzle.types.SizzleScalar;
import sizzle.types.SizzleString;
import sizzle.types.SizzleTable;
import sizzle.types.SizzleTime;
import sizzle.types.SizzleTuple;
import sizzle.types.SizzleType;
import sizzle.types.SizzleVarargs;

public class SymbolTable {
	private static Logger LOG = Logger.getLogger(SymbolTable.class);

	private final boolean strictCompatibility;

	private final ClassLoader loader;

	private FunctionTrie functions;
	private final HashMap<String, Class<?>> aggregators;

	private final Map<Class<?>, SizzleType> protomap;
	private final Map<String, SizzleType> idmap;

	private Map<String, SizzleType> globals;
	private Map<String, SizzleType> locals;

	private SizzleType initializerType;
	private String mapName;
	private String staticInitializer;

	public SymbolTable() throws IOException {
		this(new ArrayList<URL>(), new SizzleString());
	}

	public SymbolTable(final List<URL> libs) throws IOException {
		this(libs, new SizzleString());
	}

	public SymbolTable(final SizzleType input) throws IOException {
		this(new ArrayList<URL>(), input);
	}

	public SymbolTable(final List<URL> libs, final SizzleType input) throws IOException {
		this.strictCompatibility = true;

		this.loader = Thread.currentThread().getContextClassLoader();

		// this maps the Java types in protocol buffers into Sizzle types
		this.protomap = new HashMap<Class<?>, SizzleType>();
		this.protomap.put(int.class, new SizzleInt());
		this.protomap.put(long.class, new SizzleInt());
		this.protomap.put(float.class, new SizzleFloat());
		this.protomap.put(double.class, new SizzleFloat());
		this.protomap.put(boolean.class, new SizzleBool());
		this.protomap.put(byte[].class, new SizzleBytes());
		this.protomap.put(String.class, new SizzleString());

		// this maps scalar Sizzle scalar types names to their classes
		// TODO: do this via reflection
		this.idmap = new HashMap<String, SizzleType>();
		this.idmap.put("any", new SizzleAny());
		this.idmap.put("none", null);
		this.idmap.put("bool", new SizzleBool());
		this.idmap.put("int", new SizzleInt());
		this.idmap.put("float", new SizzleFloat());
		this.idmap.put("time", new SizzleTime());
		this.idmap.put("fingerprint", new SizzleFingerprint());
		this.idmap.put("string", new SizzleString());
		this.idmap.put("bytes", new SizzleBytes());

		// does the same for arrays
		for (final String key : new HashSet<String>(this.idmap.keySet())) {
			final SizzleType value = this.idmap.get(key);
			if (value instanceof SizzleScalar)
				this.idmap.put("array of " + key, new SizzleArray((SizzleScalar) value));
		}

		// variables with a global scope
		this.globals = new HashMap<String, SizzleType>();
		// set the type of the input
		this.globals.put("input", input);
		this.globals.put("true", new SizzleBool());
		this.globals.put("false", new SizzleBool());
		this.globals.put("PI", new SizzleFloat());
		this.globals.put("Inf", new SizzleFloat());
		this.globals.put("inf", new SizzleFloat());
		this.globals.put("NaN", new SizzleFloat());
		this.globals.put("nan", new SizzleFloat());

		// variables with a local scope
		this.locals = new HashMap<String, SizzleType>();

		this.aggregators = new HashMap<String, Class<?>>();
		// TODO: this needs to be a tree in order to properly handle generic
		// intrinsics
		this.functions = new FunctionTrie();

		// these generic functions require more finagling than can currently be
		// (easily) done with a static method, so they are handled with macros

		this.setFunction("def", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleAny() }, "${0} != null"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleArray(new SizzleScalar()) }, "${0}.length"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleString() }, "${0}.length()"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleBytes() }, "${0}.length"));
		this.setFunction("len", new SizzleFunction(new SizzleInt(), new SizzleType[] { new SizzleMap(new SizzleScalar(), new SizzleScalar()) },
				"${0}.keySet().size()"));
		this.setFunction("haskey", new SizzleFunction(new SizzleBool(), new SizzleType[] { new SizzleMap(new SizzleScalar(), new SizzleScalar()),
				new SizzleScalar() }, "${0}.containsKey(${1})"));
		this.setFunction("keys", new SizzleFunction(new SizzleArray(new SizzleScalar()), new SizzleType[] { new SizzleMap(new SizzleScalar(),
				new SizzleScalar()) }, "${0}.keySet().toArray()"));
		this.setFunction("lookup", new SizzleFunction(new SizzleScalar(), new SizzleType[] { new SizzleMap(new SizzleScalar(), new SizzleScalar()),
				new SizzleScalar(), new SizzleScalar() }, "(${0}.containsKey(${1}) ? ${0}.get(${1}) : ${2})"));

		// these fingerprints are identity functions
		this.setFunction("fingerprintof", new SizzleFunction(new SizzleFingerprint(), new SizzleScalar[] { new SizzleInt() }, "${0}"));
		this.setFunction("fingerprintof", new SizzleFunction(new SizzleFingerprint(), new SizzleScalar[] { new SizzleTime() }, "${0}"));

		/* expose all the casting constructors to Sawzall */

		// string to bool
		this.setFunction("bool",
				new SizzleFunction("sizzle.functions.SizzleCasts.stringToBoolean", new SizzleBool(), new SizzleScalar[] { new SizzleString() }));

		// bool to int
		this.setFunction("int", new SizzleFunction("sizzle.functions.SizzleCasts.booleanToLong", new SizzleInt(), new SizzleScalar[] { new SizzleBool() }));
		// float to int
		this.setFunction("int", new SizzleFunction(new SizzleInt(), new SizzleScalar[] { new SizzleFloat() }, "(long)${0}"));
		// time to int
		this.setFunction("int", new SizzleFunction(new SizzleInt(), new SizzleScalar[] { new SizzleTime() }, "${0}"));
		// fingerprint to int
		this.setFunction("int", new SizzleFunction(new SizzleInt(), new SizzleScalar[] { new SizzleFingerprint() }, "${0}"));
		// string to int
		this.setFunction("int", new SizzleFunction("java.lang.Long.decode", new SizzleInt(), new SizzleScalar[] { new SizzleString() }));
		// string to int with param base
		this.setFunction("int", new SizzleFunction("java.lang.Long.parseLong", new SizzleInt(), new SizzleScalar[] { new SizzleString(), new SizzleInt() }));
		// bytes to int with param encoding format
		this.setFunction("int", new SizzleFunction("sizzle.functions.SizzleCasts.bytesToLong", new SizzleInt(), new SizzleScalar[] { new SizzleBytes(),
				new SizzleString() }));

		// int to float
		this.setFunction("float", new SizzleFunction(new SizzleFloat(), new SizzleScalar[] { new SizzleInt() }, "(double)${0}"));
		// string to float
		this.setFunction("float", new SizzleFunction("java.lang.Double.parseDouble", new SizzleFloat(), new SizzleScalar[] { new SizzleString() }));

		// int to time
		this.setFunction("time", new SizzleFunction(new SizzleTime(), new SizzleScalar[] { new SizzleInt() }, "${0}"));
		// string to time
		this.setFunction("time", new SizzleFunction("sizzle.functions.SizzleCasts.stringToTime", new SizzleTime(), new SizzleScalar[] { new SizzleString() }));
		// string to time
		this.setFunction("time", new SizzleFunction("sizzle.functions.SizzleCasts.stringToTime", new SizzleTime(), new SizzleScalar[] { new SizzleString(),
				new SizzleString() }));

		// int to fingerprint
		this.setFunction("fingerprint", new SizzleFunction(new SizzleFingerprint(), new SizzleScalar[] { new SizzleInt() }, "${0}"));
		// string to fingerprint
		this.setFunction("fingerprint", new SizzleFunction("java.lang.Long.parseLong", new SizzleInt(), new SizzleScalar[] { new SizzleString(), }));
		// string to fingerprint with param base
		this.setFunction("fingerprint", new SizzleFunction("java.lang.Long.parseLong", new SizzleInt(), new SizzleScalar[] { new SizzleString(),
				new SizzleInt() }));
		// bytes to fingerprint
		this.setFunction("fingerprint", new SizzleFunction("sizzle.functions.SizzleCasts.bytesToFingerprint", new SizzleFingerprint(),
				new SizzleScalar[] { new SizzleBytes() }));

		// bool to string
		this.setFunction("string", new SizzleFunction("java.lang.Boolean.toString", new SizzleString(), new SizzleScalar[] { new SizzleBool() }));
		// int to string
		this.setFunction("string", new SizzleFunction("java.lang.Long.toString", new SizzleString(), new SizzleScalar[] { new SizzleInt() }));
		// int to string with parameter base
		this.setFunction("string", new SizzleFunction("sizzle.functions.SizzleCasts.longToString", new SizzleString(), new SizzleScalar[] { new SizzleInt(),
				new SizzleInt() }));
		// float to string
		this.setFunction("string", new SizzleFunction("java.lang.Double.toString", new SizzleString(), new SizzleScalar[] { new SizzleFloat() }));
		// time to string
		this.setFunction("string", new SizzleFunction("sizzle.functions.SizzleCasts.timeToString", new SizzleString(), new SizzleScalar[] { new SizzleTime() }));
		// fingerprint to string
		this.setFunction("string", new SizzleFunction("java.lang.Long.toHexString", new SizzleString(), new SizzleScalar[] { new SizzleFingerprint() }));
		// bytes to string
		this.setFunction("string", new SizzleFunction("new java.lang.String", new SizzleString(), new SizzleScalar[] { new SizzleBytes() }));
		// bytes to string
		this.setFunction("string", new SizzleFunction("new java.lang.String", new SizzleString(), new SizzleScalar[] { new SizzleBytes(), new SizzleString() }));

		// int to bytes with param encoding format
		this.setFunction("bytes", new SizzleFunction("sizzle.functions.SizzleCasts.longToBytes", new SizzleInt(), new SizzleScalar[] { new SizzleInt(),
				new SizzleString() }));
		// fingerprint to bytes
		this.setFunction("bytes", new SizzleFunction("sizzle.functions.SizzleCasts.fingerprintToBytes", new SizzleBytes(),
				new SizzleScalar[] { new SizzleFingerprint() }));
		// string to bytes
		this.setFunction("bytes",
				new SizzleFunction("sizzle.functions.SizzleCasts.stringToBytes", new SizzleBytes(), new SizzleScalar[] { new SizzleString() }));

		/* expose the java.lang.Math class to Sawzall */

		this.setFunction("highbit", new SizzleFunction("java.lang.Long.highestOneBit", new SizzleInt(), new SizzleScalar[] { new SizzleInt() }));

		// abs just needs to be overloaded
		this.setFunction("abs", new SizzleFunction("java.lang.Math.abs", new SizzleFloat(), new SizzleScalar[] { new SizzleInt() }));
		this.setFunction("abs", new SizzleFunction("java.lang.Math.abs", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

		// abs is also named fabs in Sawzall
		this.setFunction("fabs", new SizzleFunction("java.lang.Math.abs", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

		// log is named ln in Sawzall
		this.setFunction("ln", new SizzleFunction("java.lang.Math.log", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

		// expose the rest of the unary functions
		for (final String s : Arrays.asList("log10", "exp", "sqrt", "sin", "cos", "tan", "asin", "acos", "atan", "cosh", "sinh", "tanh", "ceil", "floor",
				"round"))
			this.setFunction(s, new SizzleFunction("java.lang.Math." + s, new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

		// expose the binary functions
		for (final String s : Arrays.asList("pow", "atan2"))
			this.setFunction(s, new SizzleFunction("java.lang.Math." + s, new SizzleFloat(), new SizzleScalar[] { new SizzleFloat(), new SizzleFloat() }));

		for (final String s : Arrays.asList("max", "min"))
			for (final SizzleScalar t : Arrays.asList(new SizzleInt(), new SizzleTime(), new SizzleFloat()))
				this.setFunction(s, new SizzleFunction("java.lang.Math." + s, new SizzleFloat(), new SizzleScalar[] { t, t }));

		// expose whatever is left, assuming we are not aiming for strict
		// compatibility
		if (!this.strictCompatibility) {
			// random takes no argument

			// these three have capitals in the name
			this.setFunction("ieeeremainder", new SizzleFunction("java.lang.Math.IEEEremainder", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat(),
					new SizzleFloat() }));
			this.setFunction("todegrees", new SizzleFunction("java.lang.Math.toDegrees", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));
			this.setFunction("toradians", new SizzleFunction("java.lang.Math.toRadians", new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

			// the unaries
			for (final String s : Arrays.asList("cbrt", "expm1", "log1p", "rint", "signum", "ulp"))
				this.setFunction(s, new SizzleFunction("java.lang.Math." + s, new SizzleFloat(), new SizzleScalar[] { new SizzleFloat() }));

			// and binaries
			for (final String s : Arrays.asList("hypot"))
				this.setFunction(s, new SizzleFunction("java.lang.Math." + s, new SizzleFloat(), new SizzleScalar[] { new SizzleFloat(), new SizzleFloat() }));
		}

		// add in the default tables
		final List<SizzleScalar> stdTypes = new ArrayList<SizzleScalar>();
		stdTypes.add(new SizzleString());
		this.set("stdout", new SizzleTable(stdTypes));
		this.set("stderr", new SizzleTable(stdTypes));
		final List<SizzleScalar> outputTypes = new ArrayList<SizzleScalar>();
		stdTypes.add(new SizzleBytes());
		this.set("output", new SizzleTable(outputTypes));

		this.importLibs(libs);
	}

	public SymbolTable cloneNonLocals() throws IOException {
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		st.globals = this.globals;
		st.functions = this.functions;
		st.locals = new HashMap<String, SizzleType>(this.locals);

		return st;
	}

	public void set(final String id, final SizzleType type) {
		this.set(id, type, false);
	}

	public void set(final String id, final SizzleType type, final boolean global) {
		if (global)
			this.globals.put(id, type);
		else
			this.locals.put(id, type);
	}

	public boolean contains(final String id) {
		return this.globals.containsKey(id) || this.locals.containsKey(id);
	}

	public SizzleType get(final String id) {
		if (this.globals.containsKey(id))
			return this.globals.get(id);

		if (this.locals.containsKey(id))
			return this.locals.get(id);

		throw new TypeException("no such identifier " + id);
	}

	public SizzleType getType(final String id) {
		if (this.idmap.containsKey(id))
			return this.idmap.get(id);

		throw new TypeException("no such type " + id);
	}

	private void importAggregator(final Class<?> clazz) {
		if (!clazz.isAnnotationPresent(AggregatorSpec.class))
			return;

		final AggregatorSpec annotation = clazz.getAnnotation(AggregatorSpec.class);

		final String type = annotation.type();
		if (type.equals("any"))
			this.aggregators.put(annotation.name(), clazz);
		else
			this.aggregators.put(annotation.name() + ":" + type, clazz);
	}

	private void importAggregator(final String c) {
		try {
			this.importAggregator(Class.forName(c, false, this.loader));
		} catch (final ClassNotFoundException e) {
			throw new TypeException("no such class " + c, e);
		}
	}

	public Class<?> getAggregator(final String name, final SizzleScalar type) {
		if (this.aggregators.containsKey(name + ":" + type))
			return this.aggregators.get(name + ":" + type);
		else if (this.aggregators.containsKey(name))
			return this.aggregators.get(name);
		else
			throw new TypeException("no such aggregator " + name + " of " + type);
	}

	private void importFunction(final Method m) {
		final FunctionSpec annotation = m.getAnnotation(FunctionSpec.class);

		final String[] formalParameters = annotation.formalParameters();
		final SizzleType[] formalParameterTypes = new SizzleType[formalParameters.length];

		for (int i = 0; i < formalParameters.length; i++) {
			final String id = formalParameters[i];

			// check for varargs
			if (id.endsWith("..."))
				formalParameterTypes[i] = new SizzleVarargs(this.getType(id.substring(0, id.indexOf('.'))));
			else
				formalParameterTypes[i] = this.getType(id);
		}

		for (final String dep : annotation.typeDependencies())
			if (dep.endsWith(".proto"))
				this.importProto(dep);
			else if (dep.endsWith(".avro"))
				this.importAvro(dep);
			else
				throw new TypeException("unknown dependency in " + dep);

		this.setFunction(annotation.name(),
				new SizzleFunction(m.getDeclaringClass().getCanonicalName() + "." + m.getName(), this.getType(annotation.returnType()), formalParameterTypes));
	}

	private void importFunctions(final Class<?> c) {
		for (final Method m : c.getMethods())
			if (m.isAnnotationPresent(FunctionSpec.class))
				this.importFunction(m);
	}

	private void importFunctions(final String c) {
		try {
			this.importFunctions(Class.forName(c));
		} catch (final ClassNotFoundException e) {
			throw new TypeException("no such class " + c, e);
		}
	}

	private void importLibs(final List<URL> urls) throws IOException {
		final AnnotationDB db = new AnnotationDB();
		db.setScanMethodAnnotations(true);
		db.setScanClassAnnotations(true);

		// let's assume the entire runtime is in the same classpath entry as the
		// int sum aggregator
		db.scanArchives(ClasspathUrlFinder.findClassBase(IntSumAggregator.class));

		for (final URL s : ClasspathUrlFinder.findClassPaths())
			if (s.getPath().endsWith("/"))
				db.scanArchives(s);

		for (final URL url : urls)
			db.scanArchives(url);

		final Map<String, Set<String>> annotationIndex = db.getAnnotationIndex();

		for (final String c : annotationIndex.get(AggregatorSpec.class.getCanonicalName()))
			try {
				this.importAggregator(c);
			} catch (final NoClassDefFoundError e) {
				SymbolTable.LOG.error("unable to import aggregator " + c + ": " + e.getClass().getSimpleName() + " for " + e.getMessage());
			}

		for (final String c : annotationIndex.get(FunctionSpec.class.getCanonicalName())) {
			this.importFunctions(c);
		}
	}

	void importProto(final String name) {
		final String camelCased = SymbolTable.camelCase(name.substring(0, name.indexOf('.')));

		Class<?> wrapper;
		try {
			wrapper = Class.forName("sizzle.types." + camelCased);
		} catch (final ClassNotFoundException e) {
			throw new TypeException("no such proto " + name);
		}

		for (final Class<?> c : wrapper.getClasses()) {
			final Map<String, SizzleType> members = new HashMap<String, SizzleType>();

			for (final Field field : c.getDeclaredFields()) {
				if (!field.getName().endsWith("_"))
					continue;

				final String member = SymbolTable.deCamelCase(field.getName().substring(0, field.getName().length() - 1));

				final Class<?> type = field.getType();

				members.put(member, this.protomap.get(type));
			}

			final SizzleTuple SizzleTuple = new SizzleTuple(c.getSimpleName(), members);
			this.idmap.put(c.getSimpleName(), SizzleTuple);
			// TODO support protocol buffer casts
			// this.castmap.put("bytes2" + SizzleTuple, new
			// SizzleFunction("UNIMPLEMENTED", new SizzleAny()));
		}
	}

	private void importAvro(final String dep) {
		throw new RuntimeException("unimplemented");
	}

	public SizzleFunction getFunction(final String id) {
		return this.getFunction(id, new SizzleType[0]);
	}

	public SizzleFunction getFunction(final String id, final List<SizzleType> formalParameters) {
		return this.getFunction(id, formalParameters.toArray(new SizzleType[formalParameters.size()]));
	}

	public SizzleFunction getFunction(final String id, final SizzleType[] formalParameters) {
		final SizzleFunction func = this.functions.getFunction(id, formalParameters);

		return func;
	}

	public void setFunction(final String id, final SizzleFunction sizzleFunction) {
		this.functions.addFunction(id, sizzleFunction);
	}

	public boolean hasCast(final SizzleType from, final SizzleType to) {
		try {
			this.getFunction(to.toString(), new SizzleType[] { from });

			return true;
		} catch (final TypeException e) {
			return false;
		}
	}

	public SizzleFunction getCast(final SizzleType from, final SizzleType to) {
		return this.getFunction(to.toString(), new SizzleType[] { from });
	}

	public void setInitializerType(final SizzleType initializerType) {
		this.initializerType = initializerType;
	}

	public SizzleType getInitializerType() {
		return this.initializerType;
	}

	public void setMapName(final String mapName) {
		this.mapName = mapName;
	}

	public String getMapName() {
		return this.mapName;
	}

	public String getStaticInitializer() {
		return this.staticInitializer;
	}

	public void setStaticInitializer(final String staticInitializer) {
		this.staticInitializer = staticInitializer;
	}

	@Override
	public String toString() {
		final List<String> r = new ArrayList<String>();

		for (final Entry<String, SizzleType> entry : this.locals.entrySet())
			r.add(entry.getKey() + ":" + entry.getValue());

		return r.toString();
	}

	private static String camelCase(final String string) {
		final StringBuilder camelized = new StringBuilder();

		boolean lower = false;
		for (final char c : string.toCharArray())
			if (c == '_')
				lower = false;
			else if (Character.isDigit(c)) {
				camelized.append(c);
				lower = false;
			} else if (Character.isLetter(c)) {
				if (lower)
					camelized.append(c);
				else
					camelized.append(Character.toUpperCase(c));

				lower = true;
			}

		return camelized.toString();
	}

	private static String deCamelCase(final String string) {
		final StringBuilder decamelized = new StringBuilder();

		for (final char c : string.toCharArray())
			if (Character.isUpperCase(c)) {
				decamelized.append('_');
				decamelized.append(Character.toLowerCase(c));
			} else
				decamelized.append(c);

		return decamelized.toString();
	}
}