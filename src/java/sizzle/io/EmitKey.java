package sizzle.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * A {@link WritableComparable} that contains a low resolution key which is the
 * name of the table this value is being emitted to, and a high resolution key
 * which is an index into that table.
 * 
 * @author anthonyu
 * 
 */
public class EmitKey implements WritableComparable<EmitKey>, RawComparator<EmitKey>, Serializable {
	private static final long serialVersionUID = -6302400030199718829L;

	private String index;
	private String name;

	/**
	 * Construct an EmitKey.
	 * 
	 */
	public EmitKey() {
		// default constructor for Writable
	}

	/**
	 * Construct an EmitKey.
	 * 
	 * @param name
	 *            A {@link String} containing the name of the table this was
	 *            emitted to
	 * 
	 */
	public EmitKey(final String name) {
		this("[]", name);
	}

	/**
	 * Construct an EmitKey.
	 * 
	 * @param index
	 *            A {@link String} containing the index into the table this was
	 *            emitted to
	 * 
	 * @param name
	 *            A {@link String} containing the name of the table this was
	 *            emitted to
	 * 
	 */
	public EmitKey(final String index, final String name) {
		if (index.equals(""))
			throw new RuntimeException();

		this.index = index;
		this.name = name;
	}

	/** {@inheritDoc} */
	@Override
	public void readFields(final DataInput in) throws IOException {
		this.index = Text.readString(in);
		this.name = Text.readString(in);
	}

	/** {@inheritDoc} */
	@Override
	public void write(final DataOutput out) throws IOException {
		Text.writeString(out, this.index);
		Text.writeString(out, this.name);
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final byte[] b1, final int s1, final int l1, final byte[] b2, final int s2, final int l2) {
		return WritableComparator.compareBytes(b1, s1, l1, b2, s2, l2);
	}

	/** {@inheritDoc} */
	@Override
	public int compare(final EmitKey k1, final EmitKey k2) {
		return k1.compareTo(k2);
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final EmitKey that) {
		// compare the names
		final int c = this.name.compareTo(that.name);

		// if the names are different
		if (c != 0)
			// return that difference
			return c;
		else
			// otherwise compare the indices
			return this.index.compareTo(that.index);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.index == null ? 0 : this.index.hashCode());
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		final EmitKey other = (EmitKey) obj;
		if (this.index == null) {
			if (other.index != null)
				return false;
		} else if (!this.index.equals(other.index))
			return false;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * Get the index into the table this key was emitted to.
	 * 
	 * @return A {@link String} containing the index into the table this key was
	 *         emitted to
	 */
	public String getIndex() {
		return this.index;
	}

	/**
	 * Get the index into the table this key was emitted to.
	 * 
	 * @param index
	 *            A {@link String} containing the index into the table this key
	 *            was emitted to
	 */
	public void setIndex(final String index) {
		this.index = index;
	}

	/**
	 * Get the name of the table this key was emitted to.
	 * 
	 * @return A {@link String} containing the name of the table this key was
	 *         emitted to
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the name of the table this key was emitted to.
	 * 
	 * @param name
	 *            A {@link String} containing the name of the table this key was
	 *            emitted to
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.name + this.index;
	}
}
