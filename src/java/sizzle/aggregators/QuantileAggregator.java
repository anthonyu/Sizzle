package sizzle.aggregators;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import sizzle.io.EmitKey;


/**
 * A Sizzle aggregator to calculate the quantiles for the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
abstract class QuantileAggregator extends Aggregator {
	private int total;

	/**
	 * Construct a QuantileAggregator.
	 * 
	 * @param n
	 *            A long representing the number of quantiles to calculate
	 */
	public QuantileAggregator(long n) {
		super(n);
	}

	/**
	 * Parse a string as long and add it to the running total.
	 * 
	 * @param metadata
	 *            A {@link String} containing the number of values, or null
	 * 
	 * @return A long representing the value in metadata
	 */
	public long count(String metadata) {
		long count;
		if (metadata == null)
			count = 1;
		else
			count = Long.parseLong(metadata);

		this.total += count;

		return count;
	}

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);

		this.total = 0;
	}

	/** {@inheritDoc} */
	@Override
	public abstract void aggregate(String data, String metadata) throws IOException;

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we're in the combiner, just output the compressed data
		if (this.isCombining()) {
			for (Pair<String, Long> e : this.getTuples())
				this.collect(e.getFirst(), e.getSecond().toString());
		} else {
			// otherwise, set up the quantiles
			int n = (int) (this.getArg() - 1);
			String[] quartiles = new String[n];
			double step = this.total / (double) n;

			long last = 0;
			long q = 0;
			for (Pair<String, Long> e : this.getTuples()) {
				q += e.getSecond();

				int curr = (int) (q / step);

				if (curr == last)
					continue;

				last = curr;

				quartiles[curr - 1] = e.getFirst();
			}

			this.collect(Arrays.toString(quartiles));
		}
	}

	/**
	 * Return the data points from the dataset in pairs.
	 * 
	 * @return A {@link List} of {@link Pair}&lt{@link Number}, {@link Long}&gt;
	 *         containing the data points from the dataset
	 */
	public abstract List<Pair<String, Long>> getTuples();

	/** {@inheritDoc} */
	@Override
	public boolean isAssociative() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCommutative() {
		return true;
	}
}