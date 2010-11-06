package sizzle.types;

/**
 * A {@link SizzleScalar} representing a 64 bit integer value.
 * 
 * @author anthonyu
 * 
 */
public class SizzleInt extends SizzleScalar {
	/** {@inheritDoc} */
	@Override
	public SizzleScalar arithmetics(final SizzleType that) {
		// if that is a function, check its return value
		if (that instanceof SizzleFunction)
			return this.arithmetics(((SizzleFunction) that).getType());
		// otherwise, if it is an int, the type is int
		else if (that instanceof SizzleInt)
			return new SizzleInt();
		// otherwise, if it's a time, the type is time
		else if (that instanceof SizzleTime)
			return new SizzleTime();
		// otherwise if it's a float, the type is float
		else if (that instanceof SizzleFloat)
			return new SizzleFloat();

		// otherwise, check the default
		return super.arithmetics(that);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "int";
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "long";
	}
}
