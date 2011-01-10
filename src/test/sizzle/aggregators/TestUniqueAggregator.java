package sizzle.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public class TestUniqueAggregator {
	@Test
	public void testUniqueAggregatorCombineDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new UniqueSizzleCombiner());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testUniqueAggregatorCombineIndistinct() {
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
				new UniqueSizzleCombiner());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("one"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("two"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("three"));
		reduceDriver.addOutput(new EmitKey("test"), new EmitValue("four"));
		reduceDriver.runTest();
	}

	@Test
	public void testUniqueAggregatorReduceDistinct() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("one"));
		values.add(new EmitValue("two"));
		values.add(new EmitValue("three"));
		values.add(new EmitValue("four"));

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new UniqueSizzleReducer());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new Text("test[] = 4"), NullWritable.get());
		reduceDriver.runTest();
	}

	@Test
	public void testUniqueAggregatorReduceIndistinct() {
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
				new UniqueSizzleReducer());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new Text("test[] = 4"), NullWritable.get());
		reduceDriver.runTest();
	}
}

class UniqueSizzleCombiner extends sizzle.runtime.SizzleCombiner {
	public UniqueSizzleCombiner() {
		super();

		this.tables.put("test", new Table(new sizzle.aggregators.UniqueAggregator(10000)));
	}
}

class UniqueSizzleReducer extends sizzle.runtime.SizzleReducer {
	public UniqueSizzleReducer() {
		super();

		this.tables.put("test", new Table(new sizzle.aggregators.UniqueAggregator(10000)));
	}
}