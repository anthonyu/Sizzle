package sizzle.aggregators;

import java.io.IOException;

import sizzle.io.EmitKey;

/**
 * A Sizzle aggregator to calculate the top or bottom <i>n</i> values in a
 * dataset by weight.
 * 
 * @author anthonyu
 * 
 */
abstract class MinOrMaxAggregator extends Aggregator {
	protected final WeightedString[] list;
	private final int last;

	/**
	 * Construct a MinOrMaxAggregator.
	 * 
	 * @param n
	 *            A long representing the number of values to return
	 */
	public MinOrMaxAggregator(final long n) {
		super(n);

		// an array of weighted string of length n
		this.list = new WeightedString[(int) this.getArg()];

		// the index of the last entry in the list
		this.last = (int) (this.getArg() - 1);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) {
		double weight;
		if (metadata == null)
			// by default, minimum weight
			weight = Double.MIN_VALUE;
		else
			weight = Double.parseDouble(metadata);

		final WeightedString s = new WeightedString(data, weight);

		if (this.compare(s, this.list[this.last]) > 0) {
			// find this new item's position within the list
			for (int i = 0; i < this.getArg(); i++)
				if (this.compare(s, this.list[i]) > 0) {
					// we found it. move all subsequent items down one spot
					for (int j = (int) (this.getArg() - 2); j >= i; j--)
						this.list[j + 1] = this.list[j];

					// insert the item where it belongs
					this.list[i] = s;

					break;
				}
		}
	}

	/**
	 * Compare two weighted strings.
	 * 
	 * @param a
	 *            A {@link WeightedString} containing a {@link String} and its
	 *            weight.
	 * 
	 * @param b
	 *            A {@link WeightedString} containing a {@link String} and its
	 *            weight.
	 * 
	 * @return An int representing the comparison between the two strings.
	 */
	abstract protected int compare(WeightedString s, WeightedString weightedString);

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		for (int i = 0; i < this.getArg(); i++)
			if (this.isCombining())
				this.collect(this.list[i].getString(), Double.toString(this.list[i].getWeight()));
			else
				this.collect(this.list[i].toString());
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

/**
 * A tuple containing a {@link String} and its weight.
 * 
 * @author anthonyu
 * 
 */
class WeightedString {
	private final String string;
	private final double weight;

	/**
	 * Construct a WeightedString.
	 * 
	 * @param string
	 *            A {@link String} containing the string part of the tuple
	 * 
	 * @param weight
	 *            A double representing the weight part of the tuple
	 */
	public WeightedString(final String string, final double weight) {
		this.string = string;
		this.weight = weight;
	}

	/**
	 * Get the string part of the tuple.
	 * 
	 * @return A {@link String} containing the string part of the tuple
	 */
	public String getString() {
		return this.string;
	}

	/**
	 * Get the weight part of the tuple.
	 * 
	 * @return A double containing the weight part of the tuple
	 */
	public double getWeight() {
		return this.weight;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.string == null ? 0 : this.string.hashCode());
		final long temp = Double.doubleToLongBits(this.weight);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final WeightedString other = (WeightedString) obj;
		if (this.string == null) {
			if (other.string != null)
				return false;
		} else if (!this.string.equals(other.string))
			return false;
		if (Double.doubleToLongBits(this.weight) != Double.doubleToLongBits(other.weight))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.string + " weight " + this.weight;
	}
}
