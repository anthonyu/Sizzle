package sizzle.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public class TestIntMeanAggregator {
	@Test
	public void testIntMeanAggregatorCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(new IntMeanSizzleCombiner()).withInput(new EmitKey("test"), values)
				.withOutput(new EmitKey("test"), new EmitValue("10", "4")).runTest();
	}

	@Test
	public void testIntMeanAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(new IntMeanSizzleReducer()).withInput(new EmitKey("test"), values)
				.withOutput(new Text("test[] = 2.5"), NullWritable.get()).runTest();
	}
}

class IntMeanSizzleCombiner extends sizzle.runtime.SizzleCombiner {
	public IntMeanSizzleCombiner() {
		super();

		this.aggregators.put("test", new sizzle.aggregators.IntMeanAggregator());
	}
}

class IntMeanSizzleReducer extends sizzle.runtime.SizzleReducer {
	public IntMeanSizzleReducer() {
		super();

		this.aggregators.put("test", new sizzle.aggregators.IntMeanAggregator());
	}
}
