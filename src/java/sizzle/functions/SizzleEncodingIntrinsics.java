package sizzle.functions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;

public class SizzleEncodingIntrinsics {
	/**
	 * The function tobase64 takes an input array of byte and returns an array
	 * of byte containing its base64 encoding. The "safe" flag, if set, invokes
	 * the web-safe encoding that uses '-' instead of '+' and '_' instead of
	 * '/', and does not pad the output with =.
	 * 
	 * @param b
	 *            An array of byte containing the unencoded input
	 * 
	 * @param websafe
	 *            Whether the output shall be web safe
	 * 
	 * @return The Base64 encoding of <em>b</em>
	 */
	@FunctionSpec(name = "tobase64", returnType = "bytes", formalParameters = { "bytes", "bool" })
	public static byte[] toBase64(final byte[] input, final boolean websafe) {
		return Base64.encodeBase64(input, false, websafe);
	}

	/**
	 * The function frombase64 takes an input bytes array and returns a bytes
	 * array containing its base64 decoding. The boolean flag, if set, invokes
	 * the web-safe decoding that uses '-' instead of '+' and '_' instead of
	 * '/'.
	 * 
	 * @param b
	 *            An array of byte containing the encoded input
	 * 
	 * @param websafe
	 *            Whether the output shall be web safe
	 * 
	 * @return The Base64 decoding of <em>b</em>
	 */
	@FunctionSpec(name = "frombase64", returnType = "bytes", formalParameters = { "bytes", "bool" })
	public static byte[] fromBase64(final byte[] input, final boolean websafe) {
		return Base64.decodeBase64(input);
	}

	/**
	 * Decompress gzip compressed data. The data must contain a valid gzip
	 * header and footer (as in a .gz file), but data after the footer is
	 * ignored.
	 * 
	 * @param compressedData
	 *            An array of byte containing gzip compressed data
	 * 
	 * @return An array of byte containing the uncompressed data
	 * 
	 * @throws IOException
	 */
	@FunctionSpec(name = "gunzip", returnType = "bytes", formalParameters = { "bytes" })
	public static byte[] gUnzip(final byte[] compressedData) throws IOException {
		final GZIPInputStream i = new GZIPInputStream(new ByteArrayInputStream(compressedData));

		try {
			final ByteArrayOutputStream o = new ByteArrayOutputStream(compressedData.length);

			try {
				final byte[] buf = new byte[4096];

				int len;
				while ((len = i.read(buf)) > 0)
					o.write(buf, 0, len);
			} finally {
				o.close();
			}

			return o.toByteArray();
		} finally {
			i.close();
		}
	}

	/**
	 * Compress data using gzip.
	 * 
	 * @param uncompressedData
	 *            An array of byte containing data to be compressed
	 * 
	 * @return An array of byte containing the compressed data
	 * 
	 * @throws IOException
	 */
	@FunctionSpec(name = "gzip", returnType = "bytes", formalParameters = { "bytes" })
	public static byte[] gZip(final byte[] uncompressedData) throws IOException {
		final InputStream i = new ByteArrayInputStream(uncompressedData);

		try {
			final ByteArrayOutputStream o = new ByteArrayOutputStream();

			try {
				final GZIPOutputStream g = new GZIPOutputStream(o);

				try {
					final byte[] buf = new byte[8192];

					int len;
					while ((len = i.read(buf)) > 0)
						g.write(buf, 0, len);
				} finally {
					g.close();
				}
			} finally {
				o.close();
			}

			return o.toByteArray();
		} finally {
			i.close();
		}
	}

	/**
	 * Uncompresses the zipped data using zlib, and returns the uncompressed
	 * data.
	 * 
	 * @param compressedData
	 *            An array of byte containing gzip compressed data
	 * 
	 * @param skipHeader
	 *            Ignored
	 * 
	 * @return An array of byte containing the uncompressed data
	 * 
	 * @throws IOException
	 */
	@FunctionSpec(name = "zlibuncompress", returnType = "bytes", formalParameters = { "bytes", "bool" })
	public static byte[] zlibUncompress(final byte[] compressedData, final boolean skipHeader) throws IOException {
		final DeflaterInputStream i = new DeflaterInputStream(new ByteArrayInputStream(compressedData), new Deflater(Deflater.DEFAULT_COMPRESSION), 8192);

		try {
			final ByteArrayOutputStream o = new ByteArrayOutputStream(compressedData.length);

			try {
				final byte[] buf = new byte[4096];

				int len;
				while ((len = i.read(buf)) > 0)
					o.write(buf, 0, len);
			} finally {
				o.close();
			}

			return o.toByteArray();
		} finally {
			i.close();
		}
	}

	/**
	 * Compresses data using deflate.
	 * 
	 * @param uncompressedData
	 *            An array of byte containing data to be compressed
	 * 
	 * @param skipHeader
	 *            Ignored
	 * 
	 * @return An array of byte containing the compressed data
	 * 
	 * @throws IOException
	 */
	@FunctionSpec(name = "zlibcompress", returnType = "bytes", formalParameters = { "bytes", "bool" })
	public static byte[] zlibCompress(final byte[] uncompressedData, final boolean skipHeader) throws IOException {
		final InputStream i = new ByteArrayInputStream(uncompressedData);

		try {
			final ByteArrayOutputStream o = new ByteArrayOutputStream();

			try {
				final DeflaterOutputStream d = new DeflaterOutputStream(o, new Deflater(Deflater.DEFAULT_COMPRESSION), 8192);

				try {
					final byte[] buf = new byte[8192];

					int len;
					while ((len = i.read(buf)) > 0)
						d.write(buf, 0, len);
				} finally {
					d.close();
				}
			} finally {
				o.close();
			}

			return o.toByteArray();
		} finally {
			i.close();
		}
	}
}
