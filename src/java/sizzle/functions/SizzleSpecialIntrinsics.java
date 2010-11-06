package sizzle.functions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Special Functions
 * 
 * These functions have special properties, such as variable types, variable
 * numbers of parameters, or parameters that are types rather than values. Some
 * of the syntax used to describe them, e.g. "...", default arguments and
 * overloading, is not part of the Sawzall language.
 * 
 * @author anthonyu
 * 
 */
public class SizzleSpecialIntrinsics {
	private static MessageDigest md;

	static {
		try {
			SizzleSpecialIntrinsics.md = MessageDigest.getInstance("SHA");
		} catch (final NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}
	}

	/**
	 * If <em>condition</em> is false, print the <em>message</em> to standard
	 * error, with the prefix assertion failed:, and exit. The message may be
	 * empty or absent altogether.
	 * 
	 * @param condition
	 *            The condition to be checked
	 * 
	 * @param message
	 *            A {@link String} containing the message to be printed upon
	 *            failure
	 * 
	 * @return True iff <em>condition</em> is true
	 */
	@FunctionSpec(name = "assert", formalParameters = { "bool", "string" })
	public void azzert(final boolean condition, final String message) {
		if (!condition)
			throw new RuntimeException("assertion failed: " + message);
	}

	/**
	 * If <em>condition</em> is false, print the <em>message</em> to standard
	 * error, with the prefix assertion failed:, and exit. The message may be
	 * empty or absent altogether.
	 * 
	 * @param condition
	 *            The condition to be checked
	 * 
	 * @return True iff <em>condition</em> is true
	 */
	@FunctionSpec(name = "assert", formalParameters = { "bool" })
	public void azzert(final boolean condition) {
		if (!condition)
			throw new RuntimeException("assertion failed");
	}

	private static byte[] longToByteArray(final long l) {
		return new byte[] { (byte) (l >> 56 & 0xff), (byte) (l >> 48 & 0xff), (byte) (l >> 40 & 0xff), (byte) (l >> 32 & 0xff), (byte) (l >> 24 & 0xff),
				(byte) (l >> 16 & 0xff), (byte) (l >> 8 & 0xff), (byte) (l >> 0 & 0xff), };
	}

	private static long byteArrayToLong(final byte[] bs) {
		return (long) (0xff & bs[0]) << 56 | (long) (0xff & bs[1]) << 48 | (long) (0xff & bs[2]) << 40 | (long) (0xff & bs[3]) << 32
				| (long) (0xff & bs[4]) << 24 | (long) (0xff & bs[5]) << 16 | (long) (0xff & bs[6]) << 8 | (long) (0xff & bs[7]) << 0;
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param d
	 *            A double to be fingerprinted
	 * 
	 * @return The fingerprint of d
	 */
	@FunctionSpec(name = "fingerprintof", formalParameters = { "float" })
	public long fingerprintOf(final double d) {
		return SizzleSpecialIntrinsics
				.byteArrayToLong(SizzleSpecialIntrinsics.md.digest(SizzleSpecialIntrinsics.longToByteArray(Double.doubleToRawLongBits(d))));
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param s
	 *            A {@link String} to be fingerprinted
	 * 
	 * @return The fingerprint of s
	 */
	@FunctionSpec(name = "fingerprintof", formalParameters = { "string" })
	public long fingerprintOf(final String s) {
		return SizzleSpecialIntrinsics.byteArrayToLong(SizzleSpecialIntrinsics.md.digest(s.getBytes()));
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param bs
	 *            An array of byte to be fingerprinted
	 * 
	 * @return The fingerprint of bs
	 */
	@FunctionSpec(name = "fingerprintof", formalParameters = { "bytes" })
	public long fingerprintOf(final byte[] bs) {
		return SizzleSpecialIntrinsics.byteArrayToLong(SizzleSpecialIntrinsics.md.digest(bs));
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param b
	 *            A boolean to be fingerprinted
	 * 
	 * @return The fingerprint of b
	 */
	@FunctionSpec(name = "fingerprintof", formalParameters = { "bool" })
	public long fingerprintOf(final boolean b) {
		if (b)
			return 1;

		return 0;
	}

	/**
	 * The fingerprintof function returns the 64-bit fingerprint of the
	 * argument, which may be of any type.
	 * 
	 * @param l
	 *            A long to be fingerprinted
	 * 
	 * @return The fingerprint of l
	 */
	@FunctionSpec(name = "fingerprintof", formalParameters = { "fingerprint" })
	public long fingerprintOf(final long l) {
		return SizzleSpecialIntrinsics.byteArrayToLong(SizzleSpecialIntrinsics.md.digest(SizzleSpecialIntrinsics.longToByteArray(l)));
	}

	// TODO: implement new()
	// TODO: implement regex()

	private static SawyerReturn sawyer(final String string, final String[] regexes) {
		final List<String> result = new ArrayList<String>();

		final Pattern p = Pattern.compile(regexes[0]);

		final Matcher matcher = p.matcher(string);
		if (matcher.find()) {
			if (regexes.length > 1) {
				final SawyerReturn sawyerReturn = SizzleSpecialIntrinsics.sawyer(string.substring(matcher.end()),
						Arrays.copyOfRange(regexes, 1, regexes.length));

				result.add(string.substring(matcher.start(), matcher.end()));
				result.addAll(sawyerReturn.result);

				return new SawyerReturn(result, sawyerReturn.leftover);
			} else {
				result.add(string.substring(matcher.start(), matcher.end()));

				return new SawyerReturn(result, string.substring(matcher.end()));
			}
		} else {
			return new SawyerReturn(result, string);
		}

	}

	private static String[] saw(final int n, final String string, final String[] regexes) {
		final List<String> result = new ArrayList<String>();

		String todo = string;
		for (int i = 0; i < n; i++) {
			final SawyerReturn sawyerReturn = SizzleSpecialIntrinsics.sawyer(todo, Arrays.copyOf(regexes, regexes.length));
			result.addAll(sawyerReturn.result);
			if (sawyerReturn.leftover.equals("") || sawyerReturn.leftover.equals(todo))
				break;

			todo = sawyerReturn.leftover;
		}

		return result.toArray(new String[result.size()]);
	}

	@FunctionSpec(name = "saw", returnType = "array of string", formalParameters = { "string", "string..." })
	public static String[] saw(final String string, final String... regexes) {
		return SizzleSpecialIntrinsics.saw(1, string, regexes);
	}

	@FunctionSpec(name = "sawn", returnType = "array of string", formalParameters = { "int", "string", "string..." })
	public static String[] sawn(final int n, final String string, final String... regexes) {
		return SizzleSpecialIntrinsics.saw(n, string, regexes);
	}

	@FunctionSpec(name = "sawzall", returnType = "array of string", formalParameters = { "string", "string..." })
	public static String[] sawzall(final String string, final String... regexes) {
		// will someone ever trigger this obscure bug?
		return SizzleSpecialIntrinsics.saw(Integer.MAX_VALUE, string, regexes);
	}
}

class SawyerReturn {
	List<String> result;
	String leftover;

	public SawyerReturn(final List<String> result, final String leftover) {
		this.result = result;
		this.leftover = leftover;
	}
}
