package sizzle.aggregators;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;

import sizzle.io.EmitKey;
import sizzle.io.EmitValue;

/**
 * The base class for all Sizzle aggregators.
 * 
 * @author anthonyu
 * 
 */
public abstract class Aggregator {
	private long arg;
	@SuppressWarnings("rawtypes")
	private Context context;
	private EmitKey key;
	private boolean combining;

	/**
	 * Construct an Aggregator.
	 * 
	 */
	public Aggregator() {
		// default constructor
	}

	/**
	 * Construct an Aggregator.
	 * 
	 * @param arg
	 *            A long (Sizzle int) containing the argument to the table
	 * 
	 */
	public Aggregator(final long arg) {
		this();

		this.arg = arg;
	}

	/**
	 * Reset this aggregator for a new key.
	 * 
	 * @param key
	 *            The {@link EmitKey} to aggregate for
	 * 
	 */
	public void start(final EmitKey key) {
		this.setKey(key);
	}

	public abstract void aggregate(String data, String metadata) throws IOException, InterruptedException;

	public void aggregate(final String data) throws IOException, InterruptedException {
		this.aggregate(data, null);
	}

	public void aggregate(final long data, final String metadata) throws IOException, InterruptedException {
		this.aggregate(Long.toString(data), metadata);
	}

	public void aggregate(final long data) throws IOException, InterruptedException {
		this.aggregate(data, null);
	}

	public void aggregate(final double data, final String metadata) throws IOException, InterruptedException {
		this.aggregate(Double.toString(data), metadata);
	}

	public void aggregate(final double data) throws IOException, InterruptedException {
		this.aggregate(data, null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final String data, final String metadata) throws IOException, InterruptedException {
		if (this.combining)
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
		else if (metadata != null)
			this.getContext().write(new Text(this.getKey() + " = " + data + " weight " + metadata), NullWritable.get());
		else
			this.getContext().write(new Text(this.getKey() + " = " + data), NullWritable.get());
	}

	protected void collect(final String data) throws IOException, InterruptedException {
		this.collect(data, null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final long data, final String metadata) throws IOException, InterruptedException {
		if (this.combining)
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
		else if (metadata != null)
			this.getContext().write(new Text(this.getKey() + " = " + data + " weight " + metadata), NullWritable.get());
		else
			this.getContext().write(new Text(this.getKey() + " = " + data), NullWritable.get());
	}

	protected void collect(final long data) throws IOException, InterruptedException {
		this.collect(data, null);
	}

	@SuppressWarnings("unchecked")
	protected void collect(final double data, final String metadata) throws IOException, InterruptedException {
		if (this.combining)
			this.getContext().write(this.getKey(), new EmitValue(data, metadata));
		else if (metadata != null)
			this.getContext().write(new Text(this.getKey() + " = " + data + " weight " + metadata), NullWritable.get());
		else
			this.getContext().write(new Text(this.getKey() + " = " + data), NullWritable.get());
	}

	protected void collect(final double data) throws IOException, InterruptedException {
		this.collect(data, null);
	}

	public void finish() throws IOException, InterruptedException {
		// do nothing by default
	}

	public long getArg() {
		return this.arg;
	}

	public void setContext(@SuppressWarnings("rawtypes") final Context context) {
		this.context = context;
	}

	public boolean isCombining() {
		return this.combining;
	}

	public void setCombining(final boolean combining) {
		this.combining = combining;
	}

	// these are checked at runtime by the combiner
	public boolean isAssociative() {
		return false;
	}

	public boolean isCommutative() {
		return false;
	}

	@SuppressWarnings("rawtypes")
	public Context getContext() {
		return this.context;
	}

	public void setKey(final EmitKey key) {
		this.key = key;
	}

	public EmitKey getKey() {
		return this.key;
	}
}