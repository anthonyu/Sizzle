package sizzle.aggregators;

import java.io.IOException;


import org.apache.hadoop.util.bloom.DynamicBloomFilter;
import org.apache.hadoop.util.bloom.Filter;
import org.apache.hadoop.util.bloom.Key;
import org.apache.hadoop.util.hash.Hash;

import sizzle.io.EmitKey;

/**
 * A Sizzle aggregator to estimate the set of the unique values in a dataset.
 * Roughly equivalent to a distinct(*).
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "distinct", formalParameters = { "int" })
public class DistinctAggregator extends Aggregator {
	// from o.a.h.io.BloomMapFile#initBloomFilter
	private static int HASH_COUNT = 5;

	private final int vectorSize;
	private final float errorRate;

	private Filter filter;

	/**
	 * Construct a DistinctAggregator.
	 * 
	 * @param arg
	 *            The size of the internal table used to perform the
	 *            calculation.
	 */
	public DistinctAggregator(long arg) {
		super(arg);

		// this is all cribbed from o.a.h.io.BloomMapFile#initBloomFilter

		// vector size should be <code>-kn / (ln(1 - c^(1/k)))</code> bits for
		// single key, where <code> is the number of hash functions,
		// <code>n</code> is the number of keys and <code>c</code> is the
		// desired
		// max. error rate.
		// Our desired error rate is by default 0.005, i.e. 0.5%
		this.errorRate = 0.005f;
		this.vectorSize = (int) Math.ceil(-DistinctAggregator.HASH_COUNT * arg / Math.log(1.0 - Math.pow(this.errorRate, 1.0 / DistinctAggregator.HASH_COUNT)));
	}

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);

		// TODO: add a clear function to the bloom filter in Hadoop and use it
		// here instead of instantiating a new one for every key
		this.filter = new DynamicBloomFilter(this.vectorSize, DistinctAggregator.HASH_COUNT, Hash.MURMUR_HASH, (int) this.getArg());
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
		// instantiate a bloom filter input key initialized by the data
		Key key = new Key(data.getBytes());

		// if the key is already in the filter, forget it
		if (this.filter.membershipTest(key))
			return;

		// add the key to the bloom filter
		this.filter.add(key);

		// and collect it
		this.collect(data);
	}

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

	protected Filter getFilter() {
		return this.filter;
	}
}