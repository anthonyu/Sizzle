package sizzle.aggregators;

import java.io.IOException;

/**
 * A Sizzle aggregator to output data to stdout.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "stdout", type = "string")
public class StdoutAggregator extends Aggregator {
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException {
		System.out.println(data);
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
