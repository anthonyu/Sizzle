package sizzle.types;

import sizzle.compiler.TypeException;

/**
 * A {@link SizzleType} representing an array of scalar values.
 * 
 * @author anthonyu
 * 
 */
public class SizzleArray extends SizzleType {
	private SizzleType type;

	/**
	 * Construct a SizzleArray.
	 * 
	 * @param type
	 *            A {@link SizzleScalar} representing the type of the elements
	 *            in this array
	 */
	public SizzleArray(final SizzleScalar type) {
		this.type = type;
	}

	public SizzleArray(final SizzleAny sizzleAny) {
		this.type = sizzleAny;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		// if that is a function, check its return type
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if it's not an array, forget it
		if (!(that instanceof SizzleArray))
			return false;

		// if the element types are wrong, forget it
		if (this.type.assigns(((SizzleArray) that).type))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		// if that is a function, check its return type
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if it's not an array, forget it
		if (!(that instanceof SizzleArray))
			return false;

		// if the element types are wrong, forget it
		if (this.type.accepts(((SizzleArray) that).type))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final SizzleType that) {
		// FIXME: is this needed?
		// if that is an array..
		if (that instanceof SizzleArray)
			// check against the element types of these arrays
			return this.type.compares(((SizzleArray) that).type);

		// otherwise, forget it
		return false;
	}

	/**
	 * Get the element type of this array.
	 * 
	 * @return A {@link SizzleScalar} representing the element type of this
	 *         array
	 */
	public SizzleScalar getType() {
		if (this.type instanceof SizzleScalar)
			return (SizzleScalar) this.type;

		throw new TypeException("this shouldn't happen");
	}

	/**
	 * Set the element type of this array.
	 * 
	 * @param type
	 *            A {@link SizzleScalar} representing the element type of this
	 *            array
	 */
	public void setType(final SizzleScalar type) {
		this.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.type == null ? 0 : this.type.hashCode());
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;

		if (!super.equals(obj))
			return false;

		if (this.getClass() != obj.getClass())
			return false;

		final SizzleArray other = (SizzleArray) obj;

		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return this.type.toJavaType() + "[]";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "array of " + this.type.toString();
	}
}
