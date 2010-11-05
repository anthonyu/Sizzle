package sizzle.aggregators;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specification annotation for Sizzle aggregators in Java.
 * 
 * @author anthonyu
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AggregatorSpec {
	/**
	 * The name of the aggregator.
	 */
	String name();

	/**
	 * The Sizzle type to be emitted to this aggregator. Defaults to "any",
	 * meaning it accepts all types.
	 */
	String type() default "any";

	/**
	 * The Sizzle types of each of its formal parameters.
	 */
	String[] formalParameters() default {};

	/**
	 * The Sizzle type that emits to this table will be weighted by. Defaults to
	 * "none", meaning that it accepts no weights.
	 */
	String weightType() default "none";
}
