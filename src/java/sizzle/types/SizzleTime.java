package sizzle.types;

/**
 * A {@link SizzleScalar} representing a time value in milliseconds since 1970.
 * 
 * @author anthonyu
 *
 */
public class SizzleTime extends SizzleScalar {
	/** {@inheritDoc} */
	@Override
	public SizzleScalar arithmetics(SizzleType that) {
		// if that is a function, try its return type
		if (that instanceof SizzleFunction)
			return this.arithmetics(((SizzleFunction) that).getType());
		// otherwise, if it is a time or in, the type is time
		else if (that instanceof SizzleTime || that instanceof SizzleInt)
			return new SizzleTime();

		return super.arithmetics(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "time";
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "long";
	}
}