package sizzle.types;

/**
 * A {@link SizzleType} representing an array of scalar values.
 * 
 * @author anthonyu
 * 
 */
public class SizzleVarargs extends SizzleType {
	private final SizzleType type;

	/**
	 * Construct a SizzleVarargs.
	 * 
	 * @param type
	 *            A {@link SizzleScalar} representing the type of the elements
	 *            in this array
	 */
	public SizzleVarargs(final SizzleType type) {
		this.type = type;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		// if that is a function, check its return type
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		if (this.type.equals(that))
			return true;

		if (this.type.equals(new SizzleAny()))
			return true;

		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final SizzleType that) {
		// varargs don't need to compare each other
		return false;
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

		final SizzleVarargs other = (SizzleVarargs) obj;

		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "varargs of " + this.type.toString();
	}
}
