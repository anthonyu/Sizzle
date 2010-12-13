package sizzle.aggregators;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for one or more Sizzle aggregators.
 * 
 * @author anthonyu
 * 
 */
public class Table {
	private final List<Class<?>> aggregators;

	public Table(final Class<?> aggregator) {
		this.aggregators = new ArrayList<Class<?>>();

		this.aggregators.add(aggregator);
	}

	public Table(final List<Class<?>> aggregators) {
		this.aggregators = aggregators;
	}

	public Class<?> getAggregator(final int index) {
		return this.aggregators.get(index);
	}

	public List<Class<?>> getAggregators() {
		return this.aggregators;
	}
}
