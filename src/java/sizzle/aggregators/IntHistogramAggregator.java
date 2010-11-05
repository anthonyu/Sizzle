package sizzle.aggregators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import sizzle.io.EmitKey;


/**
 * A Sizzle aggregator to calculate a histogram for the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "histogram", type = "int", formalParameters = { "int", "int", "int" })
public class IntHistogramAggregator extends HistogramAggregator {
	private SortedCountingSet<Long> list;

	/**
	 * Construct an IntHistogramAggregator.
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
	public IntHistogramAggregator(long min, long max, long buckets) {
		super(min, max, buckets);
	}

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);

		this.list = new SortedCountingSet<Long>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(String data, String metadata) throws NumberFormatException, IOException, InterruptedException {
		if (data.indexOf('.') != -1)
			this.aggregate(Double.valueOf(data).longValue(), metadata);
		else
			this.aggregate(Long.parseLong(data), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(long data, String metadata) throws IOException {
		this.list.add(Long.valueOf(data), super.count(metadata));
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(double data, String metadata) throws IOException {
		this.aggregate(Double.valueOf(data).longValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public List<Pair<Number, Long>> getTuples() {
		List<Pair<Number, Long>> list = new ArrayList<Pair<Number, Long>>();

		// convert the map entries into a list of Pair
		for (final Entry<Long, Long> e : this.list.getEntries())
			list.add(new Pair<Number, Long>(e.getKey(), e.getValue()));

		return list;
	}
}
