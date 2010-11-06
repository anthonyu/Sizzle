package sizzle.types;

/**
 * A {@link SizzleScalar} representing a string of bytes.
 * 
 * @author anthonyu
 * 
 */
public class SizzleBytes extends SizzleScalar {
	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		return this.assigns(that);
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "byte[]";
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "bytes";
	}
}