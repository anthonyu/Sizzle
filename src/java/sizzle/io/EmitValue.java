package sizzle.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * A {@link Writable} that contains a datum and an optional metadatum to be
 * emitted to a Sizzle table.
 * 
 * @author anthonyu
 * 
 */
public class EmitValue implements Writable {
	String data;
	String metadata;

	/**
	 * Construct an EmitValue.
	 */
	public EmitValue() {
		// default constructor for Writable
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A {@link String} containing the data to be emitted
	 */
	public EmitValue(final String data) {
		this(data, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A {@link String} containing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final String data, final String metadata) {
		this.data = data;
		this.metadata = metadata;
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A {@link String} containing the data to be emitted
	 * @param metadata
	 *            A long representing the metadata to be emitted
	 */
	public EmitValue(final String data, final long metadata) {
		this(data, Long.toString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A {@link String} containing the data to be emitted
	 * @param metadata
	 *            A double representing the metadata to be emitted
	 */
	public EmitValue(final String data, final double metadata) {
		this(data, Double.toString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A long representing the data to be emitted
	 */
	public EmitValue(final long data) {
		this(data, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A long representing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final long data, final String metadata) {
		this.data = Long.toString(data);
		this.metadata = metadata;
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A long representing the data to be emitted
	 * @param metadata
	 *            A long representing the metadata to be emitted
	 */
	public EmitValue(final long data, final long metadata) {
		this(data, Long.toString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A long representing the data to be emitted
	 * @param metadata
	 *            A double representing the metadata to be emitted
	 */
	public EmitValue(final long data, final double metadata) {
		this(data, Double.toString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A double representing the data to be emitted
	 */
	public EmitValue(final double data) {
		this(data, null);
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A double representing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final double data, final String metadata) {
		this.data = Double.toString(data);
		this.metadata = metadata;
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A double representing the data to be emitted
	 * @param metadata
	 *            A long representing the metadata to be emitted
	 */
	public EmitValue(final double data, final long metadata) {
		this(data, Long.toString(metadata));
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            A double representing the data to be emitted
	 * @param metadata
	 *            A double representing the metadata to be emitted
	 */
	public EmitValue(final double data, final double metadata) {
		this(data, Double.toString(metadata));
	}

	/** {@inheritDoc} */
	@Override
	public void readFields(final DataInput in) throws IOException {
		this.data = Text.readString(in);
		final String metadata = Text.readString(in);
		if (metadata.equals(""))
			this.metadata = null;
		else
			this.metadata = metadata;
	}

	/** {@inheritDoc} */
	@Override
	public void write(final DataOutput out) throws IOException {
		Text.writeString(out, this.data);
		if (this.metadata == null)
			Text.writeString(out, "");
		else
			Text.writeString(out, this.metadata);
	}

	/**
	 * @return the data
	 */
	public String getData() {
		return this.data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(final String data) {
		this.data = data;
	}

	/**
	 * @return the metadata
	 */
	public String getMetadata() {
		return this.metadata;
	}

	/**
	 * @param metadata
	 *            the metadatum to set
	 */
	public void setMetadata(final String metadata) {
		this.metadata = metadata;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.metadata == null ? 0 : this.metadata.hashCode());
		result = prime * result + (this.data == null ? 0 : this.data.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EmitValue))
			return false;
		final EmitValue other = (EmitValue) obj;
		if (this.metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!this.metadata.equals(other.metadata))
			return false;
		if (this.data == null) {
			if (other.data != null)
				return false;
		} else if (!this.data.equals(other.data))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.data + ":" + this.metadata;
	}
}
