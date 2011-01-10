package sizzle.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javaxtools.compiler.CharSequenceCompiler;
import javaxtools.compiler.CharSequenceCompilerException;

import org.antlr.stringtemplate.CommonGroupLoader;
import org.antlr.stringtemplate.PathGroupLoader;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateGroupLoader;
import org.antlr.tool.ErrorManager;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;
import sizzle.parser.ParseException;
import sizzle.parser.SizzleParser;
import sizzle.runtime.SizzleRunner;
import sizzle.types.SizzleBytes;

@SuppressWarnings("unused")
public class TestCodeGeneratingVisitor {
	private static TypeCheckingVisitor typeChecker;
	private static CharSequenceCompiler<SizzleRunner> compiler;
	private static StringTemplateGroup stg;

	@BeforeClass
	public static void init() throws IOException {
		TestCodeGeneratingVisitor.typeChecker = new TypeCheckingVisitor();
		// this is a JavaCC wtf, do not remove
		try {
			new SizzleParser(new StringReader(""));
		} catch (final Error e) {
			// eat it
		}

		TestCodeGeneratingVisitor.compiler = new CharSequenceCompiler<SizzleRunner>(null, null);

		final StringTemplateGroupLoader loader = new PathGroupLoader("/home/anthonyu/Projects/Sizzle/src/antlr/", ErrorManager.getStringTemplateErrorListener());
		StringTemplateGroup.registerGroupLoader(loader);

		TestCodeGeneratingVisitor.stg = StringTemplateGroup.loadGroup("SizzleJavaHadoop");
	}

