package sizzle.types;

/**
 * A {@link SizzleType} representing the wildcard or any type.
 * 
 * @author anthonyu
 * 
 */
public class SizzleAny extends SizzleType {
	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		// anything can be assigned to a variable of type 'any'
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "any";
	}
}