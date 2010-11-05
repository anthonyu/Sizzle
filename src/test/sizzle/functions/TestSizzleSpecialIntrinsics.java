package sizzle.functions;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class TestSizzleSpecialIntrinsics {
	@Test
	public void testSizzleSpecialIntrinsicsSaw() {
		final String[] result = SizzleSpecialIntrinsics.saw("abcdef", "...", "..", ".");

		final String[] expected = new String[] { "abc", "de", "f" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}

	@Test
	public void testSizzleSpecialIntrinsicsSaw2() {
		final String[] result = SizzleSpecialIntrinsics.saw("abcdef", "abc", "de", "g");

		final String[] expected = new String[] { "abc", "de" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}

	@Test
	public void testSizzleSpecialIntrinsicsSaw3D() {
		final String[] result = SizzleSpecialIntrinsics.saw("abcdef", "abc", "e", "f");

		final String[] expected = new String[] { "abc", "e", "f" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}

	@Test
	public void testSizzleSpecialIntrinsicsSaw4() {
		final String[] result = SizzleSpecialIntrinsics.saw("abcdef", "abc", "^e", "f");

		final String[] expected = new String[] { "abc" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}

	@Test
	public void testSizzleSpecialIntrinsicsSawzall() {
		final String[] result = SizzleSpecialIntrinsics.sawzall("1	2	3	4	5	6	7	8", "[^\t]+");

		final String[] expected = new String[] { "1", "2", "3", "4", "5", "6", "7", "8" };

		Assert.assertEquals("result is the wrong size", expected.length, result.length);
		Assert.assertTrue("result is not equal", Arrays.equals(expected, result));
	}
}
