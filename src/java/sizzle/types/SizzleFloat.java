package sizzle.types;

/**
 * A {@link SizzleScalar} representing an double precision floating point value.
 * 
 * @author anthonyu
 * 
 */
public class SizzleFloat extends SizzleScalar {
	/** {@inheritDoc} */
	@Override
	public SizzleScalar arithmetics(SizzleType that) {
		// if that is a function, check its return type
		if (that instanceof SizzleFunction)
			return this.arithmetics(((SizzleFunction) that).getType());

		// if it's a float, the type is float
		if (that instanceof SizzleFloat)
			return new SizzleFloat();

		// same with ints
		if (that instanceof SizzleInt)
			return new SizzleFloat();

		return super.arithmetics(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(SizzleType that) {
		// ints can be assigned to floats
		if (that instanceof SizzleInt)
			return true;

		// otherwise, just check the defaults
		return super.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "double";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "float";
	}
}
