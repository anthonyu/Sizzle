package sizzle.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public class TestDistinctAggregator {
	@Test
	public void testDistinctAggregatorCombineDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new DistinctSizzleCombiner());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testDistinctAggregatorCombineIndistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new DistinctSizzleCombiner());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testDistinctAggregatorReduceDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new DistinctSizzleReducer());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new Text("test[] = one"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = two"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = three"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = four"), NullWritable.get());
		reduceDriver.runTest();
	}

	@Test
	public void testDistinctAggregatorReduceIndistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new DistinctSizzleReducer());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new Text("test[] = one"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = two"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = three"), NullWritable.get());
		reduceDriver.addOutput(new Text("test[] = four"), NullWritable.get());
		reduceDriver.runTest();
	}
}

class DistinctSizzleCombiner extends sizzle.runtime.SizzleCombiner {
	public DistinctSizzleCombiner() {
		super();

		this.tables.put("test", new Table(new sizzle.aggregators.DistinctAggregator(10000)));
	}
}

class DistinctSizzleReducer extends sizzle.runtime.SizzleReducer {
	public DistinctSizzleReducer() {
		super();

		this.tables.put("test", new Table(new sizzle.aggregators.DistinctAggregator(10000)));
	}
}