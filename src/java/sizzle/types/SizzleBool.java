package sizzle.types;
/**
 * A {@link SizzleScalar} representing a true/false value.
 * 
 * @author anthonyu
 *
 */
public class SizzleBool extends SizzleScalar {
	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "boolean";
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "boolean";
	}
}

