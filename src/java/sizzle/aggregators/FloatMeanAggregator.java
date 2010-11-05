package sizzle.aggregators;

import java.io.IOException;

import sizzle.io.EmitKey;


/**
 * A Sizzle aggregator to calculate a mean of the values in a dataset.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "mean", type = "float")
public class FloatMeanAggregator extends MeanAggregator {
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
			this.aggregate(Double.parseDouble(data), metadata);
		else
			this.aggregate(Long.parseLong(data), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(long data, String metadata) {
		this.aggregate(Long.valueOf(data).doubleValue(), metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(double data, String metadata) {
		this.sum += data;

		super.count(metadata);
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		// if we are in the combiner, output the sum and the count
		if (this.isCombining())
			this.collect(this.sum, Long.toString(this.getCount()));
		// otherwise, output the final answer
		else
			this.collect(this.sum / this.getCount());
	}
}
