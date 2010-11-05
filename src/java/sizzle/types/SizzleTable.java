package sizzle.types;

import java.util.List;

/**
 * A {@link SizzleType} representing an aggregator that can be emitted to.
 * 
 * @author anthonyu
 * 
 */
public class SizzleTable extends SizzleType {
	private List<SizzleScalar> types;
	private List<String> subscripts;
	private List<SizzleScalar> indexTypes;
	private SizzleScalar weightType;

	/**
	 * Construct a SizzleTable.
	 * 
	 * @param types
	 *            A {@link List} of {@link SizzleScalar} representing the types
	 *            of this SizzleTable
	 */
	public SizzleTable(final List<SizzleScalar> types) {
		this(types, null, null, null);
	}

	/**
	 * Construct a SizzleTable.
	 * 
	 * @param types
	 *            A {@link List} of {@link SizzleScalar} representing the types
	 *            of this SizzleTable
	 * 
	 * @param subscripts
	 *            A {@link List} of {@link String} containing the names of the
	 *            subscripts of this SizzleTable
	 */
	public SizzleTable(final List<SizzleScalar> types, final List<String> subscripts) {
		this(types, subscripts, null, null);
	}

	/**
	 * Construct a SizzleTable.
	 * 
	 * @param types
	 *            A {@link List} of {@link SizzleScalar} representing the types
	 *            of this SizzleTable
	 * 
	 * @param subscripts
	 *            A {@link List} of {@link String} containing the names of the
	 *            subscripts of this SizzleTable
	 * 
	 * @param indexTypes
	 *            A {@link List} of {@link SizzleScalar} representing the index
	 *            types of this SizzleTable
	 */
	public SizzleTable(final List<SizzleScalar> types, final List<String> subscripts, final List<SizzleScalar> indexTypes) {
		this(types, subscripts, indexTypes, null);
	}

	/**
	 * Construct a SizzleTable.
	 * 
	 * @param types
	 *            A {@link List} of {@link SizzleScalar} representing the types
	 *            of this SizzleTable
	 * 
	 * @param subscripts
	 *            A {@link List} of {@link String} containing the names of the
	 *            subscripts of this SizzleTable
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
	public SizzleTable(final List<SizzleScalar> types, final List<String> subscripts, final List<SizzleScalar> indexTypes, final SizzleScalar weightType) {
		this.types = types;
		this.subscripts = subscripts;
		this.indexTypes = indexTypes;
		this.weightType = weightType;
	}

	/**
	 * Return the number of values each emit to this table will require.
	 * 
	 * @return An int containing the number of values each emit to this table
	 *         will require
	 */
	public int countTypes() {
		return this.types.size();
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
	 * Get the type of value to be emitted at that position.
	 * 
	 * @param position
	 *            An int representing the position
	 * 
	 * @return A {@link SizzleScalar} representing the type of the value to be
	 *         emitted at that position
	 * 
	 */
	public SizzleScalar getType(final int position) {
		return this.types.get(position);
	}

	/**
	 * Get the name of the subscript at that position.
	 * 
	 * @param position
	 *            An int representing the position
	 * 
	 * @return A {@link String} containing the name of the subscript at that
	 *         position
	 * 
	 */
	public String getSubscript(final int position) {
		return this.subscripts.get(position);
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
	public boolean accepts(final List<SizzleType> types) {
		// for each of the types
		for (int i = 0; i < this.types.size(); i++)
			// check if the types are equivalent
			if (!this.types.get(i).assigns(types.get(i)))
				return false;

		// they all were
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
	 * Get the types of the values to be emitted to this table.
	 * 
	 * @return A {@link List} of {@link SizzleScalar} representing the types of
	 *         the values to be emitted to this table
	 * 
	 */
	public List<SizzleScalar> getTypes() {
		return this.types;
	}

	/**
	 * Set the types of the values to be emitted to this table.
	 * 
	 * @param types
	 *            A {@link List} of {@link SizzleScalar} representing the types
	 *            of the values to be emitted to this table
	 * 
	 */
	public void setTypes(final List<SizzleScalar> types) {
		this.types = types;
	}

	/**
	 * Get the names of the subscripts to this table.
	 * 
	 * @return A {@link List} of {@link String} containing the names of the
	 *         subscripts to this table
	 * 
	 */
	public List<String> getSubscripts() {
		return this.subscripts;
	}

	/**
	 * Set the names of the subscripts to this table.
	 * 
	 * @param subscripts
	 *            A {@link List} of {@link String} containing the names of the
	 *            subscripts to this table
	 * 
	 */
	public void setSubscripts(final List<String> subscripts) {
		this.subscripts = subscripts;
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
		result = prime * result + (this.subscripts == null ? 0 : this.subscripts.hashCode());
		result = prime * result + (this.types == null ? 0 : this.types.hashCode());
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
		if (!(obj instanceof SizzleTable))
			return false;
		final SizzleTable other = (SizzleTable) obj;
		if (this.indexTypes == null) {
			if (other.indexTypes != null)
				return false;
		} else if (!this.indexTypes.equals(other.indexTypes))
			return false;
		if (this.subscripts == null) {
			if (other.subscripts != null)
				return false;
		} else if (!this.subscripts.equals(other.subscripts))
			return false;
		if (this.types == null) {
			if (other.types != null)
				return false;
		} else if (!this.types.equals(other.types))
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
		return this.getTypes() + "/" + this.subscripts + "/" + this.indexTypes + "/" + this.weightType;
	}
}
