package sizzle.types;

import sizzle.compiler.TypeException;

/**
 * Base class for the types in Sizzle.
 * 
 * @author anthonyu
 * 
 */
public abstract class SizzleType {
	/**
	 * Returns the type that results from an expression of this type and an
	 * expression of that type in an arithmetic expression. (e.g. an int plus a
	 * float results in an expression of type float).
	 * 
	 * @param that
	 *            A SizzleType representing the other expression's type
	 * 
	 * @return A SizzleType representing the type of the resulting expression
	 */
	public SizzleScalar arithmetics(final SizzleType that) {
		// by default, no types are allowed in arithmetic
		throw new TypeException("incorrect type " + this + " for arithmetic with " + that);
	}

	/**
	 * Returns true when an expression of that type may be assigned to a
	 * variable of this type.
	 * 
	 * @return A boolean representing whether an expression of that type may be
	 *         assigned to a variable of this type
	 */
	public boolean assigns(final SizzleType that) {
		// by default no types can be assigned
		return false;
	}

	/**
	 * Returns true when an expression of that type may be used as a formal
	 * parameter of this type.
	 * 
	 * @return A boolean representing whether an expression of that type may be
	 *         used as a formal parameter of this type
	 */
	public boolean accepts(final SizzleType that) {
		// by default no types will be accepted
		return false;
	}

	/**
	 * Returns true when an expression of that type may be compared to an
	 * expression of this type.
	 * 
	 * @return A boolean representing whether an expression of that type may be
	 *         compared to an expression of this type
	 */
	public boolean compares(final SizzleType that) {
		// by default, no types can be compared
		return false;
	}

	/**
	 * Returns a string representation of the Java equivalent of this Sizzle
	 * type.
	 * 
	 * @return A String containing the name of the Java type equivalent to this
	 *         Sizzle type
	 */
	public String toJavaType() {
		throw new TypeException("no java equivalent for type " + this.toString());
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object that) {
		if (that == null)
			return false;

		// return whether the class names are the same
		return that.getClass().equals(this.getClass());
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return 0;
	}

	/** {@inheritDoc} */
	@Override
	public abstract String toString();
}
