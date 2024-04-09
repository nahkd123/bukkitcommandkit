package io.github.nahkd123.bukkitcommandkit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * A method annotated with {@link Hook} will be called by the generated code
 * when certain thing happened, like invalid argument or not having permission.
 * See {@link HookType#NO_PERMISSION} and {@link HookType#UNKNOWN_ARGUMENT} for
 * hook types.
 * </p>
 * <p>
 * The annotated methods must be visible to the generated class (either add
 * {@code public} visibility modifier or keep it package-private if both
 * generated class and the base command is in the same package.
 * </p>
 * 
 * @see HookType#NO_PERMISSION
 * @see HookType#UNKNOWN_ARGUMENT
 */
@Documented
@Retention(SOURCE)
@Target(METHOD)
public @interface Hook {
	/**
	 * <p>
	 * Type of the hook.
	 * </p>
	 */
	public HookType value();
}
