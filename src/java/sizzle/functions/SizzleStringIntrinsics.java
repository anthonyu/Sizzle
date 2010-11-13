package sizzle.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * String Manipulation
 * 
 * @author anthonyu
 * 
 */
public class SizzleStringIntrinsics {
	/**
	 * Returns a copy of the given {@link String} with all characters lowered.
	 * 
	 * @param s
	 *            A {@link String} that wants lowercasing
	 * 
	 * @return A copy of <i>s</i> with all characters converted to lower case,
	 *         as defined by Unicode.
	 * 
	 */
	@FunctionSpec(name = "lowercase", returnType = "string", formalParameters = { "string" })
	public static String lowerCase(final String s) {
		return s.toLowerCase();
	}

	/**
	 * Returns a copy of the given {@link String} with all characters uppered.
	 * 
	 * @param s
	 *            A {@link String} that wants uppercasing
	 * 
	 * @return A copy of <i>s</i> with all characters converted to upper case,
	 *         as defined by Unicode.
	 * 
	 */
	@FunctionSpec(name = "uppercase", returnType = "string", formalParameters = { "string" })
	public static String upperCase(final String s) {
		return s.toUpperCase();
	}

	/**
	 * Search for the first occurrence of the literal string p within s and
	 * return the integer index of its first character, or -1 if it does not
	 * occur.
	 * 
	 * @param p
	 *            A {@link String} containing the needle
	 * 
	 * @param s
	 *            A {@link String} containing the haystack
	 * 
	 * @return A long representing the first occurrence of the literal string
	 *         <em>p</em> within <em>s</em> and return the integer index of its
	 *         first character, or -1 if it does not occur
	 */
	@FunctionSpec(name = "strfind", returnType = "int", formalParameters = { "string", "string" })
	public static long indexOf(final String p, final String s) {
		return s.indexOf(p, 0);
	}

	/**
	 * Search for the last occurrence of the literal string p within s and
	 * return the integer index of its first character, or -1 if it does not
	 * occur.
	 * 
	 * @param p
	 *            A {@link String} containing the needle
	 * 
	 * @param s
	 *            A {@link String} containing the haystack
	 * 
	 * @return A long representing the last occurrence of the literal string
	 *         <em>p</em> within <em>s</em> and return the integer index of its
	 *         first character, or -1 if it does not occur
	 */
	@FunctionSpec(name = "strrfind", returnType = "int", formalParameters = { "string", "string" })
	public static long lastIndexOf(final String p, final String s) {
		return s.lastIndexOf(p, 0);
	}

	/**
	 * Search for the first occurrence of the literal bytes p within b and
	 * return the integer index of its first byte, or -1 if it does not occur.
	 * 
	 * @param p
	 *            An array of byte containing the needle
	 * 
	 * @param s
	 *            A array of byte containing the haystack
	 * 
	 * @return A long representing the first occurrence of the literal bytes
	 *         <em>p</em> within <em>s</em> and return the integer index of its
	 *         first byte, or -1 if it does not occur
	 */
	@FunctionSpec(name = "bytesfind", returnType = "string", formalParameters = { "string", "string" })
	public static long indexOf(final byte[] p, final byte[] s) {
		for (int i = 0; i < s.length; i++)
			for (int j = 0; j < p.length; j++)
				if (s[i] != p[j])
					break;
				else if (j == p.length - 1)
					return i;

		return -1;
	}

	/**
	 * Search for the last occurrence of the literal bytes p within b and return
	 * the integer index of its first byte, or -1 if it does not occur.
	 * 
	 * @param p
	 *            An array of byte containing the needle
	 * 
	 * @param s
	 *            A array of byte containing the haystack
	 * 
	 * @return A long representing the last occurrence of the literal bytes
	 *         <em>p</em> within <em>s</em> and return the integer index of its
	 *         first byte, or -1 if it does not occur
	 */
	@FunctionSpec(name = "bytesrfind", returnType = "string", formalParameters = { "string", "string" })
	public static long lastIndexOf(final byte[] p, final byte[] s) {
		for (int i = s.length - p.length; i >= 0; i--)
			for (int j = 0; j < p.length; j++)
				if (s[i] != p[j])
					break;
				else if (j == p.length - 1)
					return i;

		return -1;
	}

