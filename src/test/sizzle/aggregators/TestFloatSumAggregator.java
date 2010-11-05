package sizzle.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public class TestFloatSumAggregator {
	@Test
	public void testFloatSumAggregatorFloatsCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(new FloatSumSizzleCombiner()).withInput(new EmitKey("test"), values)
				.withOutput(new EmitKey("test"), new EmitValue("10.0")).runTest();
	}

	@Test
	public void testFloatSumAggregatorFloatsReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(new FloatSumSizzleReducer()).withInput(new EmitKey("test"), values)
				.withOutput(new Text("test[] = 10.0"), NullWritable.get()).runTest();
	}
}

class FloatSumSizzleCombiner extends sizzle.runtime.SizzleCombiner {
	public FloatSumSizzleCombiner() {
		super();

		this.aggregators.put("test", new sizzle.aggregators.FloatSumAggregator());
	}
}

class FloatSumSizzleReducer extends sizzle.runtime.SizzleReducer {
	public FloatSumSizzleReducer() {
		super();

		this.aggregators.put("test", new sizzle.aggregators.FloatSumAggregator());
	}
}