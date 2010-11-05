package sizzle.compiler;

/**
 * An {@link Exception} thrown for type errors in Sizzle programs.
 * 
 * @author anthonyu
 * 
 */
public class TypeException extends RuntimeException {
	private static final long serialVersionUID = -5838752670934187621L;

	/**
	 * Construct a TypeException.
	 * 
	 * @param text
	 *            A {@link String} containing the description of the error
	 */
	public TypeException(final String text) {
		super(text);
	}

	/**
	 * Construct a TypeException caused by another exception.
	 * 
	 * @param text
	 *            A {@link String} containing the description of the error
	 * @param e
	 *            A {@link Throwable} representing the cause of this type
	 *            exception
	 */
	public TypeException(final String text, final Throwable e) {
		super(text, e);
	}
}
