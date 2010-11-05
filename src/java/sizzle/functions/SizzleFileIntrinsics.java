package sizzle.functions;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * Files and other OS resources.
 * 
 * @author anthonyu
 * 
 */
public class SizzleFileIntrinsics {
	private static FileSystem fs;

	static {
		try {
			SizzleFileIntrinsics.fs = FileSystem.get(new Configuration());
		} catch (final IOException e) {
			throw new RuntimeException(e.getClass().getSimpleName() + " caught", e);
		}
	}

	/**
	 * Return the entire contents of the named file as an uninterpreted array of
	 * bytes.
	 * 
	 * @param file
	 *            A {@link String} containing the name of the file
	 * 
	 * @return An array of byte containing the contents of the named file
	 * 
	 * @throws IOException
	 */
	@FunctionSpec(name = "load", returnType = "bytes", formalParameters = { "string" })
	public static byte[] load(final String file) throws IOException {
		final InputStream i = new BufferedInputStream(SizzleFileIntrinsics.fs.open(new Path(file)));

		try {
			final ByteArrayOutputStream o = new ByteArrayOutputStream();

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
	 * Return the contents of the named environment variable as a string. The
	 * raw data is interpreted as UTF-8 in the same manner as the default
	 * conversion from bytes to string.
	 * 
	 * @param variable
	 *            A {@link String} containing the name of the desired
	 *            environment variable
	 * 
	 * @return A {@link String} containing the contents of the desired
	 *         environment variable
	 * 
	 * @throws IOException
	 */
	@FunctionSpec(name = "getenv", returnType = "string", formalParameters = { "string" })
	public static String getenv(final String variable) throws IOException {
		return System.getenv(variable);
	}

	// TODO: implement {set,get,lock}additionalinput
	/*
	 * getadditionalinput: function(variable: string): bytes;
	 * 
	 * A map of strings to bytes may be provided to Proc by the process running
	 * szl. Return the bytes mapped to by the argument. Never returns undef.
	 * 
	 * 
	 * setadditionalinput: function(label: string, value: bytes);
	 * 
	 * Stores a (label, value) pair. Never returns undef.
	 * 
	 * 
	 * lockadditionalinput: function();
	 * 
	 * Prevents further calls to setadditionalinput for this record. Never
	 * returns undef.
	 * 
	 * 
	 * type resourcestats = { initializedavailablemem: int,
	 * initializedallocatedmem: int, initializedusertime: time,
	 * initializedsystemtime: time, availablemem: int, allocatedmem: int,
	 * usertime: time, systemtime: time };
	 * 
	 * 
	 * getresourcestats: function(): resourcestats;
	 * 
	 * Return a tuple of type resourcestats containing resource usage
	 * statistics. The f irst set of numbers reports the statistics after static
	 * initialization. The second set reports the values consumed by processing
	 * the current input record. The availablemem figure reports total size of
	 * the heap; allocatedmem is the amount in use on the heap. Memory is
	 * measured in bytes, and time is measured in microseconds. Availability and
	 * accuracy of these values is implementation dependent. Never returns
	 * undef. The database intrinsics are not implemented. They are provided as
	 * a recommendation for the any future implementation of database access.
	 */
	// TODO: implement database intrinsics
	/*
	 * type SQL_DB = int; # basic type
	 * 
	 * dbconnect: function(dbspec: string, defaultspec: string): int;
	 * 
	 * Connects to a database with the dbspecs and returns a db object. It is
	 * recommended to declare the db object as static so only one connection is
	 * made per worker. Returns undef only if an error occurs.
	 * 
	 * 
	 * dbquery: function(db: int, query: string): array of array of string;
	 * 
	 * Executes a sql query on the given database object. Returns an array of
	 * array of string, each array of string representing one row of results.
	 * For most queries such as SELECT statements, the results can be declared
	 * as static to avoid excessive queries on the database. Returns undef only
	 * if an error occurs.
	 */
}
