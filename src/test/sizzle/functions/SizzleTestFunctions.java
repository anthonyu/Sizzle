package sizzle.functions;

import java.net.MalformedURLException;
import java.net.URL;

import sizzle.functions.FunctionSpec;

public class SizzleTestFunctions {
	private static int[] scrabbleValue = new int[] { 1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10 };

	@FunctionSpec(name = "keywords", returnType = "array of string", formalParameters = { "string" })
	public static String[] keywords(final String url) {
		try {
			return new URL(url).getQuery().split("=", 2)[1].split("\\+");
		} catch (final MalformedURLException e) {
			return null;
		}
	}

	@FunctionSpec(name = "domain", returnType = "string", formalParameters = { "string" })
	public static String domain(final String url) {
		try {
			return new URL(url).getHost();
		} catch (final MalformedURLException e) {
			return null;
		}
	}

	@FunctionSpec(name = "scrabble", returnType = "int", formalParameters = { "string" })
	public static long scrabble(final String word) {
		int sum = 0;
		for (final char c : word.toCharArray())
			sum += SizzleTestFunctions.scrabbleValue[Character.toLowerCase(c) - 'a'];

		return sum;
	}

	@FunctionSpec(name = "regexreplace", returnType = "string", formalParameters = { "string", "string", "string", "bool" })
	public static String regexReplace(final String string, final String regex, final String replacement, final boolean replaceAll) {
		if (replaceAll)
			return string.replaceAll(regex, replacement);
		else
			return string.replaceFirst(regex, replacement);
	}
}
