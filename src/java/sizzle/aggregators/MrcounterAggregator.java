package sizzle.aggregators;

import java.io.IOException;

/**
 * A Sizzle aggregator to increment named mapreduce counters by weight.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "mrcounter", type = "string", weightType = "int")
public class MrcounterAggregator extends Aggregator {
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException {
		// we should be able to get away with just incrementing these in the
		// combiner
		if (!this.isCombining())
			return;

		// increment the counter specified by the key and its index and the
		// current data item
		this.getContext().getCounter(this.getKey().toString(), data).increment(Long.parseLong(metadata));
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