	@Test
	public void testCodeGeneratingVisitorHello() throws IOException, ParseException, ClassCastException, InstantiationException, IllegalAccessException,
			CharSequenceCompilerException {
		final String source = "emit stdout <- \"Hello, World!\";\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("Hello", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.Hello", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text(""));
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorSimple() throws IOException, ParseException, ClassCastException, InstantiationException, IllegalAccessException,
			CharSequenceCompilerException {
		final String source = "count: table sum of int;\ntotal: table sum of float;\nsum_of_squares: table sum of float;\nx: float = input;\nemit count <- 1;\nemit total <- x;\nemit sum_of_squares <- x * x;\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("Simple", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.Simple", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("1.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("2.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("3.0"));
		mapReduceDriver.addOutput(new Text("count[] = 3"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("sum_of_squares[] = 14.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("total[] = 6.0"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorSimpleCompound() throws IOException, ParseException, ClassCastException, InstantiationException,
			IllegalAccessException, CharSequenceCompilerException {
		final String source = "s: table sum of { count: int, total: float, sum_of_squares: float };\nx: float = input;\nemit s <- { 1, x, x * x };\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("SimpleCompound", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.SimpleCompound", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("1.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("2.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("3.0"));
		mapReduceDriver.addOutput(new Text("s[] = { 3, 6.0, 14.0 }"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorQuickExample() throws IOException, ParseException, ClassCastException, InstantiationException, IllegalAccessException,
			CharSequenceCompilerException {
		final String source = "topwords: table top(3) of word: string weight count: int;\nline: bytes = input;\nfields: array of bytes = splitcsvline(line);\nw: string = string(fields[0]);\nc: int = int(string(fields[1]), 10);\nif (c != 0) {\nemit topwords <- w weight c;\n}\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("QuickExample", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.QuickExample", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());

		mapReduceDriver.addInput(new LongWritable(0), new Text("abc,1"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("def,2"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("ghi,3"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("def,4"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("jkl,5"));
		mapReduceDriver.addOutput(new Text("topwords[] = def, 6, 0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("topwords[] = jkl, 5, 0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("topwords[] = ghi, 3, 0"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorRealWordCount() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "count: table sum[word: string] of int;\nline: string = input;\nwords: array of string = sawzall(line, \"[A-Za-z]+\");\nfor (i : int = 0; i < len(words); i++)\n\temit count[lowercase(words[i])] <- 1;\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("RealWordCount", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.RealWordCount", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("To be, or not to be: that is the question:"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("Whether 'tis nobler in the mind to suffer"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("The slings and arrows of outrageous fortune,"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("Or to take arms against a sea of troubles,"));
		mapReduceDriver.addOutput(new Text("count[a] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[against] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[and] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[arms] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[arrows] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[be] = 2"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[fortune] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[in] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[is] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[mind] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[nobler] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[not] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[of] = 2"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[or] = 2"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[outrageous] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[question] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[sea] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[slings] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[suffer] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[take] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[that] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[the] = 3"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[tis] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[to] = 4"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[troubles] = 1"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("count[whether] = 1"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorWordStats() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "scrabble: table maximum(5) of string weight score: int;\nlongest: table maximum(5) of string weight length: int;\nline: string = input;\nwords: array of string = sawzall(line, \"[A-Za-z]+\");\nfor (i: int = 0; i < len(words); i++) {\n\temit scrabble <- lowercase(words[i]) weight scrabble(words[i]);\n\temit longest <- lowercase(words[i]) weight len(words[i]);\n}\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("WordStats", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.WordStats", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("To be, or not to be: that is the question:"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("Whether 'tis nobler in the mind to suffer"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("The slings and arrows of outrageous fortune,"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("Or to take arms against a sea of troubles,"));
		mapReduceDriver.addOutput(new Text("longest[] = outrageous weight 10.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("longest[] = question weight 8.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("longest[] = troubles weight 8.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("longest[] = against weight 7.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("longest[] = fortune weight 7.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("scrabble[] = question weight 17.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("scrabble[] = whether weight 16.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("scrabble[] = suffer weight 12.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("scrabble[] = outrageous weight 11.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("scrabble[] = fortune weight 10.0"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorFilteredMeanValue() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "value: table mean of float;\nf: float = input;\nif (f != 0.0) {\n\temit value <- f;\n}\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("FilteredMeanValue", TestCodeGeneratingVisitor.stg);

		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.FilteredMeanValue", src, null, new Class<?>[] { SizzleRunner.class })
					.newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("0.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("1.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("0.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("2.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("0.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("3.0"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("0.0"));
		mapReduceDriver.addOutput(new Text("value[] = 2.0"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorStringConversion() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "out: table collection of string;\nx: int = input;\ny: bytes = input;\nif (true) {\n\temit out <- string(x, 16);\n}\nemit out <- string(y, \"UTF-8\");\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("StringConversion", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.StringConversion", src, null, new Class<?>[] { SizzleRunner.class })
					.newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("100"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("200"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("300"));
		mapReduceDriver.addOutput(new Text("out[] = 64"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[] = 100"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[] = c8"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[] = 200"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[] = 12c"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[] = 300"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorValueQuartiles() throws IOException, ParseException, ClassCastException, InstantiationException,
			IllegalAccessException, CharSequenceCompilerException, IOException {
		final String source = "quartiles: table quantile(5) of int;\ni: int = input;\nemit quartiles <- i;\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("ValueQuartiles", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.ValueQuartiles", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("6"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("47"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("49"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("15"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("42"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("41"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("7"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("39"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("43"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("40"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("36"));
		mapReduceDriver.addOutput(new Text("quartiles[] = [15, 40, 43, 49]"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Ignore
	@Test
	public void testCodeGeneratingVisitorBigUglyWhen() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "resultx: table sum of int;\na: array of int = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };\nb: array of int = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };\nwhen(i0, i1, j0, j1: some int; a[i0:i1] == b[j0:j1] &&\n\t\t\t\t\t\ti1 >= i0+3) {\nemit resultx <- 1;\n}\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("BigUglyWhen", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);
		System.err.println(src);
		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.BigUglyWhen", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("test"));
		mapReduceDriver.addOutput(new Text("result[] = 36"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorMaps() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "xlated: table collection of lang: string;\nstatic CJK: map[string] of string = {\n\t\"zh\": \"Chinese\",\n\t\"ja\": \"Japanese\",\n\t\"ko\": \"Korean\"\n};\nabbr: string = input;\nemit xlated <- CJK[abbr];\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("Maps", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.Maps", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("zh"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("ja"));
		mapReduceDriver.addInput(new LongWritable(0), new Text("ko"));
		mapReduceDriver.addOutput(new Text("xlated[] = Chinese"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("xlated[] = Japanese"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("xlated[] = Korean"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorUppercaseMap() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "upper: table collection of x: string;\ns: string = input;\nemit upper <- uppercase(s);";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("UppercaseMap", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.UppercaseMap", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("lowercase"));
		mapReduceDriver.addOutput(new Text("upper[] = LOWERCASE"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorTimeParser() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "times: table collection[timezone: string] of time: string;\nt: string = input;\nemit times[\"PST8PDT\"] <- string(trunctoday(time(t)));emit times[\"America/New_York\"] <- string(trunctoday(time(t), \"America/New_York\"));\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("TimeParser", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.TimeParser", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text("Sun Feb  2 23:40:08 PST 2003"));
		mapReduceDriver.addOutput(new Text("times[America/New_York] = Sun Feb 2 21:00:00 PST 2003"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("times[PST8PDT] = Sun Feb 2 00:00:00 PST 2003"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorTopReferers() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "best: table top(3)[url: string] of referer: string weight count: int;\nline: string = input;\nfields: array of string = saw(line, \".*GET \", \"[^\\t ]+\", \" HTTP/1.[0-9]\\\"\", \"[0-9]+\", \"[0-9]+\", \"\\\"[^\\t ]+\\\"\");\nemit best[fields[1]] <- fields[5] weight 1;\n";
		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("TopReferers", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.TopReferers", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver
				.addInput(
						new LongWritable(0),
						new Text(
								"127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326 \"http://www.example.com/start.html\" \"Mozilla/4.08 [en] (Win98; I ;Nav)\""));
		mapReduceDriver.addOutput(new Text("best[/apache_pb.gif] = \"http://www.example.com/start.html\", 1, 0"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorRegex() throws IOException, ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "out: table collection[name: string] of output: string;\nemit out[\"int,16\"] <- regex(int, 16);\nemit out[\"int,10\"] <- regex(int, 10);\nemit out[\"int,8\"] <- regex(int, 8);\nemit out[\"float\"] <- regex(float);\nemit out[\"time\"] <- regex(time);\nemit out[\"string\"] <- regex(string);\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("Regex", TestCodeGeneratingVisitor.stg);
		SymbolTable st;
		try {
			st = new SymbolTable();
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}

		SizzleParser.ReInit(new StringReader(source));
		TestCodeGeneratingVisitor.typeChecker.visit(SizzleParser.Start(), st);
		SizzleParser.ReInit(new StringReader(source));
		final String src = codeGenerator.visit(SizzleParser.Start(), st);

		SizzleRunner sizzleRunner = null;
		try {
			sizzleRunner = TestCodeGeneratingVisitor.compiler.compile("sizzle.Regex", src, null, new Class<?>[] { SizzleRunner.class }).newInstance();
		} catch (final CharSequenceCompilerException e) {
			for (final Diagnostic<? extends JavaFileObject> d : e.getDiagnostics().getDiagnostics())
				System.err.println(d.toString());
			throw e;
		}

		final MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable> mapReduceDriver = new MapReduceDriver<LongWritable, Text, EmitKey, EmitValue, Text, NullWritable>();
		mapReduceDriver.setMapper(sizzleRunner.getMapper());
		// TODO: add the combiner when MAPREDUCE-797 is integrated
		mapReduceDriver.setReducer(sizzleRunner.getReducer());
		mapReduceDriver.addInput(new LongWritable(0), new Text(""));
		mapReduceDriver.addOutput(new Text("out[float] = [-+]?[0-9]*\\.?[0-9]+(e[-+]?[0-9]+)?"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[int,10] = [+-]?[0-9]+"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[int,16] = (0x)?[A-Fa-f0-9]+h?"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[int,8] = 0[0-7]+"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[string] = \\S+"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("out[time] = [0-9]+"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	// do
	// d++;
	// while (d*d < x);

	// i++;
	// a[i]--;
	// f(x, y);

	// e := ?{
	// j := function(): int { result 1; }; # compile-time error
	// };

	// x := function(): int {
	// n := ?{
	// return 2; # compile-time error
	// };
	// };

	// while (d*d < x)
	// d++;

	// function() {}
	// function(radius: float): float { return 2.0 * PI * radius; }
	// Action { kill_bill(); }

	// # signum
	// signum := ?{ switch(true) { case x < 0: result -1; case x > 0: result 1;
	// default: result 0; };
	//
	// # array filtering
	// a = ?{
	// aa: array of int = {};
	// when (i: each int; Test(a[i])) aa = aa + {a[i]};
	// result aa;
	// };

	// 1 # = 1
	// 011 # = 9
	// 0b1010 # = 10
	// 42 # = 42
	// 0x7f # = 127

	// 1u # = 1
	// 011u # = 9
	// 0b1010U # = 10
	// 42u # = 42
	// 0x7fU # = 127
	// 18446744073709551616U # = largest possible 64-bit value

	// 0p # the fingerprint with value 0
	// 0x6347P # the fingerprint with value 25415

	// .01 # = 0.01
	// 1e-3 # = 0.001
	// 2. # = 2.0
	// 3.1415 # = 3.1415
	// 2.18E5 # = 218000.0

	// '\n' # = 10
	// 'A' # = 65

	// "" # empty string
	// "Sawzall\n" # Sawzall followed by a newline character
	// `"Hello"` # "Hello" (including the "'s)

	// B"" # empty bytes literal
	// B"hello" # five bytes
	// B`"` # one byte, a double quote
	// B"\xff" # one byte, value 255
	// X"0011AB" # three bytes: 0x00 0x11 0xAB

	// 0t # 0 microseconds
	// 1000000T # 1 second
	// T"Tue Jun  5 10:43:07 America/Los_Angeles 2007"
	// T"Wed Feb  4 16:26:41 PST 2004"

}
