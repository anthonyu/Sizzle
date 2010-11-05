package sizzle.compiler;

import java.io.StringReader;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import sizzle.parser.ParseException;
import sizzle.parser.SizzleParser;

public class TestIndexeeFindingVisitor {
	private static IndexeeFindingVisitor indexeeFinder;

	@BeforeClass
	public static void init() {
		TestIndexeeFindingVisitor.indexeeFinder = new IndexeeFindingVisitor(new NameFindingVisitor());
		// this is a JavaCC wtf, do not remove
		try {
			new SizzleParser(new StringReader(""));
		} catch (final Error e) {
			// eat it
		}
	}

	@Test
	public void testSizzleIndexeeFindingVisitor() throws ParseException {
		final String source = "result: table sum[key: string][month: int][day: int] of int;\nstatic keywords: array of string = { \"hitchhiker\", \"benedict\", \"vytorin\", \"itanium\", \"aardvark\" };\ntuple: array of string = split(input, \"\");\nquerywords: array of string = keywords(tuple[5]);\nts: time = tuple[2];\nmonth: int = monthofyear(ts);\nday: int = dayofweek(ts);\nwhen (i: each int; j: some int; querywords[i] == keywords[j])\n    emit result[keywords[j]][month][day] <- 1;\n";

		SizzleParser.ReInit(new StringReader(source));
		final Set<String> indexees1 = TestIndexeeFindingVisitor.indexeeFinder.visit(SizzleParser.Start(), "i");

		Assert.assertEquals("indexees1 is the wrong size", 1, indexees1.size());
		Assert.assertTrue("indexees1 is wrong", indexees1.contains("querywords"));

		SizzleParser.ReInit(new StringReader(source));
		final Set<String> indexees2 = TestIndexeeFindingVisitor.indexeeFinder.visit(SizzleParser.Start(), "j");

		Assert.assertEquals("indexees2 is the wrong size", 1, indexees2.size());
		Assert.assertTrue("indexees2 is wrong", indexees2.contains("keywords"));
	}
}
