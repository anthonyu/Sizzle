package sizzle.functions;

import java.text.ParseException;

import junit.framework.Assert;

import org.junit.Test;

public class TestSizzleCasts {
	@Test
	public void testSizzleCastsStringToTime() throws ParseException {
		Assert.assertEquals("stringToTime is wrong", 1044258008000000l, SizzleCasts.stringToTime("Sun Feb  2 23:40:08 PST 2003"));
	}
}
