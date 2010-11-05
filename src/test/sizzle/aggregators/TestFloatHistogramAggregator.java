package sizzle.aggregators;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.ReduceDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.junit.Assert;
import org.junit.Test;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public class TestFloatHistogramAggregator {
	@Test
	public void testFloatHistogramAggregatorCombine() throws IOException {
		final List<EmitValue> values = new ArrayList<EmitValue>();

		final BufferedReader r = new BufferedReader(new FileReader("test/normals"));
		try {
			String line = null;
			while ((line = r.readLine()) != null)
				values.add(new EmitValue(Long.parseLong(line)));
		} finally {
			r.close();
		}

		final ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue> reduceDriver = new ReduceDriver<EmitKey, EmitValue, EmitKey, EmitValue>(
				new FloatHistogramSizzleCombiner());
		reduceDriver.setInput(new EmitKey("test"), values);
		final List<Pair<EmitKey, EmitValue>> output = reduceDriver.run();

		Assert.assertEquals("size is wrong", 575, output.size());

		Assert.assertEquals("outputs are wrong", new EmitKey("test"), output.get(14).getFirst());
		Assert.assertEquals("outputs are wrong", new EmitValue("-292.0", 1), output.get(14).getSecond());

		Assert.assertEquals("outputs are wrong", new EmitKey("test"), output.get(256).getFirst());
		Assert.assertEquals("outputs are wrong", new EmitValue("-32.0", 42), output.get(256).getSecond());
	}

	@Test
	public void testFloatHistogramAggregator() throws IOException {
		final List<EmitValue> values = new ArrayList<EmitValue>();

		final BufferedReader r = new BufferedReader(new FileReader("test/normals"));
		try {
			String line = null;
			while ((line = r.readLine()) != null)
				values.add(new EmitValue(Long.parseLong(line)));
		} finally {
			r.close();
		}

		final ReduceDriver<EmitKey, EmitValue, Text, NullWritable> reduceDriver = new ReduceDriver<EmitKey, EmitValue, Text, NullWritable>(
				new FloatHistogramSizzleReducer());
		reduceDriver.setInput(new EmitKey("test"), values);
		reduceDriver.addOutput(new Text("test[] = [12, 211, 1284, 3444, 3455, 1396, 184, 14]"), NullWritable.get());
		reduceDriver.runTest();
	}
}

class FloatHistogramSizzleCombiner extends sizzle.runtime.SizzleCombiner {
	public FloatHistogramSizzleCombiner() {
		super();

		this.aggregators.put("test", new sizzle.aggregators.FloatHistogramAggregator(-400, 400, 8));
	}
}

class FloatHistogramSizzleReducer extends sizzle.runtime.SizzleReducer {
	public FloatHistogramSizzleReducer() {
		super();

		this.aggregators.put("test", new sizzle.aggregators.FloatHistogramAggregator(-400, 400, 8));
	}
}