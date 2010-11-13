package sizzle.compiler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import javaxtools.compiler.CharSequenceCompiler;
import javaxtools.compiler.CharSequenceCompilerException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;
import sizzle.parser.ParseException;
import sizzle.parser.SizzleParser;
import sizzle.runtime.SizzleMapper;
import sizzle.runtime.SizzleRunner;
import sizzle.types.SizzleBytes;

@SuppressWarnings("unused")
public class TestCodeGeneratingVisitor {
	private static TypeCheckingVisitor typeChecker;
	private static CharSequenceCompiler<SizzleRunner> compiler;

	@BeforeClass
	public static void init() {
		TestCodeGeneratingVisitor.typeChecker = new TypeCheckingVisitor(new NameFindingVisitor());
		// this is a JavaCC wtf, do not remove
		try {
			new SizzleParser(new StringReader(""));
		} catch (final Error e) {
			// eat it
		}

		TestCodeGeneratingVisitor.compiler = new CharSequenceCompiler<SizzleRunner>(null, null);
	}

	@Test
	public void testCodeGeneratingVisitorSimple() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException,
			CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "count: table sum of int;\ntotal: table sum of float;\nsum_of_squares: table sum of float;\nx: float = input;\nemit count <- 1;\nemit total <- x;\nemit sum_of_squares <- x * x;\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("Simple");
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
	public void testCodeGeneratingVisitorSimpleCompound() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException,
			CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "s: table sum of { count: int, total: float, sum_of_squares: float };\nx: float = input;\nemit s <- 1, x, x * x;\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("SimpleCompound");
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
		mapReduceDriver.addOutput(new Text("s.count[] = 3"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("s.sum_of_squares[] = 14.0"), NullWritable.get());
		mapReduceDriver.addOutput(new Text("s.total[] = 6.0"), NullWritable.get());
		mapReduceDriver.runTest();
	}

	@Test
	public void testCodeGeneratingVisitorQuickExample() throws ParseException, ClassCastException, InstantiationException, IllegalAccessException,
			CharSequenceCompilerException {
		final String source = "topwords: table top(3) of word: string weight count: int;\nfields: array of bytes = splitcsvline(input);\nw: string = string(fields[0]);\nc: int = int(string(fields[1]), 10);\nif (c != 0) {\nemit topwords <- w weight c;\n}\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("QuickExample");
		SymbolTable st;
		try {
			st = new SymbolTable(new SizzleBytes());
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
	public void testCodeGeneratingVisitorRealWordCount() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "count: table sum[word: string] of int;\nline: string = input;\nwords: array of string = sawzall(line, \"[A-Za-z]+\");\ni: int;\nfor (i = 0; i < len(words); i++)\n\temit count[lowercase(words[i])] <- 1;\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("RealWordCount");
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
	public void testCodeGeneratingVisitorWordStats() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "scrabble: table maximum(5) of string weight score: int;\nlongest: table maximum(5) of string weight length: int;\nline: string = input;\nwords: array of string = sawzall(line, \"[A-Za-z]+\");\ni: int;\nfor (i = 0; i < len(words); i++) {\n\temit scrabble <- lowercase(words[i]) weight scrabble(words[i]);\n\temit longest <- lowercase(words[i]) weight len(words[i]);\n}\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("WordStats");
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
	public void testCodeGeneratingVisitorFilteredMeanValue() throws ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "value: table mean of float;\nf: float = input;\nif (f != 0.0) {\n\temit value <- f;\n}\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("FilteredMeanValue");

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
	public void testCodeGeneratingVisitorStringConversion() throws ParseException, CharSequenceCompilerException, InstantiationException,
			IllegalAccessException {
		final String source = "out: table collection of string;\nx: int = input;\ny: bytes = input;\nif (true) {\n\temit out <- string(x, 16);\n}\nemit out <- string(y, \"UTF-8\");\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("StringConversion");
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
	public void testCodeGeneratingVisitorValueQuartiles() throws ParseException, ClassCastException, InstantiationException, IllegalAccessException,
			CharSequenceCompilerException, IOException {
		final String source = "quartiles: table quantile(5) of int;\ni: int = input;\nemit quartiles <- i;\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("ValueQuartiles");
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

	@Test
	public void testCodeGeneratingVisitorBigUglyWhen() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "result: table sum of int;\na: array of int = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };\nb: array of int = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };\nwhen(i0, i1, j0, j1: some int; a[i0:i1] == b[j0:j1] &&\n\t\t\t\t\t\ti1 >= i0+3) {\nemit result <- 1;\n}\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("BigUglyWhen");
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
	public void testCodeGeneratingVisitorMaps() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "xlated: table collection of lang: string;\nstatic CJK: map[string] of string = {\n\t\"zh\": \"Chinese\",\n\t\"ja\": \"Japanese\",\n\t\"ko\": \"Korean\"\n};\nabbr: string = input;\nemit xlated <- CJK[abbr];\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("Maps");
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
	public void testCodeGeneratingVisitorTimeParser() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "times: table collection[timezone: string] of time: string;\nt: string = input;\nemit times[\"PST8PDT\"] <- string(trunctoday(time(t)));emit times[\"America/New_York\"] <- string(trunctoday(time(t), \"America/New_York\"));\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("TimeParser");
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
	public void testCodeGeneratingVisitorTopReferers() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "best: table top(3)[url: string] of referer: string weight count: int;\nline: string = input;\nfields: array of string = saw(line, \".*GET \", \"[^\\t ]+\", \" HTTP/1.[0-9]\\\"\", \"[0-9]+\", \"[0-9]+\", \"\\\"[^\\t ]+\\\"\");\nemit best[fields[1]] <- fields[5] weight 1;\n";
		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("TopReferers");
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
	public void testCodeGeneratingVisitorRegex() throws ParseException, CharSequenceCompilerException, InstantiationException, IllegalAccessException {
		final String source = "out: table collection[name: string] of output: string;\nemit out[\"int,16\"] <- regex(int, 16);\nemit out[\"int,10\"] <- regex(int, 10);\nemit out[\"int,8\"] <- regex(int, 8);\nemit out[\"float\"] <- regex(float);\nemit out[\"time\"] <- regex(time);\nemit out[\"string\"] <- regex(string);\n";

		final CodeGeneratingVisitor codeGenerator = new CodeGeneratingVisitor("Regex");
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
}
