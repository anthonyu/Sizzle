package sizzle.aggregators;

import java.io.IOException;

import sizzle.io.EmitKey;

/**
 * A Sizzle aggregator to increment named mapreduce counters by weight.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "mrcounter", type = "int")
public class MrcounterAggregator extends Aggregator {
	private String group;
	private String name;

	@Override
	public void setKey(final EmitKey key) {
		// set the group and name for the counter
		if (key.getIndex().length() == 2) {
			this.group = "Sizzle Counters";
			this.name = "Unnamed counter";
		} else {
			final String[] indices = key.getIndex().replace("[", "").split("]");

			if (indices.length == 1) {
				this.group = "Sizzle Counters";
				this.name = indices[0];
			} else {
				this.group = indices[0];

				final StringBuilder name = new StringBuilder();
				for (int i = 1; i < indices.length; i++)
					name.append(indices[i]);
				this.name = name.toString();
			}
		}

		super.setKey(key);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) throws IOException {
		// we can get away with just incrementing these in the combiner
		if (!this.isCombining())
			return;

		// increment the counter specified by the key and its index and the
		// current data item

		this.getContext().getCounter(this.group, this.name).increment(Long.parseLong(data));
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
