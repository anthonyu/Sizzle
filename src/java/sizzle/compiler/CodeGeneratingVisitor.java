package sizzle.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import sizzle.parser.syntaxtree.Assignment;
import sizzle.parser.syntaxtree.AssignmentList;
import sizzle.parser.syntaxtree.AssignmentRest;
import sizzle.parser.syntaxtree.AssignmentStatement;
import sizzle.parser.syntaxtree.Atom;
import sizzle.parser.syntaxtree.Block;
import sizzle.parser.syntaxtree.CallExpression;
import sizzle.parser.syntaxtree.ComparisonExpression;
import sizzle.parser.syntaxtree.EmitStatement;
import sizzle.parser.syntaxtree.Expression;
import sizzle.parser.syntaxtree.ExpressionList;
import sizzle.parser.syntaxtree.ExpressionRest;
import sizzle.parser.syntaxtree.FloatingPointLiteral;
import sizzle.parser.syntaxtree.ForStatement;
import sizzle.parser.syntaxtree.Identifier;
import sizzle.parser.syntaxtree.IdentifierList;
import sizzle.parser.syntaxtree.IdentifierRest;
import sizzle.parser.syntaxtree.IfStatement;
import sizzle.parser.syntaxtree.Index;
import sizzle.parser.syntaxtree.IndexExpression;
import sizzle.parser.syntaxtree.Initializer;
import sizzle.parser.syntaxtree.IntegerLiteral;
import sizzle.parser.syntaxtree.MappingList;
import sizzle.parser.syntaxtree.MemberExpression;
import sizzle.parser.syntaxtree.Node;
import sizzle.parser.syntaxtree.NodeChoice;
import sizzle.parser.syntaxtree.NodeOptional;
import sizzle.parser.syntaxtree.NodeSequence;
import sizzle.parser.syntaxtree.NodeToken;
import sizzle.parser.syntaxtree.ParentheticalExpression;
import sizzle.parser.syntaxtree.PlusExpression;
import sizzle.parser.syntaxtree.Program;
import sizzle.parser.syntaxtree.Proto;
import sizzle.parser.syntaxtree.SliceExpression;
import sizzle.parser.syntaxtree.Start;
import sizzle.parser.syntaxtree.Statement;
import sizzle.parser.syntaxtree.StringLiteral;
import sizzle.parser.syntaxtree.TableDeclaration;
import sizzle.parser.syntaxtree.Term;
import sizzle.parser.syntaxtree.TimesExpression;
import sizzle.parser.syntaxtree.Typle;
import sizzle.parser.syntaxtree.TypleList;
import sizzle.parser.syntaxtree.TypleRest;
import sizzle.parser.syntaxtree.UnaryExpression;
import sizzle.parser.syntaxtree.VariableDeclaration;
import sizzle.parser.syntaxtree.WhenStatement;
import sizzle.parser.syntaxtree.WhileStatement;
import sizzle.parser.visitor.GJDepthFirst;
import sizzle.types.SizzleArray;
import sizzle.types.SizzleBytes;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleMap;
import sizzle.types.SizzleScalar;
import sizzle.types.SizzleString;
import sizzle.types.SizzleTable;
import sizzle.types.SizzleType;

class AggregatorDescription {
	private String aggregator;
	private List<SizzleScalar> types;
	private List<String> parameters;

	public AggregatorDescription(final String aggregator, final List<SizzleScalar> types) {
		this(aggregator, types, null);
	}

	public AggregatorDescription(final String aggregator, final List<SizzleScalar> types, final List<String> parameters) {
		this.aggregator = aggregator;
		this.types = types;
		this.parameters = parameters;
	}

	/**
	 * @return the name
	 */
	public String getAggregator() {
		return this.aggregator;
	}

	/**
	 * @return the parameters
	 */
	public List<String> getParameters() {
		return this.parameters;
	}

