package sizzle.types;

public class SizzleName extends SizzleType {
	private final SizzleType type;
	private final String id;

	public SizzleName(final SizzleType type, final String id) {
		this.type = type;
		this.id = id;
	}

	public SizzleName(final SizzleType type) {
		this(type, type.toString());
	}

	@Override
	public String toString() {
		return this.id + " type";
	}

	public SizzleType getType() {
		return type;
	}
}
