package sizzle.aggregators;

import java.io.IOException;

import sizzle.io.EmitKey;


/**
 * A Sizzle aggregator to calculate the sum of the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "sum", type = "float")
public class FloatSumAggregator extends Aggregator {
	private double sum;

	/** {@inheritDoc} */
	@Override
	public void start(EmitKey key) {
		super.start(key);

		this.sum = 0;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(String data, String metadata) throws IOException, InterruptedException {
		if (data.indexOf('.') != -1)
			this.aggregate(Double.parseDouble(data));
		else
			this.aggregate(Long.parseLong(data));
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(long data, String metadata) {
		this.sum += data;
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(double data, String metadata) {
		this.sum += data;
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		this.collect(this.sum);
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
