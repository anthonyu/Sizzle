package sizzle.aggregators;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public class TestFloatMeanAggregator {
	@Test
	public void testFloatMeanAggregatorCombine() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(new FloatMeanSizzleCombiner()).withInput(new EmitKey("test"), values)
				.withOutput(new EmitKey("test"), new EmitValue("10.0", "4")).runTest();
	}

	@Test
	public void testFloatMeanAggregatorReduce() {
		final List<EmitValue> values = new ArrayList<EmitValue>();
		values.add(new EmitValue("1"));
		values.add(new EmitValue("2.0"));
		values.add(new EmitValue(3));
		values.add(new EmitValue(4.0));

		new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(new FloatMeanSizzleReducer()).withInput(new EmitKey("test"), values)
				.withOutput(new Text("test[] = 2.5"), NullWritable.get()).runTest();
	}
}

class FloatMeanSizzleCombiner extends sizzle.runtime.SizzleCombiner {
	public FloatMeanSizzleCombiner() {
		super();

		this.tables.put("test", new Table(new sizzle.aggregators.FloatMeanAggregator()));
	}
}

class FloatMeanSizzleReducer extends sizzle.runtime.SizzleReducer {
	public FloatMeanSizzleReducer() {
		super();

		this.tables.put("test", new Table(new sizzle.aggregators.FloatMeanAggregator()));
	}
}
