package sizzle.types;

import java.util.Map;

/**
 * A {@link SizzleScalar} representing a data structure with named members of
 * arbitrary type.
 * 
 * @author anthonyu
 * 
 */
public class SizzleTuple extends SizzleType {
	private String name;
	private Map<String, SizzleType> members;

	/**
	 * Construct a SizzleTuple.
	 * 
	 * @param name
	 *            A {@link String} containing the name of this type
	 * 
	 * @param members
	 *            A {@link Map} of {@link SizzleType} containing a mapping from
	 *            the names to the types of the members of this tuple
	 * 
	 */
	public SizzleTuple(final String name, final Map<String, SizzleType> members) {
		this.name = name;
		this.members = members;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// have to construct it somehow
		if (that instanceof SizzleBytes)
			return true;

		if (!(that instanceof SizzleTuple))
			return false;

		return ((SizzleTuple) that).name.equals(this.name);
	}

	/**
	 * Return the type of the member identified by a given name.
	 * 
	 * @param member
	 *            A {@link String} containing the name of the member
	 * 
	 * @return A {@link SizzleType} representing the type of the member
	 * 
	 */
	public SizzleType getMember(final String member) {
		return this.members.get(member);
	}

	/**
	 * Get the name of this type.
	 * 
	 * @return A {@link String} containing the name of the type
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the name of this type.
	 * 
	 * @param name
	 *            A {@link String} containing the name of the type
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Get the members mapping for this tuple.
	 * 
	 * @return A {@link Map} or {@link SizzleType} containing the members
	 *         mapping for this tuple
	 * 
	 */
	public Map<String, SizzleType> getMembers() {
		return this.members;
	}

	/**
	 * Set the members mapping for this tuple.
	 * 
	 * @param members
	 *            A {@link Map} or {@link SizzleType} containing the members
	 *            mapping for this tuple
	 * 
	 */
	public void setMembers(final Map<String, SizzleType> members) {
		this.members = members;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "tuple " + this.name + this.members.toString();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.members == null ? 0 : this.members.hashCode());
		result = prime * result + (this.name == null ? 0 : this.name.hashCode());
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
		final SizzleTuple other = (SizzleTuple) obj;
		if (this.members == null) {
			if (other.members != null)
				return false;
		} else if (!this.members.equals(other.members))
			return false;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		return true;
	}
}
