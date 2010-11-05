package sizzle.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;

public class MakeRandomDoubles {
	public static void main(final String[] args) throws FileNotFoundException {
		final Random random = new Random();

		final PrintStream out = new PrintStream(new File("doubles"));

		int count = 0;
		double sum = 0.0;
		double sumsq = 0.0;

		for (int i = 0; i < 10000; i++) {
			final double down = random.nextDouble();

			count++;
			sum += down;
			sumsq += down * down;

			out.println(down);
		}

		out.close();

		System.out.println(Integer.toString(count) + " " + sum + " " + sumsq);
	}
}
