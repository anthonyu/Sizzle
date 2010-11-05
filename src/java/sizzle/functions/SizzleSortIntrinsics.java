package sizzle.functions;

import java.util.Arrays;

public class SizzleSortIntrinsics {
	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of int", formalParameters = { "array of int" })
	public static long[] sortLongArray(final long[] a) {
		final long[] b = Arrays.copyOf(a, a.length);

		Arrays.sort(b);

		return b;
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of int", formalParameters = { "array of int", "string" })
	public static long[] sortLongArray(final long[] a, final String ignored) {
		return SizzleSortIntrinsics.sortLongArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of double
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of float", formalParameters = { "array of float" })
	public static double[] sortDoubleArray(final double[] a) {
		final double[] b = Arrays.copyOf(a, a.length);

		Arrays.sort(b);

		return b;
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of double
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of float", formalParameters = { "array of float", "string" })
	public static double[] sortDoubleArray(final double[] a, final String ignored) {
		return SizzleSortIntrinsics.sortDoubleArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of {@link String}
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of string", formalParameters = { "array of string" })
	public static String[] sortStringArray(final String[] a) {
		final String[] b = Arrays.copyOf(a, a.length);

		Arrays.sort(b);

		return b;
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of {@link String}
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of string", formalParameters = { "array of string", "string" })
	public static String[] sortStringArray(final String[] a, final String ignored) {
		return SizzleSortIntrinsics.sortStringArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of time", formalParameters = { "array of time" })
	public static long[] sortTimeArray(final long[] a) {
		return SizzleSortIntrinsics.sortLongArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of time", formalParameters = { "array of time", "string" })
	public static long[] sortTimeArray(final long[] a, final String ignored) {
		return SizzleSortIntrinsics.sortTimeArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of byte[]
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of bytes", formalParameters = { "array of bytes" })
	public static byte[][] sortBytesArray(final byte[][] a) {
		final byte[][] b = Arrays.copyOf(a, a.length);

		Arrays.sort(b);

		return b;
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of byte[]
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of bytes", formalParameters = { "array of bytes", "string" })
	public static byte[][] sortBytesArray(final byte[][] a, final String ignored) {
		return SizzleSortIntrinsics.sortBytesArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of fingerprint", formalParameters = { "array of fingerprint" })
	public static long[] sortFingerprintArray(final long[] a) {
		return SizzleSortIntrinsics.sortLongArray(a);
	}

	/**
	 * Returns the sorted version of an array. Only scalar values can be sorted.
	 * Values will be arranged in increasing order. (An optional comparison
	 * function, which takes two elements and returns int {-,0,+}, is accepted
	 * as a second argument, but it is curently ignored.)
	 * 
	 * @param a
	 *            An array of long
	 * 
	 * @return A sorted copy of <em>a</em>
	 */
	@FunctionSpec(name = "sort", returnType = "array of fingerprint", formalParameters = { "array of fingerprint", "string" })
	public static long[] sortFingerprintArray(final long[] a, final String ignored) {
		return SizzleSortIntrinsics.sortLongArray(a);
	}

	// TODO: implement sortx
}