	/**
	 * Return a copy of string <em>str</em>, with non-overlapping instances of
	 * <em>lit</em> replaced by <em>rep</em>. If <em>replace_all</em> is false,
	 * only the first found instance is replaced.
	 * 
	 * @param str
	 *            A {@link String} containing the source string
	 * 
	 * @param lit
	 *            A {@link String} containing the substring to be replaced
	 * 
	 * @param rep
	 *            A {@link String} containing the replacement string
	 * 
	 * @param replaceAll
	 *            A boolean representing whether to replace every instance of
	 *            <em>lit</em> with <em>rep</em>
	 * 
	 * @return A copy of {@link String} <em>str</em>, with non-overlapping
	 *         instances of <em>lit</em> replaced by <em>rep</em>
	 */
	@FunctionSpec(name = "strreplace", returnType = "string", formalParameters = { "string", "string", "string", "bool" })
	public static String stringReplace(final String str, final String lit, final String rep, final boolean replaceAll) {
		if (replaceAll)
			return str.replace(lit, rep);
		else
			return str.replaceFirst(Pattern.quote(lit), rep);
	}

	/**
	 * Search for a match of the regular expression <em>r</em> within <em>s</em>
	 * , and return a boolean value indicating whether a match was found. (The
	 * regular expression syntax is that of PCRE. <http://www.pcre.org/>)
	 * 
	 * @param r
	 *            A {@link String} containing a regular expression
	 * 
	 * @param s
	 *            A {@link String} containing the text to be searched
	 * 
	 * @return A boolean representing whether the regular expression <em>r</em>
	 *         was found within <em>s</em>
	 */
	@FunctionSpec(name = "match", returnType = "bool", formalParameters = { "string", "string" })
	public static boolean match(final String r, final String s) {
		final Matcher m = Pattern.compile(r).matcher(s);
		return m.find();
	}

	/**
	 * Search for a match of the regular expression <em>r</em> within <em>s</em>
	 * , and return an array consisting of character positions within <em>s</em>
	 * defined by the match. Positions 0 and 1 of the array report the location
	 * of the match of the entire expression, subsequent pairs report the
	 * location of matches of successive parenthesized subexpressions.
	 * 
	 * @param r
	 *            A {@link String} containing a regular expression
	 * 
	 * @param s
	 *            A {@link String} containing the text to be searched
	 * 
	 * @return An array of long consisting of character positions within
	 *         <em>s</em> defined by the match
	 */
	@FunctionSpec(name = "matchposns", returnType = "array of int", formalParameters = { "string", "string" })
	public static long[] matchPositions(final String r, final String s) {
		final Matcher m = Pattern.compile(r).matcher(s);

		if (!m.find())
			return new long[0];

		final int n = m.groupCount();

		final long[] matches = new long[(n + 1) * 2];

		for (int i = 0; i <= n; i++) {
			matches[i * 2] = m.start(i);
			matches[i * 2 + 1] = m.end(i);
		}

		return matches;
	}

	/**
	 * Search for a match of the regular expression <em>r</em> within <em>s</em>
	 * , and return . The 0th string is the entire match; following elements of
	 * the array hold matches of successive parenthesized subexpressions. This
	 * function is equivalent to using matchposns to find successive locations
	 * of matches and created array slices of <em>s</em> with the indices
	 * returned.
	 * 
	 * 
	 * @param r
	 *            A {@link String} containing a regular expression
	 * 
	 * @param s
	 *            A {@link String} containing the text to be searched
	 * 
	 * @return an array of {@link String} consisting of matched substrings of
	 *         <em>s</em>
	 */
	@FunctionSpec(name = "matchstrs", returnType = "array of string", formalParameters = { "string", "string" })
	public static String[] matchStrings(final String r, final String s) {
		final Matcher m = Pattern.compile(r).matcher(s);

		if (!m.find())
			return new String[0];

		final int n = m.groupCount();

		final String[] matches = new String[(n + 1)];

		for (int i = 0; i <= n; i++)
			matches[i] = m.group(i);

		return matches;
	}

