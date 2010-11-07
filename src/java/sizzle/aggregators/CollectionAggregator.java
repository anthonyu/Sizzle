package sizzle.aggregators;

import java.io.IOException;

/**
 * A Sizzle aggregator to output all of the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "collection")
public class CollectionAggregator extends Aggregator {
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		// just pass it through
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
}
