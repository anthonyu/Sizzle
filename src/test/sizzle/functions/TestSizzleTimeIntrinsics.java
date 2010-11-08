package sizzle.functions;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import junit.framework.Assert;

public class TestSizzleTimeIntrinsics {
	@Test
	public void testSizzleTimeIntrinsicsAddDay() {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST8PDT"));
		calendar.set(2001, 1, 28, 0, 0, 0);
		final long t = calendar.getTimeInMillis() * 1000;

		Assert.assertEquals("addDay is wrong", "Thu Mar 1 00:00:00 2001", SizzleTimeIntrinsics.formatTime("%c", SizzleTimeIntrinsics.addDay(t, 1)));
	}

	@Test
	public void testSizzleTimeIntrinsicsDayOfMonth() {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST8PDT"));
		calendar.set(2001, 1, 28, 0, 0, 0);
		final long t = calendar.getTimeInMillis() * 1000;

		Assert.assertEquals("dayOfMonth is wrong", 28, SizzleTimeIntrinsics.dayOfMonth(t));
	}

	@Test
	public void testSizzleTimeIntrinsicsTruncateToDay() {
		Assert.assertEquals("truncateToDay is wrong", "Fri Feb 13 00:00:00 2009",
				SizzleTimeIntrinsics.formatTime("%c", SizzleTimeIntrinsics.truncToDay(1234567890000000l)));
	}

	@Test
	public void testSizzleTimeIntrinsicsFormatTime() {
		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("PST8PDT"));
		calendar.set(2001, 1, 28, 0, 0, 0);
		final long t = calendar.getTimeInMillis() * 1000;

		Assert.assertEquals("formatTime is wrong", "% That was Wed Feb 28 00:00:00 2001 in timezone PST %",
				SizzleTimeIntrinsics.formatTime("%% That was %c in timezone %Z %%", t));
	}
}
