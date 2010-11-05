package sizzle.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;

public class MakeRandomInts {
	public static void main(final String[] args) throws FileNotFoundException {
		final Random random = new Random();

		final PrintStream out = new PrintStream(new File("ints"));

		int count = 0;
		long sum = 0;
		long sumsq = 0;

		for (int i = 0; i < 10000; i++) {
			final int j = random.nextInt(100);

			count++;
			sum += j;
			sumsq += j * j;

			out.println(j);
		}

		out.close();

		System.out.println(Integer.toString(count) + " " + sum + " " + sumsq);
	}
}
