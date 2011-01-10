package sizzle.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

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
	private String[] data;
	private String metadata;

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
	 *            An array of {@link String} containing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final String[] data, final String metadata) {
		this.data = data;
		this.metadata = metadata;
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            An array of {@link String} containing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final Object[] data, final String metadata) {
		final String[] strings = new String[data.length];

		for (int i = 0; i < data.length; i++)
			strings[i] = data[i].toString();

		this.data = strings;
		this.metadata = metadata;
	}

	/**
	 * Construct an EmitValue.
	 * 
	 * @param data
	 *            An array of {@link String} containing the data to be emitted
	 * @param metadata
	 *            A {@link String} containing the metadata to be emitted
	 */
	public EmitValue(final Object[] data) {
		this(data, null);
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
		this(new String[] { data }, metadata);
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
		this(Long.toString(data), metadata);
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
		this(Double.toString(data), metadata);
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
		final int count = in.readInt();

		this.data = new String[count];
		for (int i = 0; i < count; i++)
			this.data[i] = Text.readString(in);

		final String metadata = Text.readString(in);
		if (metadata.equals(""))
			this.metadata = null;
		else
			this.metadata = metadata;
	}

	/** {@inheritDoc} */
	@Override
	public void write(final DataOutput out) throws IOException {
		out.writeInt(this.data.length);

		for (final String d : this.data)
			Text.writeString(out, d);

		if (this.metadata == null)
			Text.writeString(out, "");
		else
			Text.writeString(out, this.metadata);
	}

	/**
	 * @return the data
	 */
	public String[] getData() {
		return this.data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(final String[] data) {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.data);
		result = prime * result + (this.metadata == null ? 0 : this.metadata.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final EmitValue other = (EmitValue) obj;
		if (!Arrays.equals(this.data, other.data))
			return false;
		if (this.metadata == null) {
			if (other.metadata != null)
				return false;
		} else if (!this.metadata.equals(other.metadata))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Arrays.toString(this.data) + ":" + this.metadata;
	}
}
