package sizzle.aggregators;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import sizzle.io.EmitKey;


/**
 * A Sizzle aggregator to calculate a histogram for the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
abstract class HistogramAggregator extends Aggregator {
	private final long min;
	private final long max;
	private final int buckets;

	/**
	 * Construct a HistogramAggregator.
	 * 
	 * @param min
	 *            A long representing the minimum value to be considered in the
	 *            histogram
	 * 
	 * @param max
	 *            A long representing the maximum value to be considered in the
	 *            histogram
	 * 
	 * @param buckets
	 *            A long representing the number of buckets in the histogram
	 */
	public HistogramAggregator(long min, long max, long buckets) {
		this.min = min;
		this.max = max;
		this.buckets = (int) buckets;
	}

	public long count(String metadata) {
		// if the metadata is null, it counts as a single
		if (metadata == null)
			return 1;
		// otherwise, parse the metadata and count it as that
		else
			return Long.parseLong(metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);
	}

	/** {@inheritDoc} */
	@Override
	public abstract void aggregate(String data, String metadata) throws NumberFormatException, IOException, InterruptedException;

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			// if we're in the combiner, just output the compressed data
			for (Pair<Number, Long> p : this.getTuples())
				this.collect(p.getFirst().toString(), p.getSecond().toString());
		} else {
			// otherwise, set up the histogram
			int[] buckets = new int[this.buckets];
			// calculate the step or the space between the buckets
			double step = (this.max - this.min) / (double) this.buckets;

			// for each of the compressed data points, increment the bucket it
			// belongs to by its cardinality
			for (Pair<Number, Long> p : this.getTuples())
				buckets[(int) ((p.getFirst().longValue() - this.min) / step)] += p.getSecond();

			this.collect(Arrays.toString(buckets));
		}
	}

	/**
	 * Return the data points from the dataset in pairs.
	 * 
	 * @return A {@link List} of {@link Pair}&lt{@link Number}, {@link Long}&gt;
	 *         containing the data points from the dataset
	 */
	public abstract List<Pair<Number, Long>> getTuples();

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