package sizzle.compiler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import sizzle.parser.ParseException;
import sizzle.parser.SizzleParser;
import sizzle.types.SizzleArray;
import sizzle.types.SizzleBool;
import sizzle.types.SizzleBytes;
import sizzle.types.SizzleFloat;
import sizzle.types.SizzleFunction;
import sizzle.types.SizzleInt;
import sizzle.types.SizzleMap;
import sizzle.types.SizzleName;
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

		Assert.assertEquals("count is not an unweighted, unindexed table of ints", new SizzleTable(new SizzleInt()), st.get("count"));
		Assert.assertEquals("total is not an unweighted, unindexed stable of floats", new SizzleTable(new SizzleFloat()), st.get("total"));
		Assert.assertEquals("sum_of_squares is not an unweighted, unindexed table of floats", new SizzleTable(new SizzleFloat()), st.get("sum_of_squares"));
		Assert.assertEquals("x is not a float", new SizzleFloat(), st.get("x"));
	}

	@Test
	public void testTypeCheckingVisitorSimpleCompound() throws IOException, ParseException {
		final String source = "s: table sum of { count: int, total: float, sum_of_squares: float };\nx: float = input;\nemit s <- { 1, x, x * x };";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		final List<SizzleType> members = new ArrayList<SizzleType>(Arrays.asList(new SizzleInt(), new SizzleFloat(), new SizzleFloat()));

		Assert.assertEquals("s is not an unweighted, unindexed table of tuple of int, float and float", new SizzleTable(new SizzleTuple(members)), st.get("s"));

		Assert.assertEquals("x is not a float", new SizzleFloat(), st.get("x"));
	}

	@Test
	public void testTypeCheckingVisitorP4Stat() throws IOException, ParseException {
		final String source = "proto \"p4stat.proto\"\nsubmitsthroughweek: table sum[minute: int] of count: int;\nlog: P4ChangelistStats = input;\nt: time = log.time; # microseconds\nminute: int = minuteof(t)+60*(hourof(t)+24*(dayofweek(t)-1));\nemit submitsthroughweek[minute] <- 1;\n";

		final SymbolTable st = new SymbolTable(new SizzleBytes());

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("submitsthroughweek is not an unweighted table of ints indexed by int",
				new SizzleTable(new SizzleInt(), Arrays.asList(new SizzleScalar[] { new SizzleInt() }), null), st.get("submitsthroughweek"));
		final List<SizzleType> members = new ArrayList<SizzleType>(Arrays.asList(new SizzleInt()));
		Assert.assertEquals("log is not a P4ChangelistStats", new SizzleTuple(members), st.get("log"));
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
				new SizzleTable(new SizzleString(), Arrays.asList(new SizzleScalar[] { new SizzleString() }), new SizzleFloat()), st.get("max_pagerank_url"));
		final List<SizzleType> members = new ArrayList<SizzleType>(Arrays.asList(new SizzleString(), new SizzleInt()));
		Assert.assertEquals("doc is not a Document", new SizzleTuple(members), st.get("doc"));
	}

	@Test
	public void testTypeCheckingVisitorQueryLog() throws IOException, ParseException {
		final String source = "proto \"querylog.proto\"\n\nqueries_per_degree: table sum[lat: int][lon: int] of int;\n\nlog_record: QueryLogProto = input;\nloc: Location = locationinfo(log_record.ip);\nemit queries_per_degree[int(loc.lat)][int(loc.lon)] <- 1;\n";

		final SymbolTable st = new SymbolTable(new SizzleBytes());

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("queries_per_degree is not an unweighted table of ints indexed by ints",
				new SizzleTable(new SizzleInt(), Arrays.asList(new SizzleScalar[] { new SizzleInt(), new SizzleInt() }), null), st.get("queries_per_degree"));

		final List<SizzleType> lmembers = new ArrayList<SizzleType>(Arrays.asList(new SizzleFloat(), new SizzleFloat()));

		Assert.assertEquals("loc is not a Location", new SizzleTuple(lmembers), st.get("loc"));

		final List<SizzleType> qlpmembers = new ArrayList<SizzleType>(Arrays.asList(new SizzleString(), new SizzleInt()));

		Assert.assertEquals("log_record is not a QueryLogProto", new SizzleTuple(qlpmembers), st.get("log_record"));
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

		Assert.assertEquals("queries_per_degree is not an unweighted table of ints indexed by various",
				new SizzleTable(new SizzleInt(), Arrays.asList(new SizzleTime(), new SizzleInt(), new SizzleInt()), null), st.get("queries_per_degree"));

		// assertEquals("t is not an time", new SizzleTime(), st.get("t"));
		// assertEquals("m is not an int", new SizzleInt(), st.get("m"));
		Assert.assertEquals("RESOLUTION is not an int", new SizzleInt(), st.get("RESOLUTION"));

		final List<SizzleType> lmembers = new ArrayList<SizzleType>(Arrays.asList(new SizzleFloat(), new SizzleFloat()));

		Assert.assertEquals("loc is not a Location", new SizzleTuple(lmembers), st.get("loc"));

		final List<SizzleType> qlpmembers = new ArrayList<SizzleType>(Arrays.asList(new SizzleString(), new SizzleInt()));

		Assert.assertEquals("log_record is not a QueryLogProto", new SizzleTuple(qlpmembers), st.get("log_record"));
	}

	@Test
	public void testTypeCheckingVisitorWordCount() throws IOException, ParseException {
		final String source = "out: table sum[key: string][month: int][day: int] of int;\nstatic keywords: array of string = { \"hitchhiker\", \"benedict\", \"vytorin\", \"itanium\", \"aardvark\" };\nquerywords: array of string = words_from_query();\nmonth: int = month_of_query();\nday: int = day_of_query();\nwhen (i: each int; j: some int; querywords[i] == keywords[j])\n    emit out[keywords[j]][month][day] <- 1;\n";

		final SymbolTable st = new SymbolTable();

		// fake functions for this unit test
		st.setFunction("day_of_query", new SizzleFunction("Nonexistant.day_of_query", new SizzleInt(), new SizzleType[] {}));
		st.setFunction("month_of_query", new SizzleFunction("Nonexistant.month_of_query", new SizzleInt(), new SizzleType[] {}));
		st.setFunction("words_from_query", new SizzleFunction("Nonexistant.words_from_query", new SizzleArray(new SizzleString()), new SizzleType[] {}));

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("out is not an unweighted table of ints indexed by various",
				new SizzleTable(new SizzleInt(), Arrays.asList(new SizzleString(), new SizzleInt(), new SizzleInt()), null), st.get("out"));

		Assert.assertEquals("keywords is not an array of strings", new SizzleArray(new SizzleString()), st.get("keywords"));
		Assert.assertEquals("querywords is not an array of strings", new SizzleArray(new SizzleString()), st.get("querywords"));

		Assert.assertEquals("month is not an int", new SizzleInt(), st.get("month"));
		Assert.assertEquals("day is not an int", new SizzleInt(), st.get("day"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorWordCountMissingFunctions() throws IOException, ParseException {
		final String source = "out: table sum[key: string][month: int][day: int] of int;\nstatic keywords: array of string = { \"hitchhiker\", \"benedict\", \"vytorin\", \"itanium\", \"aardvark\" };\nquerywords: array of string = words_from_query();\nmonth: int = month_of_query();\nday: int = day_of_query();\nwhen (i: each int; j: some int; querywords[i] == keywords[j])\n    emit out[keywords[j]][month][day] <- 1;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorWordCountMissingEmitIndex() throws IOException, ParseException {
		final String source = "out: table sum[key: string][month: int][day: int] of int;\nstatic keywords: array of string = { \"hitchhiker\", \"benedict\", \"vytorin\", \"itanium\", \"aardvark\" };\nquerywords: array of string = words_from_query();\nmonth: int = month_of_query();\nday: int = day_of_query();\nwhen (i: each int; j: some int; querywords[i] == keywords[j])\n    emit out <- 1;\n";

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
		final String source = "out: table sum(10) of { count: int, value: float };\nline: string = input;\ntuple: array of string = sawzall(line, \"[^\\t]+\");\nemit out <- { 1, float(tuple[8]) };\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test
	public void testTypeCheckingVisitorCompound() throws IOException, ParseException {
		final String source = "out: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = sawzall(line, \"[^\\t]+\");\nemit out <- { 1, float(tuple[8]) };\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		final List<SizzleType> members = new ArrayList<SizzleType>(Arrays.asList(new SizzleInt(), new SizzleFloat()));

		Assert.assertEquals("out is not a unindexed, unweighted table of tuple of int and float", new SizzleTable(new SizzleTuple(members)), st.get("out"));

		Assert.assertEquals("line is not a string", new SizzleString(), st.get("line"));
		Assert.assertEquals("tuple is not an array of strings", new SizzleArray(new SizzleString()), st.get("tuple"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorCompoundNonCast() throws IOException, ParseException {
		final String source = "out: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = split(line, \"\\t\");\nemit out <- { 1, tuple[8] };\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test
	public void testTypeCheckingVisitorCompoundImplicitCast() throws IOException, ParseException {
		final String source = "out: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = sawzall(line, \"[^\\t]+\");\nvalue: float = tuple[8];\nemit out <- { 1, value };\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		final List<SizzleType> members = new ArrayList<SizzleType>(Arrays.asList(new SizzleInt(), new SizzleFloat()));

		Assert.assertEquals("out is not an unindexed, unweighted table of int and float", new SizzleTable(new SizzleTuple(members)), st.get("out"));

		Assert.assertEquals("line is not a string", new SizzleString(), st.get("line"));
		Assert.assertEquals("tuple is not an array of strings", new SizzleArray(new SizzleString()), st.get("tuple"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorNonCompoundEmit() throws IOException, ParseException {
		final String source = "out: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = split(line, \"\\t\");\nemit out <- float(tuple[8]);\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorCompoundSwitched() throws IOException, ParseException {
		final String source = "out: table sum of { count: int, value: float };\nline: string = input;\ntuple: array of string = split(line, \"\\t\");\nemit out <- { float(tuple[8]), 1 };\n";

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

		Assert.assertEquals("xlated is not an unindexed, unweighted table of string", new SizzleTable(new SizzleString(), null), st.get("xlated"));

		Assert.assertEquals("abbr is not a string", new SizzleString(), st.get("abbr"));
		Assert.assertEquals("CJK is not a mapping from string to string", new SizzleMap(new SizzleString(), new SizzleString()), st.get("CJK"));
	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorTypeDeclaredAsVariable() throws IOException, ParseException {
		final String source = "times: table collection[timezone: string] of time: string;\ntime: string = input;\nemit times[\"PST8PDT\"] <- string(trunctoday(time(time)));emit times[\"America/New_York\"] <- string(trunctoday(time(time), \"America/New_York\"));\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	@Test
	public void testTypeCheckingVisitorTypeDeclaration() throws IOException, ParseException {
		final String source = "type my_bool = bool;\ntype Coordinates = { x: float, y: float };\ntype CityMap = map [city_name: string] of Coordinates;\n";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);

		Assert.assertEquals("my_bool is not an alias for bool", new SizzleName(new SizzleBool()), st.getType("my_bool"));
		final ArrayList<SizzleType> members = new ArrayList<SizzleType>(Arrays.asList(new SizzleFloat(), new SizzleFloat()));
		Assert.assertEquals("Coordinates is not is not an alias for a tuple of x: float, y: float", new SizzleName(new SizzleTuple(members)),
				st.getType("Coordinates"));
		Assert.assertEquals("CityMap is not an alias for a mapping from string to tuple of x: float, y: float", new SizzleName(new SizzleMap(
				new SizzleString(), new SizzleName(new SizzleTuple(members)))), st.getType("CityMap"));

	}

	@Test(expected = TypeException.class)
	public void testTypeCheckingVisitorCompoundBadType() throws IOException, ParseException {
		final String source = "s: table sum of { count: int, total: float, sum_of_squares: string };\nx: float = input;\nemit s <- { 1, x, \"x\" };";

		final SymbolTable st = new SymbolTable();

		SizzleParser.ReInit(new StringReader(source));
		TestTypeCheckingVisitor.typeChecker.visit(SizzleParser.Start(), st);
	}

	// TODO: write tests for the following:
	//
	// n := 10;
	// counter: int = 0;
	// static pi := 3.14159265;
	// hypot := sqrt(x*x + y*y);
	// static word := load("/home/szluser/current/word");
	// a: array of float = { 2.4, PI, float("1.234") };
	// unique_language_values: table unique (10) of {language: string, value:
	// string};
	// average := function(list: array of float): float;
	// min := function(x: int, y: int): int { if (x < y) return x; return y; };
	// static country_codes: array of string = LoadCountries("countries.txt");

	// {}
	//
	// { x: float, y: float, int }
	//
	// # the Vector tuple type
	// { x: float, y: float,
	// static Magnitude := function(p: Vector): float {
	// return sqrt(p.x*p.x + p.y*p.y);
	// }
	// }
	//
	// { ip: int = 0xffffff00 @ 1,
	// value: bytes = bytes("britney") @ 2, # proto strings are Sawzall bytes
	// timestamp: time @ 5,
	// type Server = {
	// id: int,
	// location: string
	// },
	// static location1 := "ROB",
	// static location2 := "GRI",
	// static location3 := "BGI",
	// }
	//
	// parsedmessage {
	// g: array of TimeProtocol_G @ 1, # 11
	// debug: array of bytes @ 4: bytes # 34
	// }
	//
	// proto T # T must be a tuple type
	//
	// proto {
	// x: int,
	// y: float
	// }
	//
	// proto proto proto { # proto is idempotent
	// x: int,
	// t: {
	// s: bytes,
	// t: bytes
	// }
	// }
	//
	// parsedmessage { # this type is equivalent to the previous one
	// x: int @ 1,
	// t: parsedmessage {
	// s: bytes @ 1,
	// t: bytes @ 2
	// } @ 2
	// }

	// map [int] of bool
	// map [symbol: string] of int
	// map [point: {x: float, y: float}] of name: string

	// # Type of intrinsic output variable stdout
	// table collection of x: string file("/dev/stdout") format("%s\n", x)
	//
	// # Type used to collect all the values into a single stream
	// table collection of string;
	//
	// # Type used to count the number of times each value is seen
	// table sum[value: string] of count: int;
	//
	// # Type used to record the top 10 values for each category
	// table top(10)[category: string] of value: string weight count: int;
	//
	// # Type used to record the ten most expensive operations
	// table maximum(10)[category: string] of operatikon: string weight cost:
	// float;
	//
	// # Type used to count how many unique values there are, using an
	// (internal)
	// # sampled table of 10000 values to estimate the distribution
	// table unique(10000) of value: string;

	// function()
	// function(n: int): int
	// function(name: string, hint: Coordinates): Coordinates

	// {}
	// {:}
	// { 'a', 'b', 127 }
	// { 12, "hello", 3.14, {} }
	// { "foo" : 3, "bar" : 7 }

	// 2003 # an int
	// i + 1 # an int
	// 0b100 << i # an int
	// (x - a) * (x + a) # a float
	// {"the", "answer", "is", 42} # a 4-tuple {string, string, string, int}
	// {1.2, 2.3, 3.4, 4.5, 5.6} # a 5-tuple and also an array of float holding
	// 5 elements
	// a[i % $] # (i % len(a)).th element of array a
	// x.f # field f of tuple x
	// {1, 2, 3, 4, 5}[i] # i.th element of constructed array
	// (0 <= i) and (i < n) # true if i in [0, n)
	// min(x, y) # call of function min with arguments x and y
	// a[i:j] # the array (a[i], a[i+1], .. a[j-1])

}
