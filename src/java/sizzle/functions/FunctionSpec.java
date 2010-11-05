package sizzle.functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specification annotation for Sizzle functions in Java.
 * 
 * @author anthonyu
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FunctionSpec {
	/**
	 * The name of the function.
	 * 
	 */
	String name();

	/**
	 * The Sizzle type of its return value.
	 * 
	 */
	String returnType() default "none";

	/**
	 * The Sizzle types of each of its formal parameters.
	 * 
	 */
	String[] formalParameters() default {};

	/**
	 * Any type dependencies that need to be handled prior to importing this
	 * function.
	 * 
	 */
	String[] typeDependencies() default {};
}
