package sizzle.types;

import java.util.List;

/**
 * A {@link SizzleType} representing an aggregator that can be emitted to.
 * 
 * @author anthonyu
 * 
 */
public class SizzleTable extends SizzleType {
	private SizzleType type;
	private List<SizzleScalar> indexTypes;
	private SizzleScalar weightType;

	/**
	 * Construct a SizzleTable.
	 * 
	 * @param types
	 *            A {@link List} of {@link SizzleType} representing the types of
	 *            this SizzleTable
	 */
	public SizzleTable(final SizzleType type) {
		this(type, null, null);
	}

	/**
	 * Construct a SizzleTable.
	 * 
	 * @param type
	 *            A {@link SizzleType} representing the type of this SizzleTable
	 * 
	 * @param subscripts
	 *            A {@link List} of {@link String} containing the names of the
	 *            subscripts of this SizzleTable
	 * 
	 * @param indexTypes
	 *            A {@link List} of {@link SizzleScalar} representing the index
	 *            types of this SizzleTable
	 */
	public SizzleTable(final SizzleType type, final List<SizzleScalar> indexTypes) {
		this(type, indexTypes, null);
	}

	/**
	 * Construct a SizzleTable.
	 * 
	 * @param type
	 *            A {@link SizzleType} representing the type of this SizzleTable
	 * 
	 * @param indexTypes
	 *            A {@link List} of {@link SizzleScalar} representing the index
	 *            types of this SizzleTable
	 * 
	 * @param weightType
	 *            A {@link SizzleScalar} representing the weight type of this
	 *            SizzleTable
	 * 
	 */
	public SizzleTable(final SizzleType type, final List<SizzleScalar> indexTypes, final SizzleScalar weightType) {
		this.type = type;
		this.indexTypes = indexTypes;
		this.weightType = weightType;
	}

	/**
	 * Return the number of indices this table has.
	 * 
	 * @return An int containing the number of types each emit to this table
	 *         will require
	 */
	public int countIndices() {
		if (this.indexTypes == null)
			return 0;
		return this.indexTypes.size();
	}

	/**
	 * Get the type of value to be emitted to this table.
	 * 
	 * @return A {@link SizzleType} representing the type of the value to be
	 *         emitted to this table
	 * 
	 */
	public SizzleType getType() {
		return this.type;
	}

	/**
	 * Get the type of the index at that position.
	 * 
	 * @param position
	 *            An int representing the position
	 * 
	 * @return A {@link SizzleScalar} representing the type of the index at that
	 *         position
	 * 
	 */
	public SizzleScalar getIndex(final int position) {
		return this.indexTypes.get(position);
	}

	/**
	 * Returns whether this table will accept an emit of those types.
	 * 
	 * @param types
	 *            An {@link List} of {@link SizzleType} containing the types to
	 *            be emitted
	 * 
	 * @return True if this table will accept them, false otherwise
	 */
	@Override
	public boolean accepts(final SizzleType type) {
		// check if the types are equivalent
		if (!this.type.assigns(type))
			return false;

		// they were
		return true;
	}

	/**
	 * Returns whether this table will accept an weight of that type.
	 * 
	 * @param that
	 *            An {@link SizzleType} containing the weight type of the emit
	 * 
	 * @return True if this table will accept it, false otherwise
	 */
	public boolean acceptsWeight(final SizzleType that) {
		// if it's null, forget it
		if (this.weightType == null)
			return false;

		// otherwise, check if the types are equivalent
		return this.weightType.assigns(that);
	}

	/**
	 * Set the type of the values to be emitted to this table.
	 * 
	 * @param types
	 *            A {@link SizzleType} representing the type of the values to be
	 *            emitted to this table
	 * 
	 */
	public void setType(final SizzleType type) {
		this.type = type;
	}

	/**
	 * Get the types of the indices into this table.
	 * 
	 * @return A {@link List} of {@link SizzleScalar} representing the types of
	 *         the indices into this table
	 * 
	 */
	public List<SizzleScalar> getIndexTypes() {
		return this.indexTypes;
	}

	/**
	 * Set the types of the indices into this table.
	 * 
	 * @param indexTypes
	 *            A {@link List} of {@link SizzleScalar} representing the types
	 *            of the indices into this table
	 * 
	 */
	public void setIndexTypes(final List<SizzleScalar> indexTypes) {
		this.indexTypes = indexTypes;
	}

	/**
	 * Get the type of the weight of this table.
	 * 
	 * @return A {@link SizzleScalar} representing the type of the weight of
	 *         this table
	 * 
	 */
	public SizzleScalar getWeightType() {
		return this.weightType;
	}

	/**
	 * Set the type of the weight of this table.
	 * 
	 * @param weightType
	 *            A {@link SizzleScalar} representing the type of the weight of
	 *            this table
	 * 
	 */
	public void setWeightType(final SizzleScalar weightType) {
		this.weightType = weightType;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (this.indexTypes == null ? 0 : this.indexTypes.hashCode());
		result = prime * result + (this.type == null ? 0 : this.type.hashCode());
		result = prime * result + (this.weightType == null ? 0 : this.weightType.hashCode());
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
		final SizzleTable other = (SizzleTable) obj;
		if (this.indexTypes == null) {
			if (other.indexTypes != null)
				return false;
		} else if (!this.indexTypes.equals(other.indexTypes))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		if (this.weightType == null) {
			if (other.weightType != null)
				return false;
		} else if (!this.weightType.equals(other.weightType))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.getType() + "/" + this.indexTypes + "/" + this.weightType;
	}
}
