package sizzle.types;

/**
 * A {@link SizzleType} representing a mapping a set of keys to some value.
 * 
 * @author anthonyu
 * 
 */
public class SizzleMap extends SizzleType {
	private final SizzleType type;
	private final SizzleType indexType;

	/**
	 * Construct a SizzleMap.
	 */
	public SizzleMap() {
		this(null, null);
	}

	/**
	 * Construct a SizzleMap.
	 * 
	 * @param sizzleType
	 *            A {@link SizzleType} representing the type of the values in
	 *            this map
	 * 
	 * @param sizzleType2
	 *            A {@link SizzleType} representing the type of the indices in
	 *            this map
	 */
	public SizzleMap(final SizzleType sizzleType, final SizzleType sizzleType2) {
		this.type = sizzleType;
		this.indexType = sizzleType2;
	}

	/** {@inheritDoc} */
	@Override
	public boolean assigns(final SizzleType that) {
		// if that is a function, check the return value
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if that is not a map, forget it
		if (!(that instanceof SizzleMap))
			return false;

		// if that index type is not equivalent this this's, forget it
		if (!((SizzleMap) that).indexType.assigns(this.indexType))
			return false;

		// same for the value type
		if (!((SizzleMap) that).type.assigns(this.type))
			return false;

		// ok
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean accepts(final SizzleType that) {
		// if that is a function, check the return value
		if (that instanceof SizzleFunction)
			return this.assigns(((SizzleFunction) that).getType());

		// otherwise, if that is not a map, forget it
		if (!(that instanceof SizzleMap))
			return false;

		// if that index type is not equivalent this this's, forget it
		if (!((SizzleMap) that).indexType.accepts(this.indexType))
			return false;

		// same for the value type
		if (!((SizzleMap) that).type.accepts(this.type))
			return false;

		// ok
		return true;
	}

	/**
	 * Get the type of the values of this map.
	 * 
	 * @return A {@link SizzleType} representing the type of the values of this
	 *         map
	 */
	public SizzleType getType() {
		return this.type;
	}

	/**
	 * Get the type of the indices of this map.
	 * 
	 * @return A {@link SizzleType} representing the type of the indices of this
	 *         map
	 */
	public SizzleType getIndexType() {
		return this.indexType;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "map[" + this.indexType + "] of " + this.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toJavaType() {
		return "java.util.HashMap<" + this.indexType.toJavaType() + ", " + this.type.toJavaType() + ">";
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.indexType == null ? 0 : this.indexType.hashCode());
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
		final SizzleMap other = (SizzleMap) obj;
		if (this.indexType == null) {
			if (other.indexType != null)
				return false;
		} else if (!this.indexType.equals(other.indexType))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}
}
