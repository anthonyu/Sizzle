package sizzle.aggregators;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;

import sizzle.io.EmitKey;

/**
 * A container for one or more Sizzle aggregators.
 * 
 * @author anthonyu
 * 
 */
public class Table {
	private final Aggregator[] aggregators;
	@SuppressWarnings("rawtypes")
	private Context context;
	private EmitKey key;

	public Table(final Aggregator... aggregators) {
		this.aggregators = aggregators;
	}

	public Aggregator getAggregator(final int index) {
		return this.aggregators[index];
	}

	public Aggregator[] getAggregators() {
		return this.aggregators;
	}

	public boolean isAssociative() {
		for (final Aggregator a : this.aggregators)
			if (!a.isAssociative())
				return false;

		return true;
	}

	public void setCombining(final boolean combining) {
		for (final Aggregator a : this.aggregators)
			a.setCombining(combining);
	}

	public void start(final EmitKey key) {
		this.key = key;

		for (final Aggregator a : this.aggregators)
			a.start(key);
	}

	public void setContext(@SuppressWarnings("rawtypes") final org.apache.hadoop.mapreduce.Reducer.Context context) {
		this.context = context;

		for (final Aggregator a : this.aggregators)
			a.setContext(context);
	}

	public void aggregate(final String[] data, final String metadata) throws IOException, InterruptedException, FinishedException {
		for (int i = 0; i < data.length && i < this.aggregators.length; i++)
			this.aggregators[i].aggregate(data[i], metadata);
	}

	@SuppressWarnings("unchecked")
	public void finish() throws IOException, InterruptedException {
		if (this.aggregators.length > 1) {
			final StringBuilder sb = new StringBuilder(this.key + " = { ");

			for (final Aggregator a : this.aggregators)
				sb.append(a.getResult().getData()[0] + ", ");

			final String out = sb.toString();

			this.context.write(new Text(out.substring(0, out.length() - 2) + " }"), NullWritable.get());
		} else {
			this.aggregators[0].finish();
		}
	}
}
