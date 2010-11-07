package sizzle.aggregators;

import java.io.IOException;
import java.util.HashSet;

import sizzle.io.EmitKey;

/**
 * A Sizzle aggregator to filter the values in a dataset by maximum size.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "set")
public class SetAggregator extends Aggregator {
	private HashSet<String> set;
	private final long max;

	/**
	 * Construct a SetAggregator.
	 * 
	 * @param n
	 *            A long representing the number of values to return
	 */
	public SetAggregator(final long n) {
		super(n);

		// the maximum size we will pass through
		this.max = n;
	}

	@Override
	public void start(final EmitKey key) {
		super.start(key);

		// the set of data to be collected
		this.set = new HashSet<String>();
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException, FinishedException {
		this.set.add(data);

		if (this.set.size() > this.max)
			throw new FinishedException();
	}

	@Override
	public void finish() throws IOException, InterruptedException {
		for (final String s : this.set)
			this.collect(s);

		super.finish();
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
}