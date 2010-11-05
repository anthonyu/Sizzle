package sizzle.aggregators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import sizzle.aggregators.SortedCountingSet;

public class TestSortedCountingSet {
	@Test
	public void testSortedCountingSetEmpty() {
		final SortedCountingSet<String> s = new SortedCountingSet<String>();

		Assert.assertEquals("set is wrong", new ArrayList<String>(), s.toList());
	}

	@Test
	public void testSortedCountingSetSingle() {
		final SortedCountingSet<String> s = new SortedCountingSet<String>();

		s.add("test");

		Assert.assertEquals("set is wrong", Arrays.asList("test"), s.toList());
	}

	@Test
	public void testSortedCountingSetDuplicate() {
		final SortedCountingSet<String> s = new SortedCountingSet<String>();

		s.add("test");
		s.add("test");
		s.add("test");
		s.add("test");
		s.add("test");
		s.add("test");

		Assert.assertEquals("set is wrong", Arrays.asList("test", "test", "test", "test", "test", "test"), s.toList());
	}

	@Test
	public void testSortedCountingSetSorted() {
		final SortedCountingSet<String> s = new SortedCountingSet<String>();

		s.add("test1");
		s.add("test1");
		s.add("test2");
		s.add("test2");
		s.add("test3");
		s.add("test3");

		Assert.assertEquals("set is wrong", Arrays.asList("test1", "test1", "test2", "test2", "test3", "test3"), s.toList());
	}

	@Test
	public void testSortedCountingSetUnsorted() {
		final SortedCountingSet<String> s = new SortedCountingSet<String>();

		s.add("test3");
		s.add("test1");
		s.add("test2");
		s.add("test1");
		s.add("test3");
		s.add("test2");

		Assert.assertEquals("set is wrong", Arrays.asList("test1", "test1", "test2", "test2", "test3", "test3"), s.toList());
	}

	@Test
	public void testSortedCountingSetBig() {
		final SortedCountingSet<String> s = new SortedCountingSet<String>();
		final List<String> expected = new ArrayList<String>();

		s.add("first");
		expected.add("first");

		for (int i = 0; i < 18; i++) {
			s.add("sawzall");
			expected.add("sawzall");
		}

		for (int i = 0; i < 27; i++) {
			s.add("salsa");
			expected.add("salsa");
		}

		s.add("java");
		expected.add("java");

		for (int i = 0; i < 53; i++) {
			s.add("sizzle");
			expected.add("sizzle");
		}

		s.add("last");
		expected.add("last");

		Collections.sort(expected);

		Assert.assertEquals("set is wrong", expected, s.toList());
	}
}
