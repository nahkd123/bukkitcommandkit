package io.github.nahkd123.bukkitcommandkit;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Apply command arguments constraints, such as integer in a range. Not yet
 * implemented at this moment.
 * </p>
 */
@Documented
@Retention(SOURCE)
@Target(PARAMETER)
public @interface ArgConstraint {
	public double[] min() default {};

	public double[] max() default {};

	/**
	 * <p>
	 * Enum-like list of possible values to accept. Also supports tab complete.
	 * </p>
	 * 
	 * @return A list of possible values.
	 */
	public String[] permitsOnly() default {};
}
