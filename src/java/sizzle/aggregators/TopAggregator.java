package sizzle.aggregators;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import sizzle.io.EmitKey;

/**
 * A Sizzle aggregator to estimate the top <i>n</i> values in a dataset by
 * cardinality.
 * 
 * @author anthonyu
 * 
 */
@AggregatorSpec(name = "top", formalParameters = { "int" }, weightType = "int")
public class TopAggregator extends Aggregator {
	private CountingSet<String> set;
	private final CountedString[] list;
	private final int last;

	/**
	 * Construct a TopAggregator.
	 * 
	 * @param n
	 *            A long representing the number of values to return
	 */
	public TopAggregator(final long n) {
		super(n);

		// an array of weighted string of length n
		this.list = new CountedString[(int) n];
		// the index of the last entry in the list
		this.last = (int) (this.getArg() - 1);
	}

	/** {@inheritDoc} */
	@Override
	public void start(final EmitKey key) {
		super.start(key);

		this.set = new CountingSet<String>();

		// clear out the list
		for (int i = 0; i < this.getArg(); i++)
			this.list[i] = new CountedString("", Long.MIN_VALUE);
	}

	/** {@inheritDoc} */
	@Override
	public void aggregate(final String data, final String metadata) {
		if (metadata == null)
			this.set.add(data, 1);
		else
			this.set.add(data, Double.valueOf(metadata).longValue());
	}

	/** {@inheritDoc} */
	@Override
	public void finish() throws IOException, InterruptedException {
		if (this.isCombining()) {
			for (final Entry<String, Long> e : this.set.getEntries())
				this.collect(e.getKey().toString(), e.getValue().toString());
		} else {
			// TODO: replace this with the algorithm described in M. Charikar,
			// K. Chen, and M. Farach-Colton, Finding frequent items in data
			// streams, Proc 29th Intl. Colloq. on Automata, Languages and
			// Programming, 2002.

			for (final Entry<String, Long> e : this.set.getEntries()) {
				if (e.getValue() > this.list[this.last].getCount() || e.getValue() == this.list[this.last].getCount()
						&& this.list[this.last].getString().compareTo(e.getKey()) > 0) {
					// find this new item's position within the list
					for (int i = 0; i < this.getArg(); i++)
						if (e.getValue().longValue() > this.list[i].getCount() || e.getValue() == this.list[i].getCount()
								&& this.list[i].getString().compareTo(e.getKey()) > 0) {
							// here it is. move all subsequent items down one
							for (int j = (int) (this.getArg() - 2); j >= i; j--)
								this.list[j + 1] = this.list[j];

							// insert the item where it belongs
							this.list[i] = new CountedString(e.getKey(), e.getValue());

							break;
						}
				}
			}

			for (final CountedString c : this.list)
				if (c.getCount() > Long.MIN_VALUE)
					this.collect(c.toString());
		}
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
 * A tuple containing a {@link String} and its count.
 * 
 * @author anthonyu
 * 
 */
class CountedString {
	private final String string;
	private final long count;

	/**
	 * Construct a CountedString.
	 * 
	 * @param string
	 *            A {@link String} containing the string part of the tuple
	 * 
	 * @param weight
	 *            A long representing the count part of the tuple
	 */
	public CountedString(final String string, final long count) {
		super();
		this.string = string;
		this.count = count;
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
	 * Get the string part of the tuple.
	 * 
	 * @return A long representing the count part of the tuple
	 */
	public long getCount() {
		return this.count;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.string + ", " + this.count + ", 0";
	}
}

/**
 * A counting set. Like a {@link Set}, but also keeps track of how many times a
 * given member has been added.
 * 
 * @author anthonyu
 * 
 * @param <T>
 *            The type of value that will be inserted into the set
 */
class CountingSet<T> {
	private Map<T, Long> map;

	/**
	 * Construct a CountingSet.
	 */
	public CountingSet() {
		this.map = new HashMap<T, Long>();
	}

	/**
	 * Add a value to the set.
	 * 
	 * @param t
	 *            The value to be added
	 */
	public void add(final T t) {
		// add it with cardinality 1
		this.add(t, 1);
	}

	/**
	 * Add a value and its cardinality to the set.
	 * 
	 * @param t
	 *            The value to be added
	 * @param n
	 *            The cardinality of the value
	 */
	public void add(final T t, final long n) {
		// if the map already has this key, add n to the current cardiality and
		// reinsert
		if (this.map.containsKey(t))
			this.map.put(t, Long.valueOf(this.map.get(t).longValue() + n));
		else
			this.map.put(t, Long.valueOf(n));
	}

	public void clear() {
		this.map = new HashMap<T, Long>();
	}

	/**
	 * Get the entries in this set.
	 * 
	 * @return A {@link Set} of T containing the entries in this set
	 */
	public Set<java.util.Map.Entry<T, Long>> getEntries() {
		return this.map.entrySet();
	}
}

/**
 * A pair of values.
 * 
 * @author anthonyu
 * 
 * @param <F>
 *            The type of the first value
 * 
 * @param <S>
 *            The type of the second value
 */
class Pair<F, S> {
	private final F first;
	private final S second;

	/**
	 * Construct a Pair.
	 * 
	 * @param first
	 *            The first value
	 * 
	 * @param second
	 *            The second value
	 */
	public Pair(final F first, final S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Get the first value.
	 * 
	 * @return The first value
	 */
	public F getFirst() {
		return this.first;
	}

	/**
	 * Get the second value.
	 * 
	 * @return The second value
	 */
	public S getSecond() {
		return this.second;
	}
}
