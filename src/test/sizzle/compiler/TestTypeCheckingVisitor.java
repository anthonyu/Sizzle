package sizzle.compiler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import sizzle.parser.ParseException;
import sizzle.parser.SizzleParser;
import sizzle.types.SizzleArray;
import sizzle.types.SizzleBytes;
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

public class TestTypeCheckingVisitor {
	public static TypeCheckingVisitor typeChecker;

	@BeforeClass
	public static void init() {
		TestTypeCheckingVisitor.typeChecker = new TypeCheckingVisitor(new NameFindingVisitor());
		// this is a JavaCC wtf, do not remove
		try {
			new SizzleParser(new StringReader(""));
		} catch (final Error e) {
			// eat it
		}
	}

	@Test
	public void testTypeCheckingVisitorSimple() throws IOException, ParseException {
		final String source = "count: table sum of int;\ntotal: table sum of float;\nsum_of_squares: table sum of float;\nx: float = input;\nemit count <- 1;\nemit total <- x;\nemit sum_of_squares <- x * x;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("count is not an unweighted, unindexed table of ints",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt()))), st.get("count"));
		Assert.assertEquals("total is not an unweighted, unindexed stable of floats",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleFloat()))), st.get("total"));
		Assert.assertEquals("sum_of_squares is not an unweighted, unindexed table of floats",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleFloat()))), st.get("sum_of_squares"));
		Assert.assertEquals("x is not a float", new SizzleFloat(), st.get("x"));
	}

	@Test
	public void testTypeCheckingVisitorSimpleCompound() throws IOException, ParseException {
		final String source = "s: table sum of { count: int, total: float, sum_of_squares: float };\nx: float = input;\nemit s <- 1, x, x * x;";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals(
				"s is not an unweighted, unindexed table of int, float and float",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt(), new SizzleFloat(), new SizzleFloat())), Arrays.asList("count",
						"total", "sum_of_squares")), st.get("s"));

		Assert.assertEquals("x is not a float", new SizzleFloat(), st.get("x"));
	}

	@Test
	public void testTypeCheckingVisitorP4Stat() throws IOException, ParseException {
		final String source = "proto \"p4stat.proto\"\nsubmitsthroughweek: table sum[minute: int] of count: int;\nlog: P4ChangelistStats = input;\nt: time = log.time; # microseconds\nminute: int = minuteof(t)+60*(hourof(t)+24*(dayofweek(t)-1));\nemit submitsthroughweek[minute] <- 1;\n";

		final SymbolTable st = new SymbolTable(new SizzleBytes());

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals(
				"submitsthroughweek is not an unweighted table of ints indexed by int",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt())), null, Arrays.asList(new SizzleScalar[] { new SizzleInt() }), null),
				st.get("submitsthroughweek"));
		final HashMap<String, SizzleType> members = new HashMap<String, SizzleType>();
		members.put("time", new SizzleInt());
		Assert.assertEquals("log is not a P4ChangelistStats", new SizzleTuple("P4ChangelistStats", members), st.get("log"));
		Assert.assertEquals("t is not a time", new SizzleTime(), st.get("t"));
		Assert.assertEquals("minute is not an int", new SizzleInt(), st.get("minute"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorMaxPagerankMissingProto() throws IOException, ParseException {
		final String source = "proto \"document.proto\"\nmax_pagerank_url:\n     table maximum(1) [domain: string] of url: string\n           weight pagerank: int;\ndoc: Document = input;\nemit max_pagerank_url[domain(doc.url)] <- doc.url\n     weight doc.pagerank;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test
	public void testTypeCheckingVisitorMaxPagerank() throws IOException, ParseException {
		final String source = "proto \"sizzle_document.proto\"\nmax_pagerank_url:\n     table maximum(1) [domain: string] of url: string\n           weight pagerank: float;\ndoc: Document = input;\nemit max_pagerank_url[domain(doc.url)] <- doc.url\n     weight doc.pagerank;\n";

		final SymbolTable st = new SymbolTable(new SizzleBytes());

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("max_pagerank_url is not table of strings indexed by string weighted by int",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleString())), null, Arrays.asList(new SizzleScalar[] { new SizzleString() }),
						new SizzleFloat()), st.get("max_pagerank_url"));
		final HashMap<String, SizzleType> members = new HashMap<String, SizzleType>();
		members.put("url", new SizzleString());
		members.put("pagerank", new SizzleInt());
		Assert.assertEquals("doc is not a Document", new SizzleTuple("Document", members), st.get("doc"));
	}

	@Test
	public void testTypeCheckingVisitorQueryLog() throws IOException, ParseException {
		final String source = "proto \"querylog.proto\"\n\nqueries_per_degree: table sum[lat: int][lon: int] of int;\n\nlog_record: QueryLogProto = input;\nloc: Location = locationinfo(log_record.ip);\nemit queries_per_degree[int(loc.lat)][int(loc.lon)] <- 1;\n";

		final SymbolTable st = new SymbolTable(new SizzleBytes());

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals(
				"queries_per_degree is not an unweighted table of ints indexed by ints",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt())), null, Arrays.asList(new SizzleScalar[] { new SizzleInt(),
						new SizzleInt() }), null), st.get("queries_per_degree"));

		final HashMap<String, SizzleType> lmembers = new HashMap<String, SizzleType>();
		lmembers.put("lon", new SizzleFloat());
		lmembers.put("lat", new SizzleFloat());
		Assert.assertEquals("loc is not a Location", new SizzleTuple("Location", lmembers), st.get("loc"));

		final HashMap<String, SizzleType> qlpmembers = new HashMap<String, SizzleType>();
		qlpmembers.put("time_usec", new SizzleInt());
		qlpmembers.put("ip", new SizzleString());
		Assert.assertEquals("log_record is not a QueryLogProto", new SizzleTuple("QueryLogProto", qlpmembers), st.get("log_record"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingRobustQueryLogBad() throws IOException, ParseException {
		// this is an example from the Sawzall paper, doesn't define MINUTE
		final String source = "proto \"querylog.proto\"\n	static RESOLUTION: int = 5;  # minutes; must be divisor of 60\n	log_record: QueryLogProto = input;\n	queries_per_degree: table sum[t: time][lat: int][lon: int] of int;\n	loc: Location = locationinfo(log_record.ip);\n	if (def(loc)) {\n	    t: time = log_record.time_usec;\n	    m: int = minuteof(t); # within the hour\n	    m = m - m % RESOLUTION;\n	    t = trunctohour(t) + time(m * int(MINUTE));\n	    emit queries_per_degree[t][int(loc.lat)][int(loc.lon)] <- 1;\n	}";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test
	public void testTypeCheckingRobustQueryLogFixed() throws IOException, ParseException {
		final String source = "proto \"querylog.proto\"\n	static MINUTE: float = 0.0; static RESOLUTION: int = 5;  # minutes; must be divisor of 60\n	log_record: QueryLogProto = input;\n	queries_per_degree: table sum[t: time][lat: int][lon: int] of int;\n	loc: Location = locationinfo(log_record.ip);\n	if (def(loc)) {\n	    t: time = log_record.time_usec;\n	    m: int = minuteof(t); # within the hour\n	    m = m - m % RESOLUTION;\n	    t = trunctohour(t) + time(m * int(MINUTE));\n	    emit queries_per_degree[t][int(loc.lat)][int(loc.lon)] <- 1;\n	}";

		final SymbolTable st = new SymbolTable(new SizzleBytes());

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals(
				"queries_per_degree is not an unweighted table of ints indexed by various",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt())), null, Arrays.asList(new SizzleTime(), new SizzleInt(),
						new SizzleInt()), null), st.get("queries_per_degree"));

		// assertEquals("t is not an time", new SizzleTime(), st.get("t"));
		// assertEquals("m is not an int", new SizzleInt(), st.get("m"));
		Assert.assertEquals("RESOLUTION is not an int", new SizzleInt(), st.get("RESOLUTION"));

		final HashMap<String, SizzleType> lmembers = new HashMap<String, SizzleType>();
		lmembers.put("lon", new SizzleFloat());
		lmembers.put("lat", new SizzleFloat());
		Assert.assertEquals("loc is not a Location", new SizzleTuple("Location", lmembers), st.get("loc"));

		final HashMap<String, SizzleType> qlpmembers = new HashMap<String, SizzleType>();
		qlpmembers.put("time_usec", new SizzleInt());
		qlpmembers.put("ip", new SizzleString());
		Assert.assertEquals("log_record is not a QueryLogProto", new SizzleTuple("QueryLogProto", qlpmembers), st.get("log_record"));
	}

	@Test
	public void testTypeCheckingVisitorWordCount() throws IOException, ParseException {
		final String source = "result: table sum[key: string][month: int][day: int] of int;\nstatic keywords: array of string = { \"hitchhiker\", \"benedict\", \"vytorin\", \"itanium\", \"aardvark\" };\nquerywords: array of string = words_from_query();\nmonth: int = month_of_query();\nday: int = day_of_query();\nwhen (i: each int; j: some int; querywords[i] == keywords[j])\n    emit result[keywords[j]][month][day] <- 1;\n";

		final SymbolTable st = new SymbolTable();

		// fake functions for this unit test
		st.setFunction("day_of_query", new SizzleFunction("Nonexistant.day_of_query", new SizzleInt(), new SizzleType[] {}));
		st.setFunction("month_of_query", new SizzleFunction("Nonexistant.month_of_query", new SizzleInt(), new SizzleType[] {}));
		st.setFunction("words_from_query", new SizzleFunction("Nonexistant.words_from_query", new SizzleArray(new SizzleString()), new SizzleType[] {}));

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals(
				"result is not an unweighted table of ints indexed by various",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt())), null, Arrays.asList(new SizzleString(), new SizzleInt(),
						new SizzleInt()), null), st.get("result"));

		Assert.assertEquals("keywords is not an array of strings", new SizzleArray(new SizzleString()), st.get("keywords"));
		Assert.assertEquals("querywords is not an array of strings", new SizzleArray(new SizzleString()), st.get("querywords"));

		Assert.assertEquals("month is not an int", new SizzleInt(), st.get("month"));
		Assert.assertEquals("day is not an int", new SizzleInt(), st.get("day"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorWordCountMissingFunctions() throws IOException, ParseException {
		final String source = "result: table sum[key: string][month: int][day: int] of int;\nstatic keywords: array of string = { \"hitchhiker\", \"benedict\", \"vytorin\", \"itanium\", \"aardvark\" };\nquerywords: array of string = words_from_query();\nmonth: int = month_of_query();\nday: int = day_of_query();\nwhen (i: each int; j: some int; querywords[i] == keywords[j])\n    emit result[keywords[j]][month][day] <- 1;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorWordCountMissingEmitIndex() throws IOException, ParseException {
		final String source = "result: table sum[key: string][month: int][day: int] of int;\nstatic keywords: array of string = { \"hitchhiker\", \"benedict\", \"vytorin\", \"itanium\", \"aardvark\" };\nquerywords: array of string = words_from_query();\nmonth: int = month_of_query();\nday: int = day_of_query();\nwhen (i: each int; j: some int; querywords[i] == keywords[j])\n    emit result <- 1;\n";

		final SymbolTable st = new SymbolTable();

		// fake functions for this unit test
		st.setFunction("day_of_query", new SizzleFunction("Nonexistant.day_of_query", new SizzleInt(), new SizzleType[] {}));
		st.setFunction("month_of_query", new SizzleFunction("Nonexistant.month_of_query", new SizzleInt(), new SizzleType[] {}));
		st.setFunction("words_from_query", new SizzleFunction("Nonexistant.words_from_query", new SizzleArray(new SizzleString()), new SizzleType[] {}));

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorRealWordCountUnsupportedParameter() throws IOException, ParseException {
		// sum doesn't take a parameter
		final String source = "count: table sum(10)[word: string] of int;\nline: string = input;\nwords: array of string = split(line, \"[^A-Za-z0-9]\");\ni: int;\nfor (i = 0; i < length(words); i++)\n\temit count[words[i]] <- 1;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test
	public void testTypeCheckingVisitorCompound() throws IOException, ParseException {
		final String source = "result: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = sawzall(line, \"[^\\t]+\");\nemit result <- 1, float(tuple[8]);\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("result is not a compound, unindexed, unweighted table of int and float",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt(), new SizzleFloat())), Arrays.asList("count", "value")),
				st.get("result"));

		Assert.assertEquals("line is not a string", new SizzleString(), st.get("line"));
		Assert.assertEquals("tuple is not an array of strings", new SizzleArray(new SizzleString()), st.get("tuple"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorCompoundNonCast() throws IOException, ParseException {
		final String source = "result: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = split(line, \"\\t\");\nemit result <- 1, tuple[8];\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("result is not a compound, unindexed, unweighted table of int and float",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt(), new SizzleFloat())), Arrays.asList("count", "value")),
				st.get("result"));

		Assert.assertEquals("line is not a string", new SizzleString(), st.get("line"));
		Assert.assertEquals("value is not a float", new SizzleFloat(), st.get("value"));
		Assert.assertEquals("tuple is not an array of strings", new SizzleArray(new SizzleString()), st.get("tuple"));
	}

	@Test
	public void testTypeCheckingVisitorCompoundImplicitCast() throws IOException, ParseException {
		final String source = "result: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = sawzall(line, \"[^\\t]+\");\nvalue: float = tuple[8];\nemit result <- 1, value;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("result is not a compound, unindexed, unweighted table of int and float",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleInt(), new SizzleFloat())), Arrays.asList("count", "value")),
				st.get("result"));

		Assert.assertEquals("line is not a string", new SizzleString(), st.get("line"));
		Assert.assertEquals("tuple is not an array of strings", new SizzleArray(new SizzleString()), st.get("tuple"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorNonCompoundEmit() throws IOException, ParseException {
		final String source = "result: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = split(line, \"\\t\");\nemit result <- float(tuple[8]);\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorCompoundSwitched() throws IOException, ParseException {
		final String source = "result: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = split(line, \"\\t\");\nemit result <- float(tuple[8]), 1;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorWrongWeightTypeInDecl() throws IOException, ParseException {
		final String source = "logger: table log of entry: string weight level: int;\nline: string = input;\nemit logger <- line weight 1;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorWrongWeightTypeInEmit() throws IOException, ParseException {
		final String source = "logger: table log of entry: string weight level: int;\nline: string = input;\nemit logger <- line weight 1.2;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorMissingWeightInDecl() throws IOException, ParseException {
		final String source = "logger: table log of entry: string;\nline: string = input;\nemit logger <- line;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorMissingWeightInEmit() throws IOException, ParseException {
		final String source = "logger: table log of entry: string weight level: int;\nline: string = input;\nemit logger <- line;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorUnexpectedWeightInDecl() throws IOException, ParseException {
		final String source = "allez: table collection of entry: string weight level: int;\nline: string = input;\nemit allez <- line weight 1;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorUnexpectedWeightInEmit() throws IOException, ParseException {
		final String source = "allez: table collection of entry: string;\nline: string = input;\nemit allez <- line weight 1.2;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test
	public void testTypeCheckingVisitorMap() throws IOException, ParseException {
		final String source = "xlated: table collection of lang: string;\nstatic CJK: map[string] of string = {\n\t\"zh\": \"Chinese\",\n\t\"ja\": \"Japanese\",\n\"ko\": \"Korean\"\n};\nabbr: string = input;\nemit xlated <- CJK[abbr];\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("xlated is not an unindexed, unweighted table of string",
				new SizzleTable(new ArrayList<SizzleScalar>(Arrays.asList(new SizzleString())), null), st.get("xlated"));

		Assert.assertEquals("abbr is not a string", new SizzleString(), st.get("abbr"));
		Assert.assertEquals("CJK is not a mapping from string to string", new SizzleMap(new SizzleString(), new SizzleString()), st.get("CJK"));
	}

}
