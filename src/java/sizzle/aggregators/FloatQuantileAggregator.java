package sizzle.aggregators;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import sizzle.io.EmitKey;


/**
 * A Sizzle aggregator to calculate the quantiles for the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "quantile", type = "float", formalParameters = { "int" })
public class FloatQuantileAggregator extends QuantileAggregator {
	private SortedCountingSet<Double> list;

	/**
	 * Construct a FloatQuantileAggregator.
	 * 
	 * @param n
	 *            A long representing the number of quantiles to calculate
	 */
	public FloatQuantileAggregator(long n) {
		super(n);
	}

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);

		this.list = new SortedCountingSet<Double>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(String data, String metadata) throws IOException {
		this.aggregate(Double.parseDouble(data), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(long data, String metadata) throws IOException {
		this.aggregate(Long.valueOf(data).doubleValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(double data, String metadata) throws IOException {
		this.list.add(Double.valueOf(data), super.count(metadata));
	}

	/** {@inheritDoc} */
	@Override
	public List<Pair<String, Long>> getTuples() {
		List<Pair<String, Long>> list = new ArrayList<Pair<String, Long>>();

		// convert the map entries into a list of Pair
		for (Entry<Double, Long> e : this.list.getEntries())
			list.add(new Pair<String, Long>(e.getKey().toString(), e.getValue()));

		return list;
	}
}
