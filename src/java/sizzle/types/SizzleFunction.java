package sizzle.types;

import java.util.Arrays;

/**
 * A {@link SizzleType} that represents a function, its return value, and its
 * formal parameters.
 * 
 * @author anthonyu
 * 
 */
public class SizzleFunction extends SizzleType {
	private String canonicalName;
	private SizzleType type;
	private SizzleType[] formalParameters;
	private String macro;

	/**
	 * Construct a SizzleFunction.
	 * 
	 * @param canonicalName
	 *            A String containing the canonical name of the Java
	 *            implementation of this function
	 * 
	 * @param type
	 *            A {@link SizzleType} representing the return type
	 * 
	 */
	public SizzleFunction(final String canonicalName, final SizzleType type) {
		this.setCanonicalName(canonicalName);
		this.type = type;
	}

	/**
	 * Construct a SizzleFunction.
	 * 
	 * @param canonicalName
	 *            A String containing the canonical name of the Java
	 *            implementation of this function
	 * 
	 * @param type
	 *            A {@link SizzleType} representing the return type
	 * 
	 * @param formalParameters
	 *            An array of {@link SizzleType} containing the type of each
	 *            formal parameter
	 * 
	 */
	public SizzleFunction(final String canonicalName, final SizzleType type, final SizzleType[] formalParameters) {
		this(canonicalName, type);

		this.formalParameters = formalParameters;
	}

	/**
	 * Construct a SizzleFunction.
	 * 
	 * @param type
	 *            A {@link SizzleType} representing the return type
	 * 
	 * @param formalParameters
	 *            An array of {@link SizzleType} containing the type of each
	 *            formal parameter
	 * 
	 */
	public SizzleFunction(final SizzleType type, final SizzleType[] formalParameters) {
		this(null, type, formalParameters);
	}

	/**
	 * Construct a SizzleFunction.
	 * 
	 * @param type
	 *            A {@link SizzleType} representing the return type
	 * 
	 * @param formalParameters
	 *            An array of {@link SizzleType} containing the type of each
	 *            formal parameter
	 * 
	 * @param macro
	 *            A snippet of Java code that can be used as a macro
	 * 
	 */
	public SizzleFunction(final SizzleType type, final SizzleType[] formalParameters, final String macro) {
		this(null, type, formalParameters);

		this.macro = macro;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean compares(final SizzleType that) {
		return this.type.compares(that);
	}

	/** {@inheritDoc} */
	@Override
	public SizzleScalar arithmetics(final SizzleType that) {
		return this.type.arithmetics(that);
	}

	/**
	 * Return the type of the parameter at a given position.
	 * 
	 * @param position
	 *            An int containing the desired position
	 * 
	 * @return A {@link SizzleType} representing the type of that parameter
	 */
	public SizzleType getParameter(final int position) {
		return this.formalParameters[position];
	}

	/**
	 * Returns the number of formal parameters for this function.
	 * 
	 * @return An int containing the number of formal parameters for this
	 *         function
	 * 
	 */
	public int countParameters() {
		return this.formalParameters.length;
	}

	/**
	 * Returns whether this function has a macro.
	 * 
	 * @return True iff this function has a macro
	 * 
	 */
	public boolean hasMacro() {
		return this.macro != null;
	}

	/**
	 * Get the canonical name of the Java implementation of this function.
	 * 
	 * @return A {@link String} containing the canonical name of the Java
	 *         implementation of this function
	 * 
	 */
	public String getCanonicalName() {
		return this.canonicalName;
	}

	/**
	 * Set the canonical name of the Java implementation of this function.
	 * 
	 * @param canonicalName
	 *            A {@link String} containing the canonical name of the Java
	 *            implementation of this function
	 * 
	 */
	public void setCanonicalName(final String canonicalName) {
		this.canonicalName = canonicalName;
	}

	/**
	 * Get the return type of this function.
	 * 
	 * @return A {@link SizzleType} representing the return type of this
	 *         function
	 * 
	 */
	public SizzleType getType() {
		return this.type;
	}

	/**
	 * Set the return type of this function.
	 * 
	 * @param type
	 *            A {@link SizzleType} representing the return type of this
	 *            function
	 * 
	 */
	public void setType(final SizzleType type) {
		this.type = type;
	}

	/**
	 * Get the types of the formal parameters of this function.
	 * 
	 * @return An array of {@link SizzleType} containing the types of the formal
	 *         arguments of this function
	 * 
	 */
	public SizzleType[] getFormalParameters() {
		return this.formalParameters;
	}

	/**
	 * Set the types of the formal parameters of this function.
	 * 
	 * @param formalArgs
	 *            An array of {@link SizzleType} containing the types of the
	 *            formal arguments of this function
	 * 
	 */
	public void setFormalParameters(final SizzleType[] formalParameters) {
		this.formalParameters = formalParameters;
	}

	public String getMacro() {
		return this.macro;
	}

	public void setMacro(final String macro) {
		this.macro = macro;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.canonicalName + " function" + Arrays.toString(this.formalParameters) + ": " + this.type.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(this.formalParameters);
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
		final SizzleFunction other = (SizzleFunction) obj;
		if (!Arrays.equals(this.formalParameters, other.formalParameters))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
