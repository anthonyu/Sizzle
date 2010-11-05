package sizzle.aggregators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * A sorted counting set. Like a SortedSet, but also keeps track of how many
 * times a given member has been added.
 * 
 * @author anthonyu
 * 
 * @param <T>
 *            The type of value that will be inserted into the set
 */
public class SortedCountingSet<T> implements Iterable<T> {
	private final TreeMap<T, Long> map;

	/**
	 * Construct a SortedCountingSet.
	 */
	public SortedCountingSet() {
		this.map = new TreeMap<T, Long>();
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

	/** {@inheritDoc} */
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private Entry<T, Long> lastEntry;
			private Entry<T, Long> thisEntry;

			private long cursor;

			{
				this.thisEntry = SortedCountingSet.this.map.firstEntry();
				this.lastEntry = SortedCountingSet.this.map.lastEntry();
				this.cursor = 0;
			}

			@Override
			public boolean hasNext() {
				if (this.lastEntry == null)
					return false;

				if (!this.thisEntry.getKey().equals(this.lastEntry.getKey()))
					return true;

				return this.cursor != this.lastEntry.getValue().longValue();
			}

			@Override
			public T next() {
				if (this.cursor == this.thisEntry.getValue().longValue()) {
					this.thisEntry = SortedCountingSet.this.map.higherEntry(this.thisEntry.getKey());
					this.cursor = 0;
				}

				this.cursor++;

				return this.thisEntry.getKey();
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Copy this set into a {@link List}.
	 * 
	 * @return A {@link List} containing the values in this set
	 */
	public List<T> toList() {
		final List<T> l = new ArrayList<T>();

		for (final T t : this)
			l.add(t);

		return l;
	}

	/**
	 * Get the entries in this set.
	 * 
	 * @return A {@link Set} of Map.Entry containing the entries in this set
	 */
	public Set<java.util.Map.Entry<T, Long>> getEntries() {
		return this.map.entrySet();
	}
}