	private static List<String> splitCsv(final String s) {
		final List<String> split = new ArrayList<String>();

		boolean inQuote = false;
		StringBuilder sb = new StringBuilder();
		for (final char c : s.trim().toCharArray())
			switch (c) {
			case ',':
				if (!inQuote) {
					split.add(sb.toString());
					sb = new StringBuilder();
				} else {
					sb.append(c);
				}
				break;
			case '"':
				if (!inQuote) {
					inQuote = true;
				} else {
					inQuote = false;
				}
				break;
			default:
				sb.append(c);
			}

		split.add(sb.toString());

		return split;
	}

	/**
	 * The function splitcsvline takes a line of UTF-8 bytes and splits it at
	 * commas, ignoring leading and trailing white space and using '"' for
	 * quoting. It returns the array of fields produced.
	 * 
	 * @param string
	 *            The {@link String} to be split
	 * 
	 * @return An array of byte[] containing the splits
	 * 
	 */
	@FunctionSpec(name = "splitcsvline", returnType = "array of bytes", formalParameters = { "bytes" })
	public static byte[][] splitCsvLine(final byte[] csv) {
		final List<String> split = SizzleStringIntrinsics.splitCsv(new String(csv));

		final byte[][] bytes = new byte[split.size()][];

		for (int i = 0; i < split.size(); i++)
			bytes[i] = split.get(i).getBytes();

		return bytes;
	}

	/**
	 * The function splitcsv takes an array of UTF-8 bytes containing lines of
	 * text, such as that produced by the load() builtin. It splits each line
	 * using the same method as splitcsvline, and then selects the fields
	 * indicated by the second argument (numbered starting at 1). The return
	 * value is a flat array of the collected fields.
	 * 
	 * @param csv An arry of byte containing the input data
         * @param fields An array of long specified the fields to be returned
	 * 
	 * @return An array of byte[] containing the collected fields
	 */
	@FunctionSpec(name = "splitcsv", returnType = "array of bytes", formalParameters = { "bytes", "array of int" })
	public static byte[][] splitCsv(final byte[] csv, final long[] fields) {
		final List<List<String>> strings = new ArrayList<List<String>>();

		for (final String line : new String(csv).split("\n")) {
			final List<String> values = SizzleStringIntrinsics.splitCsv(line);

			final List<String> b = new ArrayList<String>();
			for (final long field : fields)
				b.add(values.get((int) field - 1));

			strings.add(b);
		}

		final byte[][] output = new byte[strings.size() * fields.length][];

		for (int i = 0; i < strings.size(); i++)
			for (int j = 0; j < fields.length; j++)
				output[i * 2 + j] = strings.get(i).get(j).getBytes();

		return output;
	}

	/**
	 * Return a string containing the arguments formatted according to the
	 * format string fmt. The syntax of the format string is essentially that of
	 * ANSI C with the following differences:
	 * 
	 * <ul>
	 * <li>%b prints a boolean, "true" or "false".
	 * <li>%c prints a (u)int as a Unicode character in UTF-8.
	 * <li>%k like %c with single quotes and backslash escapes for special
	 * characters.
	 * <li>%s prints a Sawzall string as UTF-8.
	 * <li>%q like %s with double quotes and backslash escapes for special
	 * characters.
	 * <li>%p prints a fingerprint, in the format 0x%.16x.
	 * <li>%t prints a time, in the format of the Unix function ctime without a
	 * newline.
	 * <li>%T prints a Sawzall type of the argument; %#T expands user-defined
	 * types.
	 * <li>%d / %i / %o / %u / %x / %X apply to a Sawzall (u)int and have no 'l'
	 * or 'h' modifiers.
	 * <li>%e / %f / %g / %E / %G apply to a Sawzall float and have no 'l' or
	 * 'h' modifiers.
	 * </ul>
	 * format verbs 'n' and '*' are not supported.
	 * 
	 * @param format
	 *            A
	 * @param args
	 * 
	 * @return A string containing the arguments formatted according to the
	 *         format string <em>fmt</em>
	 */
	@FunctionSpec(name = "format", returnType = "string", formalParameters = { "string", "string..." })
	public static String format(final String format, final Object... args) {
		// TODO: support the Sawzall differences listed in the javadoc above
		return String.format(format, args);
	}
}
