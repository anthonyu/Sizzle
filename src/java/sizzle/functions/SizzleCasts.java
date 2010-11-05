package sizzle.functions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Some less trivial casts provided by Sizzle.
 * 
 * @author anthonyu
 * 
 */
public class SizzleCasts {
	/**
	 * Convert a {@link String} into a boolean.
	 * 
	 * @param s
	 *            The {@link String} to be converted
	 * 
	 * @return True iff <em>s</em> begins with 'T' or 't'
	 * 
	 */
	public static boolean stringToBoolean(final String s) {
		final char c = s.charAt(0);

		if (c == 'T' || c == 't')
			return true;

		return false;
	}

	/**
	 * Convert a boolean into a long.
	 * 
	 * @param b
	 *            The boolean to be converted
	 * 
	 * @return A long representing the boolean value <em>b</em>
	 */
	public static long booleanToLong(final boolean b) {
		if (b)
			return 1;
		return 0;
	}

	/**
	 * Convert a byte string into a long.
	 * 
	 * @param bs
	 *            An array of bytes containing the bit-level representation of a
	 *            long integer
	 * 
	 * @param encodingFormat
	 *            A {@link String} containing the encoding format. Supported int
	 *            encoding formats for conversion between bytes and int are
	 *            "fixed32-big", "fixed32-little", "fixed64-big",
	 *            "fixed64-little" (32- and 64-bit big- and little-endian packed
	 *            bytes), "saw" (an alias of "fixed64-big", "varint" (64-bit
	 *            run-length-encoded format used in protocol buffers), and
	 *            "zigzag" (a variant of "varint" which uses the ZigZag encoding
	 *            to encode negative numbers efficiently). The encoding
	 *            parameter is required.
	 * 
	 * @return A long representing the value in <em>bs</em>
	 */
	public static long bytesToLong(final byte[] bs, final String encodingFormat) {
		long l = 0;

		if (encodingFormat.startsWith("fixed")) {
			final boolean little = encodingFormat.endsWith("little");
			long tmp;
			for (int i = 0; i < bs.length; i++) {
				tmp = 0;
				if (little)
					tmp = bs[i];
				else
					tmp = bs[bs.length - i - 1];

				l |= tmp << ((i & 7) << 3);
			}
			// TODO: support these
		} else if (encodingFormat.equals("saw") || encodingFormat.equals("varint") || encodingFormat.equals("zigzag")) {
			throw new RuntimeException("unimplemented encoding " + encodingFormat);
		} else {
			throw new IllegalArgumentException("unsupported encoding " + encodingFormat);
		}

		return l;
	}

	/**
	 * Parse a time string.
	 * 
	 * @param s
	 *            A {@link String} containing a time
	 * 
	 * @return A long containing the time represented by <em>s</em>.
	 * 
	 * @throws ParseException
	 */
	public static long stringToTime(final String s) throws ParseException {
		return SizzleCasts.stringToTime(s, "PST8PDT");
	}

	/**
	 * Parse a time string.
	 * 
	 * @param s
	 *            A {@link String} containing a time
	 * 
	 * @return A long containing the time represented by <em>s</em>.
	 * 
	 * @throws ParseException
	 */
	public static long stringToTime(final String s, final String tz) throws ParseException {
		// t: time = "Apr 1 12:00:00 PST 2005";
		final SimpleDateFormat sizzleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss z yyyy");

		sizzleDateFormat.setCalendar(Calendar.getInstance(TimeZone.getTimeZone(tz)));

		return sizzleDateFormat.parse(s).getTime();
	}

	/**
	 * Pack an array of byte into a fingerprint.
	 * 
	 * @param bs
	 *            The array of byte to be packed
	 * 
	 * @return A long representing the packed bytes
	 */
	public static long bytesToFingerprint(final byte[] bs) {
		long l = 0;

		for (final byte b : bs) {
			l <<= 8;
			l |= b & 0xff;
		}

		return l;
	}

	/**
	 * Format a long into a {@link String} in the given radix.
	 * 
	 * @param l
	 *            A long
	 * 
	 * @param radix
	 *            The desired radix
	 * 
	 * @return A {@link String} containing the number <em>l</em> in base
	 *         <em>radix<em>
	 */
	public static String longToString(final long l, final long radix) {
		return Long.toString(l, (int) radix);
	}

	/**
	 * Format a time string.
	 * 
	 * @param t
	 *            A long containing a time
	 * 
	 * @return A {@link String} containing the time represented by <em>t</em>.
	 * 
	 */
	public static String timeToString(final long t, final String tz) {
		// t: time = "Apr 1 12:00:00 PST 2005";
		final SimpleDateFormat sizzleDateFormat = new SimpleDateFormat("MMM d HH:mm:ss z yyyy");

		final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(tz));

		calendar.setTimeInMillis(t / 1000);

		sizzleDateFormat.setCalendar(calendar);

		return sizzleDateFormat.format(calendar.getTime());
	}

	/**
	 * Convert a long into a byte string.
	 * 
	 * @param l
	 *            A long representing the value to be converted
	 * 
	 * @param encodingFormat
	 *            A {@link String} containing the encoding format. Supported int
	 *            encoding formats for conversion between bytes and int are
	 *            "fixed32-big", "fixed32-little", "fixed64-big",
	 *            "fixed64-little" (32- and 64-bit big- and little-endian packed
	 *            bytes), "saw" (an alias of "fixed64-big", "varint" (64-bit
	 *            run-length-encoded format used in protocol buffers), and
	 *            "zigzag" (a variant of "varint" which uses the ZigZag encoding
	 *            to encode negative numbers efficiently). The encoding
	 *            parameter is required.
	 * 
	 * @return An array of byte containing the bytes of <em>l</em>
	 */
	public static long longToBytes(final long l, final String encodingFormat) {
		if (encodingFormat.startsWith("fixed") || encodingFormat.equals("saw") || encodingFormat.equals("varint") || encodingFormat.equals("zigzag")) {
			throw new RuntimeException("unimplemented encoding " + encodingFormat);
		} else {
			throw new IllegalArgumentException("unsupported encoding " + encodingFormat);
		}
	}

	/**
	 * Unpack a fingerprint into an array of byte.
	 * 
	 * @param f
	 *            A long representing the fingerprint to be unpacked
	 * 
	 * @return An array of byte containing the unpacked value
	 */
	public static byte[] fingerprintToBytes(final long f) {
		final byte[] bs = new byte[8];

		for (int i = 0; i < 8; i++)
			bs[i] = (byte) (f >> i & 0xff);

		return bs;
	}

	/**
	 * Extract the bytes from a {@link String}.
	 * 
	 * @param s
	 *            A {@link String}
	 * 
	 * @return An array of byte containing the bytes of <em>s</em>
	 */
	public static byte[] stringToBytes(final String s) {
		return s.getBytes();
	}
}
