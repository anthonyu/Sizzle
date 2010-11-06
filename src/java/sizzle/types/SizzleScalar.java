package sizzle.types;

/**
 * A {@link SizzleType} representing any other scalar value type.
 * 
 * @author anthonyu
 * 
 */
public class SizzleScalar extends SizzleType {
	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		// if that is a function, check the return type
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if it's not a scalar, forget it
		if (!(that instanceof SizzleScalar))
			return false;

		// check that the classes match
		return this.getClass().equals(that.getClass());
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		// if that is a function, check the return type
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if it's not a scalar, forget it
		if (!(that instanceof SizzleScalar))
			return false;

		// check that the classes match
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final SizzleType that) {
		// if that is a function, check the return type
		if (that instanceof SizzleFunction)
			return this.compares(((SizzleFunction) that).getType());

		// otherwise, check if the types are equivalent one way or the other
		if (this.assigns(that) || that.assigns(this))
			return true;

		// forget it
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null)
			return false;

		return this.getClass().equals(obj.getClass());
	}

	@Override
	public String toString() {
		return "scalar";
	}
}
