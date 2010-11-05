package sizzle.aggregators;

import sizzle.io.EmitKey;

/**
 * A Sizzle aggregator to calculate the top <i>n</i> values in a dataset by
 * weight.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "maximum", formalParameters = { "int" }, weightType = "float")
public class MaximumAggregator extends MinOrMaxAggregator {
	/**
	 * Construct a MaximumAggregator.
	 * 
	 * @param n
	 *            A long representing the number of values to return
	 */
	public MaximumAggregator(final long n) {
		super(n);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		// clear out the list
		for (int i = 0; i < this.getArg(); i++)
			this.list[i] = new WeightedString("", Double.MIN_VALUE);
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
	 * @return A positive integer if <em>a</em> is larger than <em>b</em>, zero
	 *         if they are equal, and a negative integer if <em>a</em> is
	 *         smaller than <em>b</em>.
	 */
	public int compare(final WeightedString a, final WeightedString b) {
		final double delta = a.getWeight() - b.getWeight();

		// if the weights are different, return the difference
		if (Math.abs(delta) > 0)
			return (int) Math.ceil(delta);

		// otherwise compare the strings
		return b.getString().compareTo(a.getString());
	}
}