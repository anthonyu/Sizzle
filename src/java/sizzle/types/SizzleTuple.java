package sizzle.types;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A {@link SizzleScalar} representing a data structure with named members of
 * arbitrary type.
 * 
 * @author anthonyu
 * 
 */
public class SizzleTuple extends SizzleType {
	private final List<SizzleType> members;
	private final Map<String, Integer> names;

	/**
	 * Construct a SizzleTuple.
	 * 
	 * @param members
	 *            A {@link LinkedHashMap} of {@link SizzleType} containing a
	 *            mapping from the names to the types of the members of this
	 *            tuple
	 * 
	 */
	public SizzleTuple(final List<SizzleType> members) {
		this(members, new HashMap<String, Integer>());
	}

	public SizzleTuple(final List<SizzleType> members, final Map<String, Integer> names) {
		this.members = members;
		this.names = names;
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

		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		return this.assigns(that);
	}

	/**
	 * Return the type of the member identified by a given index.
	 * 
	 * @param index
	 *            An int containing the index of the member
	 * 
	 * @return A {@link SizzleType} representing the type of the member
	 * 
	 */
	public SizzleType getMember(final int index) {
		return this.members.get(index);
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
		return this.members.get(this.names.get(member));
	}

	public List<SizzleType> getTypes() {
		return this.members;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (this.members == null ? 0 : this.members.hashCode());
		return result;
	}

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
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "tuple " + this.members.toString();
	}
}
