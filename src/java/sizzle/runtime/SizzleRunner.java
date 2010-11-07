package sizzle.runtime;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

public abstract class SizzleRunner {
	/**
	 * Create a {@link Job} describing the work to be done by this Sizzle job.
	 * 
	 * @param ins
	 *            An array of {@link Path} containing the locations of the input
	 *            files
	 * 
	 * @param out
	 *            A {@link Path} containing the location of the output file
	 * 
	 * @param robust
	 *            A boolean representing whether the job should ignore most
	 *            exceptions
	 * 
	 * @return A {@link Job} describing the work to be done by this Sizzle job
	 * @throws IOException
	 */
	public Job job(final Path[] ins, final Path out, final boolean robust) throws IOException {
		final Configuration configuration = new Configuration();

		configuration.setBoolean("sizzle.runtime.robust", robust);

		final Job job = Job.getInstance(new Cluster(configuration), configuration);

		for (final Path in : ins)
			FileInputFormat.addInputPath(job, in);
		FileOutputFormat.setOutputPath(job, out);

		job.setMapOutputKeyClass(EmitKey.class);
		job.setMapOutputValueClass(EmitValue.class);

		// TODO: get map output compression working again

		// TODO: support protobufs/sequence files/avro here
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(NullWritable.class);

		return job;
	}

	public abstract SizzleMapper getMapper();

	public abstract SizzleCombiner getCombiner();

	public abstract SizzleReducer getReducer();
}
