package sizzle.aggregators;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import sizzle.io.EmitValue;

/**
 * A Sizzle aggregator to output all of the values in a dataset without any
 * indices.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "text")
public class TextAggregator extends Aggregator {
	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException, InterruptedException {
		// just pass it through
		this.collect(data);
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	protected void collect(final String data, final String metadata) throws IOException, InterruptedException {
		if (this.isCombining())
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
		else if (metadata != null)
			this.getContext().write(new Text(data + " weight " + metadata), NullWritable.get());
		else
			this.getContext().write(new Text(data), NullWritable.get());
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
