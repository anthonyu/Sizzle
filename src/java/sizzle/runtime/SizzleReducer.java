package sizzle.runtime;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import sizzle.aggregators.Aggregator;
import sizzle.aggregators.FinishedException;
import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

/**
 * A {@link Reducer} that reduces the outputs for a single {@link EmitKey}.
 * 
 * @author anthonyu
 * 
 */
public abstract class SizzleReducer extends Reducer<EmitKey, EmitValue, Text, NullWritable> implements Configurable {
	/**
	 * A {@link Logger} that log entries can be written to.
	 * 
	 */
	protected static final Logger LOG = Logger.getLogger(SizzleReducer.class);

	/**
	 * A {@link Map} from {@link String} to {@link Aggregator} indexing
	 * instantiated aggregators to their Sizzle identifiers.
	 */
	protected Map<String, Aggregator> aggregators;

	private Configuration conf;
	private boolean robust;

	/**
	 * Construct a SizzleReducer.
	 */
	protected SizzleReducer() {
		this.aggregators = new HashMap<String, Aggregator>();
	}

	/** {@inheritDoc} */
	@Override
	public Configuration getConf() {
		return this.conf;
	}

	/** {@inheritDoc} */
	@Override
	public void setConf(final Configuration conf) {
		this.conf = conf;
		this.robust = conf.getBoolean("sizzle.runtime.robust", false);
	}

	/** {@inheritDoc} */
	@Override
	protected void reduce(final EmitKey key, final Iterable<EmitValue> values, final Context context) throws IOException, InterruptedException {
		// get the aggregator named by the emit key
		final Aggregator a = this.aggregators.get(key.getName());
		// tell it we are not combining
		a.setCombining(false);

		// Counter counter = context.getCounter("Values Emitted",
		// key.toString());
		// LOG.fatal("counter for "+ counter.getDisplayName() + " " +
		// key.toString() + " " + Long.toString(counter.getValue()));

		// initialize the aggregator
		a.start(key);
		// set the reducer context
		a.setContext(context);

		// for each of the values
		for (final EmitValue value : values)
			try {
				// aggregate it
				a.aggregate(value.getData(), value.getMetadata());
			} catch (final FinishedException e) {
				// we are done
				return;
			} catch (final IOException e) {
				// won't be robust to IOException
				throw e;
			} catch (final InterruptedException e) {
				// won't be robust to InterruptedExceptions
				throw e;
			} catch (final RuntimeException e) {
				if (this.robust)
					SizzleReducer.LOG.error(e.getClass().getName() + " caught", e);
				else
					throw e;
			} catch (final Exception e) {
				if (this.robust)
					SizzleReducer.LOG.error(e.getClass().getName() + " caught", e);
				else
					throw new RuntimeException(e.getClass().getName() + " caught", e);
			}

		// finish it!
		a.finish();
	}
}
