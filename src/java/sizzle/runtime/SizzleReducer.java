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

import sizzle.aggregators.Table;
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
	 * A {@link Map} from {@link String} to {@link Table} indexing instantiated
	 */
	protected Map<String, Table> tables;

	private Configuration conf;
	private boolean robust;

	/**
	 * Construct a SizzleReducer.
	 */
	protected SizzleReducer() {
		this.tables = new HashMap<String, Table>();
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
		// get the table named by the emit key
		final Table t = this.tables.get(key.getName());
		// tell it we are not combining
		t.setCombining(false);

		// Counter counter = context.getCounter("Values Emitted",
		// key.toString());
		// LOG.fatal("counter for "+ counter.getDisplayName() + " " +
		// key.toString() + " " + Long.toString(counter.getValue()));

		// initialize the table
		t.start(key);
		// set the reducer context
		t.setContext(context);

		// for each of the values
		for (final EmitValue value : values)
			try {
				// aggregate it
				t.aggregate(value.getData(), value.getMetadata());
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
		t.finish();
	}
}
