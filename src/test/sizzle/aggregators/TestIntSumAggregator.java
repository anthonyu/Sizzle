package sizzle.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public class TestIntSumAggregator {
	@Test
	public void testIntSumAggregatorCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(new IntSumSizzleCombiner()).withInput(new EmitKey("test"), values)
				.withOutput(new EmitKey("test"), new EmitValue("10")).runTest();
	}

	@Test
	public void testIntSumAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(new IntSumSizzleReducer()).withInput(new EmitKey("test"), values)
				.withOutput(new Text("test[] = 10"), NullWritable.get()).runTest();
	}
}

class IntSumSizzleCombiner extends sizzle.runtime.SizzleCombiner {
	public IntSumSizzleCombiner() {
		super();

		this.tables.put("test", new Table(new sizzle.aggregators.IntSumAggregator()));
	}
}

class IntSumSizzleReducer extends sizzle.runtime.SizzleReducer {
	public IntSumSizzleReducer() {
		super();

		this.tables.put("test", new Table(new sizzle.aggregators.IntSumAggregator()));
	}
}
