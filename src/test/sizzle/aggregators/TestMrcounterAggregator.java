package sizzle.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Assert;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public class TestMrcounterAggregator {
	@Test
	public void testMrcounterAggregatorCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one", 1));
		values.add(new EmitValue("two", 2));
		values.add(new EmitValue("three", 3));
		values.add(new EmitValue("four", 4));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new MrcounterSizzleCombiner());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.runTest();
		final Counters counters = reduceDriver.getCounters();
		final CounterGroup group = counters.getGroup("test[]");

		Assert.assertEquals("counter value is wrong", 1, group.findCounter("one").getValue());
		Assert.assertEquals("counter value is wrong", 2, group.findCounter("two").getValue());
		Assert.assertEquals("counter value is wrong", 3, group.findCounter("three").getValue());
		Assert.assertEquals("counter value is wrong", 4, group.findCounter("four").getValue());
	}

	@Test
	public void testMrcounterAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one", 1));
		values.add(new EmitValue("two", 2));
		values.add(new EmitValue("three", 3));
		values.add(new EmitValue("four", 4));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new MrcounterSizzleReducer());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.runTest();
		final Counters counters = reduceDriver.getCounters();
		final CounterGroup group = counters.getGroup("test[]");

		Assert.assertEquals("counter value is wrong", 0, group.findCounter("one").getValue());
		Assert.assertEquals("counter value is wrong", 0, group.findCounter("two").getValue());
		Assert.assertEquals("counter value is wrong", 0, group.findCounter("three").getValue());
		Assert.assertEquals("counter value is wrong", 0, group.findCounter("four").getValue());
	}
}

class MrcounterSizzleCombiner extends sizzle.runtime.SizzleCombiner {
	public MrcounterSizzleCombiner() {
		super();

		this.aggregators.put("test", new sizzle.aggregators.MrcounterAggregator());
	}
}

class MrcounterSizzleReducer extends sizzle.runtime.SizzleReducer {
	public MrcounterSizzleReducer() {
		super();

		this.aggregators.put("test", new sizzle.aggregators.MrcounterAggregator());
	}
}