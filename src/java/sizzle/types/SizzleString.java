package sizzle.types;

/**
 * A {@link SizzleScalar} representing a string of characters.
 * 
 * @author anthonyu
 *
 */
public class SizzleString extends SizzleScalar {
	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "String";
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "string";
	}
}
