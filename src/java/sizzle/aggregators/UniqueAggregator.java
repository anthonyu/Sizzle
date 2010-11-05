package sizzle.aggregators;

import java.io.IOException;


import org.apache.hadoop.util.bloom.Filter;
import org.apache.hadoop.util.bloom.Key;

import sizzle.io.EmitKey;

/**
 * A Sizzle aggregator to estimate the size of the set of unique values in a
 * dataset. Roughly equivalent to a count(distinct(*)).
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "unique", formalParameters = { "int" })
public class UniqueAggregator extends DistinctAggregator {
	private long total;

	/**
	 * Construct a UniqueAggregator.
	 * 
	 * @param arg
	 *            The size of the internal table used to perform the
	 *            calculation.
	 */
	public UniqueAggregator(long arg) {
		super(arg);
	}

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);

		// clear out the internal total
		this.total = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
		// instantiate a bloom filter input key initialized by the data
		Key key = new Key(data.getBytes());

		// if the key is already in the filter, forget about it
		Filter filter = this.getFilter();
		if (filter.membershipTest(key))
			return;

		// add the key to the bloom filter
		filter.add(key);

		if (this.isCombining())
			this.collect(data);
		else
			this.total++;
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we are reducing, collect the total
		if (!this.isCombining())
			this.collect(this.total);
	}
}