	/**
	 * @return the types
	 */
	public List<SizzleScalar> getTypes() {
		return this.types;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setAggregator(final String aggregator) {
		this.aggregator = aggregator;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(final List<String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @param types
	 *            the types to set
	 */
	public void setTypes(final List<SizzleScalar> types) {
		this.types = types;
	}
}

public class CodeGeneratingVisitor extends GJDepthFirst<String, SymbolTable> {
	private String name;

	private final NameFindingVisitor namefinder;
	private final TypeCheckingVisitor typefinder;
	private final IndexeeFindingVisitor indexeefinder;
	private StaticDeclarationCodeGeneratingVisitor staticfinder;
	private String staticDeclarations;

	private final Map<String, AggregatorDescription> aggregators;
	private final Map<String, String> codeMap;

	public CodeGeneratingVisitor() {
		this.namefinder = new NameFindingVisitor();
		this.typefinder = new TypeCheckingVisitor(this.namefinder);
		this.indexeefinder = new IndexeeFindingVisitor(this.namefinder);

		this.aggregators = new HashMap<String, AggregatorDescription>();

		this.codeMap = new HashMap<String, String>();
		this.codeMap.put("true", "true");
		this.codeMap.put("false", "false");
		this.codeMap.put("PI", "Math.PI");
		this.codeMap.put("Inf", "Double.POSITIVE_INFINITY");
		this.codeMap.put("inf", "Double.POSITIVE_INFINITY");
		this.codeMap.put("NaN", "Double.NaN");
		this.codeMap.put("nan", "Double.NaN");
		this.codeMap.put("SECOND", "1000 * 1000");
		this.codeMap.put("SEC", "1000 * 1000");
		this.codeMap.put("MINUTE", "(60 * 1000 * 1000)");
		this.codeMap.put("MIN", "(60 * 1000 * 1000)");
		this.codeMap.put("HOUR", "(60 * 60 * 1000 * 1000)");
		this.codeMap.put("HR", "(60 * 60 * 1000 * 1000)");
	}

	public CodeGeneratingVisitor(final String name) {
		this();

		this.staticfinder = new StaticDeclarationCodeGeneratingVisitor(this.namefinder);

		this.name = name;
	}

	public static String expand(final String template, final String[] parameters) {
		String replaced = template;

		for (int i = 0; i < parameters.length; i++)
			replaced = replaced.replace("${" + i + "}", parameters[i]);

		return replaced;
	}

	// TODO: really support implicit cast on assignment?
	/** {@inheritDoc} */
	@Override
	public String visit(final Assignment n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;

		argu.setInitializerType(argu.get(id));
		final String source = n.f0.accept(this, argu) + " = " + n.f2.accept(this, argu);
		argu.setInitializerType(null);

		return source;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final AssignmentList n, final SymbolTable argu) {
		final StringBuilder source = new StringBuilder(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				source.append(((AssignmentRest) node).accept(this, argu));

		return source.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final AssignmentRest n, final SymbolTable argu) {
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final AssignmentStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Atom n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // identifier
			return ((Identifier) n.f0.choice).accept(this, argu);
		case 1: // string
			return ((StringLiteral) n.f0.choice).accept(this, argu);
		case 2: // integer
			return ((IntegerLiteral) n.f0.choice).accept(this, argu);
		case 3: // float
			return ((FloatingPointLiteral) n.f0.choice).accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Block n, final SymbolTable argu) {
		final StringBuilder source = new StringBuilder("{\n");

		if (n.f1.present()) {
			final List<Node> arrayList = n.f1.nodes;
			for (final Node i : arrayList)
				source.append(((Statement) i).accept(this, argu) + "\n");
		}

		source.append("}");

		return source.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final CallExpression n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;

		if (n.f2.present()) {
			final SizzleFunction function = argu.getFunction(id, this.typefinder.check((ExpressionList) n.f2.node, argu));

			final String parameters = ((ExpressionList) n.f2.node).accept(this, argu);

			if (function.hasMacro())
				return CodeGeneratingVisitor.expand(function.getMacro(), parameters.split(","));
			else
				return function.getCanonicalName() + "(" + parameters + ")";
		} else {
			final SizzleFunction function = argu.getFunction(id);
			return function.getCanonicalName() + "()";
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ComparisonExpression n, final SymbolTable argu) {
		final String lhs = n.f0.accept(this, argu);

		if (!n.f1.present())
			return lhs;

		SizzleType type;
		try {
			type = this.typefinder.visit(n.f0, argu.cloneNonLocals());
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}
		final List<Node> nodes = ((NodeSequence) n.f1.node).nodes;

		final String operator = ((NodeToken) ((NodeChoice) nodes.get(0)).choice).tokenImage;

		final String rhs = ((PlusExpression) nodes.get(1)).accept(this, argu);
		if (operator.equals("==") || operator.equals("!="))
			if (type.getClass().equals(SizzleArray.class))
				return "java.util.Arrays.equals(" + lhs + ", " + rhs + ")";
			else if (type.getClass().equals(SizzleString.class))
				return (operator.equals("!=") ? "!" : "") + rhs + ".equals(" + lhs + ")";
			else if (Character.isUpperCase(type.toJavaType().toCharArray()[0]))
				return (operator.equals("!=") ? "!" : "") + rhs + ".equals(" + lhs + ")";
			else
				return lhs + " " + operator + " " + " " + rhs;
		else
			return lhs + " " + operator + " " + rhs;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final EmitStatement n, final SymbolTable argu) {
		final String name = n.f1.f0.tokenImage;

		final SizzleTable t = (SizzleTable) argu.get(name);

		String index = "\"[]\"";
		if (n.f2.present()) {
			final List<String> indices = new ArrayList<String>();
			for (final Node i : n.f2.nodes)
				indices.add(((NodeSequence) i).nodes.get(1).accept(this, argu));
			final StringBuilder src = new StringBuilder("\"[\" + " + indices.get(0) + " + \"]\"");
			for (int i = 1; i < indices.size(); i++)
				src.append(" + \"[\" + " + indices.get(i) + " + \"]\"");
			index = src.toString();
		}

		String weight = null;
		if (n.f5.present())
			weight = ((Expression) ((NodeSequence) n.f5.node).nodes.get(1)).accept(this, argu);

		final StringBuilder src = new StringBuilder();

		if (n.f4.f1.present()) {
			String value;
			if (weight == null)
				value = "new sizzle.io.EmitValue(" + n.f4.f0.accept(this, argu) + ")";
			else
				value = "new sizzle.io.EmitValue(" + n.f4.f0.accept(this, argu) + ", " + weight + ")";

			src.append("context.write(new sizzle.io.EmitKey(" + index + ", \"" + name + "." + t.getSubscript(0) + "\"), " + value + ");");

			final List<Node> nodes = n.f4.f1.nodes;
			for (int i = 0; i < nodes.size(); i++) {
				if (weight == null)
					value = "new sizzle.io.EmitValue(" + ((ExpressionRest) nodes.get(i)).f1.accept(this, argu) + ")";
				else
					value = "new sizzle.io.EmitValue(" + ((ExpressionRest) nodes.get(i)).f1.accept(this, argu) + ", " + weight + ")";

				src.append("context.write(new sizzle.io.EmitKey(" + index + ", \"" + name + "." + t.getSubscript(i + 1) + "\"), " + value + ");");
			}
		} else {
			String value;
			if (weight == null)
				value = "new sizzle.io.EmitValue(" + n.f4.f0.accept(this, argu) + ")";
			else
				value = "new sizzle.io.EmitValue(" + n.f4.f0.accept(this, argu) + ", " + weight + ")";

			src.append("context.write(new sizzle.io.EmitKey(" + index + ", \"" + name + "\"), " + value + ");");
		}

		return src.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Expression n, final SymbolTable argu) {
		final StringBuilder expression = new StringBuilder(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node node : n.f1.nodes)
				expression.append(" " + ((NodeToken) ((NodeChoice) ((NodeSequence) node).nodes.get(0)).choice).tokenImage + " "
						+ ((ComparisonExpression) ((NodeSequence) node).nodes.get(1)).accept(this, argu));

		return expression.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ExpressionList n, final SymbolTable argu) {
		final StringBuilder rest = new StringBuilder();
		if (n.f1.present())
			for (final Node i : n.f1.nodes)
				rest.append(", " + ((ExpressionRest) i).accept(this, argu));

		return n.f0.accept(this, argu) + rest.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ExpressionRest n, final SymbolTable argu) {
		return n.f1.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final FloatingPointLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ForStatement n, final SymbolTable argu) {
		// when they say "for loops [...] take on their familiar form", I assume
		// they mean K&R form, e.g. declarations not allowed in initializer.
		String src = "for (";

		if (n.f2.present())
			src += ((AssignmentList) n.f2.node).accept(this, argu);

		src += "; ";

		if (n.f4.present())
			src += ((ExpressionList) n.f4.node).accept(this, argu);

		src += "; ";

		if (n.f6.present())
			src += ((ExpressionList) n.f6.node).accept(this, argu);

		return src + ")\n" + n.f8.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Identifier n, final SymbolTable argu) {
		// TODO: support protobufs/sequence files/avro here

		// check if it is a special constant
		if (this.codeMap.containsKey(n.f0.tokenImage))
			return this.codeMap.get(n.f0.tokenImage);

		// otherwise return the Java variable name
		return "___" + n.f0.tokenImage;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IdentifierList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IdentifierRest n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IfStatement n, final SymbolTable argu) {
		String src = "if (" + n.f2.accept(this, argu) + ")\n" + n.f4.accept(this, argu) + "\n";

		if (n.f5.present())
			src += "else\n" + ((Statement) ((NodeSequence) n.f5.node).elementAt(1)).accept(this, argu);

		return src;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Index n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IndexExpression n, final SymbolTable argu) {
		final String id = n.f0.accept(this, argu);
		final SizzleType type = argu.get(n.f0.f0.tokenImage);

		if (n.f1.present()) {
			final String index = ((NodeSequence) n.f1.node).nodes.get(1).accept(this, argu);
			if (type.getClass().equals(SizzleArray.class))
				return "(" + id + ".length > " + index + " ? " + id + "[(int)" + index + "]" + " : null)";
			else if (type.getClass().equals(SizzleMap.class))
				return id + ".get(" + index + ")";
			else
				throw new RuntimeException("wtf");
		} else
			return id;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Initializer n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // expression
			final SizzleType t = argu.getInitializerType();
			final SizzleType u = this.typefinder.visit(((Expression) n.f0.choice), argu);

			if (!t.assigns(u) && argu.hasCast(u, t))
				return argu.getCast(u, t).getCanonicalName() + "(" + ((Expression) n.f0.choice).accept(this, argu) + ")";
			return ((Expression) n.f0.choice).accept(this, argu);
		case 1: // map or array
			final NodeOptional nodeOptional = (NodeOptional) ((NodeSequence) n.f0.choice).nodes.get(1);
			if (nodeOptional.present()) {
				final NodeChoice node = (NodeChoice) nodeOptional.node;
				switch (node.which) {
				case 0: // mapping list
					argu.setStaticInitializer(((MappingList) node.choice).accept(this, argu));
					return "new " + argu.getInitializerType().toJavaType() + "()";
				case 1: // expression list
					return "new " + argu.getInitializerType().toJavaType() + " { " + ((ExpressionList) node.choice).accept(this, argu) + " }";
				default:
					throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
				}
			} else
				return "";
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final IntegerLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final MemberExpression n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final ParentheticalExpression n, final SymbolTable argu) {
		return "(" + n.f1.accept(this, argu) + ")";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final PlusExpression n, final SymbolTable argu) {
		final StringBuilder src = new StringBuilder();

		src.append(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node i : n.f1.nodes) {
				final NodeSequence nodeSequence = (NodeSequence) i;
				src.append(" " + ((NodeToken) ((NodeChoice) nodeSequence.nodes.get(0)).choice).tokenImage + " " + nodeSequence.nodes.get(1).accept(this, argu));
			}

		return src.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Program n, final SymbolTable argu) {
		if (argu.get("input").equals(new SizzleString()))
			this.codeMap.put("input", "value.toString()");
		else if (argu.get("input").equals(new SizzleBytes()))
			this.codeMap.put("input", "value.toString().getBytes()");

		final StringBuilder statements = new StringBuilder();

		for (final Node i : n.f1.nodes) {
			final String src = ((Statement) i).accept(this, argu);
			if (src != null)
				statements.append(src + "\n");
		}

		final StringBuilder aggregators = new StringBuilder();
		for (final Entry<String, AggregatorDescription> e : this.aggregators.entrySet()) {
			final String id = e.getKey();
			final SizzleTable t = (SizzleTable) argu.get(id);
			final AggregatorDescription description = e.getValue();
			final String parameters = description.getParameters() == null ? "" : description.getParameters().get(0);
			final List<SizzleScalar> types = description.getTypes();
			final List<String> subscripts = t.getSubscripts();

			aggregators.append("this.aggregators.put(\"stdout\", new sizzle.aggregators.StdoutAggregator());\n");
			aggregators.append("this.aggregators.put(\"stderr\", new sizzle.aggregators.StderrAggregator());\n");
			aggregators.append("this.aggregators.put(\"output\", new sizzle.aggregators.OutputAggregator());\n");

			for (int i = 0; i < types.size(); i++) {
				final Class<?> aggregatorClass = argu.getAggregator(description.getAggregator(), types.get(i));
				if (subscripts == null)
					aggregators.append("this.aggregators.put(\"" + id + "\", new " + aggregatorClass.getCanonicalName() + "(" + parameters + "));\n");
				else
					aggregators.append("this.aggregators.put(\"" + id + "." + subscripts.get(i) + "\", new " + aggregatorClass.getCanonicalName() + "("
							+ parameters + "));\n");
			}

		}

		// FIXME: figure the input format stuff out dynamically
		final String keyClass = "org.apache.hadoop.io.LongWritable";
		final String valueClass = "org.apache.hadoop.io.Text";
		final String inputFormatClass = "org.apache.hadoop.mapreduce.lib.input.TextInputFormat";

		return "package sizzle;\n\npublic class "
				+ this.name
				+ " extends sizzle.runtime.SizzleRunner {\n/** {@inheritDoc} */ @Override \npublic org.apache.hadoop.mapreduce.Job job(org.apache.hadoop.fs.Path[] ins, org.apache.hadoop.fs.Path out, boolean robust) throws java.io.IOException {\norg.apache.hadoop.mapreduce.Job job = super.job(ins, out, robust);\njob.setInputFormatClass("
				+ inputFormatClass
				+ ".class);job.setJobName(\""
				+ this.name
				+ ": \" + out);\n\njob.setMapperClass("
				+ this.name
				+ "SizzleMapper.class);\njob.setCombinerClass("
				+ this.name
				+ "SizzleCombiner.class);\njob.setReducerClass("
				+ this.name
				+ "SizzleReducer.class);\n\nreturn job;\n}\n\npublic static void main(String[] args) throws java.io.IOException, InterruptedException, ClassNotFoundException {\n"
				+ this.name
				+ " runner = new "
				+ this.name
				+ "();\nrunner.job(new org.apache.hadoop.fs.Path[] { new org.apache.hadoop.fs.Path(args[0]) }, new org.apache.hadoop.fs.Path(args[1]), args.length > 2).submit();\n}\n\nstatic class "
				+ this.name
				+ "SizzleMapper extends sizzle.runtime.SizzleMapper {\n"
				+ this.staticDeclarations
				+ "/** {@inheritDoc} */ @Override \nprotected void map("
				+ keyClass
				+ " key, "
				+ valueClass
				+ " value, org.apache.hadoop.mapreduce.Mapper<"
				+ keyClass
				+ ", "
				+ valueClass
				+ ", sizzle.io.EmitKey, sizzle.io.EmitValue>.Context context) throws java.io.IOException, InterruptedException {\ntry{\n"
				+ statements.toString()
				+ "} catch (java.io.IOException e) {\nthrow e;\n} catch (InterruptedException e) {\nthrow e;\n} catch (RuntimeException e) {\nif (this.robust)\nLOG.error(e.getClass().getName() + \" caught\", e);\nelse\nthrow e;\n} catch (Exception e) {\nif (this.robust)\nLOG.error(e.getClass().getName() + \" caught\", e);\nelse\nthrow new RuntimeException(e.getClass().getName() + \" caught\", e);\n}\n}\n}\n\nstatic class "
				+ this.name + "SizzleCombiner extends sizzle.runtime.SizzleCombiner {\npublic " + this.name + "SizzleCombiner() {\nsuper();\n\n"
				+ aggregators.toString() + "}\n}\n\nstatic class " + this.name + "SizzleReducer extends sizzle.runtime.SizzleReducer {\npublic " + this.name
				+ "SizzleReducer() {\nsuper();\n\n" + aggregators.toString() + "}\n}\n@Override\npublic sizzle.runtime.SizzleMapper getMapper() {\nreturn new "
				+ this.name + "SizzleMapper();\n}\n@Override\npublic sizzle.runtime.SizzleCombiner getCombiner() {\nreturn new " + this.name
				+ "SizzleCombiner();\n}\n@Override\npublic sizzle.runtime.SizzleReducer getReducer() {\nreturn new " + this.name + "SizzleReducer();\n}\n}\n";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Proto n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final SliceExpression n, final SymbolTable argu) {
		if (n.f1.present()) {
			final String lower = ((NodeSequence) n.f1.nodes.get(0)).elementAt(1).accept(this, argu);
			final String upper = ((NodeSequence) n.f1.nodes.get(0)).elementAt(3).accept(this, argu);
			final String id = n.f0.accept(this, argu);
			return "(" + lower + " > " + upper + " ? java.util.Arrays.copyOfRange(" + id + ", " + upper + ", " + upper + ") : java.util.Arrays.copyOfRange("
					+ id + ", " + lower + ", " + upper + "))";
		} else
			return "";
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Start n, final SymbolTable argu) {
		this.staticDeclarations = this.staticfinder.visit(n, argu);
		return n.f0.accept(this, argu);
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Statement n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // block
			return ((Block) n.f0.choice).accept(this, argu);
		case 1: // table declaration
			return ((TableDeclaration) n.f0.choice).accept(this, argu);
		case 2: // static variable declaration
			return ((VariableDeclaration) n.f0.choice).accept(this, argu);
		case 3: // non-static variable declaration
			return ((VariableDeclaration) n.f0.choice).accept(this, argu);
		case 5: // emit statement
			return ((EmitStatement) n.f0.choice).accept(this, argu);
		case 6: // if statement
			return ((IfStatement) n.f0.choice).accept(this, argu);
		case 8: // for statement
			return ((ForStatement) n.f0.choice).accept(this, argu);
		case 9: // when statement
			return ((WhenStatement) n.f0.choice).accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final StringLiteral n, final SymbolTable argu) {
		return n.f0.tokenImage;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final TableDeclaration n, final SymbolTable argu) {
		final String id = n.f0.f0.tokenImage;
		final String aggregator = n.f3.f0.tokenImage;

		final SizzleTable t = (SizzleTable) argu.get(id);

		if (n.f4.present()) {
			final String parameter = ((NodeSequence) n.f4.node).nodes.get(1).accept(this, argu);
			this.aggregators.put(id, new AggregatorDescription(aggregator, t.getTypes(), Arrays.asList(parameter)));
		} else
			this.aggregators.put(id, new AggregatorDescription(aggregator, t.getTypes()));

		return null;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Term n, final SymbolTable argu) {
		switch (n.f0.which) {
		case 0: // call
			return ((CallExpression) n.f0.choice).accept(this, argu);
		case 2: // wide index into array
			return ((SliceExpression) n.f0.choice).accept(this, argu);
		case 3: // index into array
			return ((IndexExpression) n.f0.choice).accept(this, argu);
		case 4: // expressions in parantheses
			return ((ParentheticalExpression) n.f0.choice).accept(this, argu);
		case 5: // atom
			return ((Atom) n.f0.choice).accept(this, argu);
		default:
			throw new RuntimeException("unexpected choice " + n.f0.which + " is " + n.f0.choice.getClass());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final TimesExpression n, final SymbolTable argu) {
		final StringBuilder src = new StringBuilder();

		src.append(n.f0.accept(this, argu));

		if (n.f1.present())
			for (final Node i : n.f1.nodes) {
				final NodeSequence nodeSequence = (NodeSequence) i;
				src.append(" " + ((NodeToken) ((NodeChoice) nodeSequence.nodes.get(0)).choice).tokenImage + " " + nodeSequence.nodes.get(1).accept(this, argu));
			}

		return src.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final Typle n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final TypleList n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final TypleRest n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final UnaryExpression n, final SymbolTable argu) {
		String expression = n.f0.accept(this, argu);

		if (n.f1.present())
			expression += ((NodeToken) ((NodeChoice) n.f1.node).choice).tokenImage;

		return expression;
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final VariableDeclaration n, final SymbolTable argu) {
		// already handled by the static declaration visitor
		if (n.f0.present())
			return "";

		final StringBuilder src = new StringBuilder();

		final SizzleType type = argu.get(n.f1.f0.f0.tokenImage);
		String initializer;
		if (n.f4.present()) {
			argu.setInitializerType(type);
			initializer = " = " + ((Initializer) ((NodeSequence) n.f4.node).nodes.get(1)).accept(this, argu);
			argu.setInitializerType(null);
		} else
			initializer = "";

		for (final String id : this.namefinder.visit(n.f1)) {
			final String javaType = type.toJavaType();

			src.append(javaType + " ___" + id + initializer + ";");
		}

		return src.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final WhenStatement n, final SymbolTable argu) {
		SymbolTable st;
		try {
			st = argu.cloneNonLocals();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		final StringBuilder src = new StringBuilder();
		for (final Node node : n.f2.nodes) {
			// String quant;
			// switch (((NodeChoice)((NodeSequence)node).nodes.get(2)).which) {
			// case 0:
			// quant = "some";
			// break;
			// case 1:
			// quant = "each";
			// break;
			// case 2:
			// quant = "all";
			// break;
			// default:
			// throw new RuntimeException("unexpected choice " +
			// ((NodeChoice)((NodeSequence)node).nodes.get(2)).which + " is " +
			// ((NodeChoice)((NodeSequence)node).nodes.get(2)).choice.getClass());
			// }

			final SizzleType type = st.getType(((Identifier) ((NodeSequence) node).nodes.get(3)).f0.tokenImage);

			final Set<String> ids = this.namefinder.visit((IdentifierList) ((NodeSequence) node).nodes.get(0));

			for (final String id : ids)
				st.set(id, type);

			for (final String id : ids) {
				st.set(id, type);
				final Set<String> indexees = this.indexeefinder.visit(n.f3, id);

				if (indexees.size() > 0) {
					indexees.addAll(this.indexeefinder.visit(n.f5, id));
					src.append("for (int ___" + id + " = 0; ");
					final List<String> array = new ArrayList<String>(indexees);
					src.append("___" + id + " < ___" + array.get(0) + ".length");
					for (int i = 1; i < array.size(); i++)
						src.append(" && " + id + " < " + array.get(i) + ".length");
					src.append("; ___" + id + "++)\n");
				}
			}
		}

		src.append("if (" + n.f3.accept(this, st) + ")\n");

		src.append(n.f5.accept(this, st));

		return src.toString();
	}

	/** {@inheritDoc} */
	@Override
	public String visit(final WhileStatement n, final SymbolTable argu) {
		throw new RuntimeException("unimplemented");
	}
}