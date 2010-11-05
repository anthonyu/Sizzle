package sizzle.aggregators;

import java.io.IOException;

/**
 * A Sizzle aggregator to output data to stderr.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "stderr", type = "string")
public class StderrAggregator extends Aggregator {
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException {
		System.err.println(data);
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
