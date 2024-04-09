package io.github.nahkd123.bukkitcommandkit;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * An annotation that can be used on your class so that BukkitCommandKit
 * annotation processor can read and generate code. The initial idea for this is
 * to speed up my plugin development, because making commands in Bukkit is
 * time-consuming apparently.
 * </p>
 * <p>
 * Make sure to check out {@link #generatedClass()} before you ask me where the
 * generated class is.
 * </p>
 * <p>
 * To listen for when player executed the command but have no permission, or the
 * caller executed the command with unknown argument, annotate your method with
 * {@link Hook} instead of {@link Subcommand}.
 * </p>
 * <p>
 * <b>About annotation processing</b>: Unlike traditional reflection-based
 * annotations reading, annotation processor performs processing during compile
 * time. All annotations are discarded once processing is completed and does not
 * present during runtime. You don't even have to shade BukkitCommandKit to your
 * plugin! (and you <i>shouldn't</i> do that either)
 * </p>
 * 
 * @see Subcommand
 * @see Sender
 * @see Hook
 * @see #generatedClass()
 * @see #permission()
 */
@Documented
@Retention(SOURCE)
@Target(TYPE)
public @interface Command {
	/**
	 * <p>
	 * The qualified name of the class that the annotation processor will generate.
	 * The default generated class name is the annotated class name, suffixed with
	 * {@code Generated}. For example, the default name for generated class if the
	 * annotated class name is {@code com.example.MyCommand} is
	 * {@code com.example.MyCommandGenerated}.
	 * </p>
	 * <p>
	 * A qualified name in Java includes both the package name and simple class
	 * name.
	 * </p>
	 * <p>
	 * The generated class implements {@code CommandExecutor} and
	 * {@code TabCompleter} by default, and you only need to use
	 * {@code Plugin#setCommand()} to have both command executor and tab completion.
	 * </p>
	 */
	public String generatedClass() default "";

	/**
	 * <p>
	 * A list of permission nodes that the caller must have to use the command
	 * (including its subcommands). If caller don't have permission, the generated
	 * code will either call your class annotated with {@link Hook} using
	 * {@link HookType#NO_PERMISSION}, or call the generated code that tell player
	 * that they don't have specific permission. Tab complete will returns nothing
	 * if player don't have permission.
	 * </p>
	 */
	public String[] permission() default {};

	/**
	 * <p>
	 * This was originally meant to be a way to include other commands as
	 * "subcommand", but I haven't implemented it, plus I don't think I am going to
	 * implement this, as this was meant to be a simple annotation so you can speed
	 * up your plugin development process, not a fully functional command framework.
	 * </p>
	 * 
	 * @deprecated Not yet implemented. Please come back later.
	 */
	@Deprecated
	public Class<?>[] includes() default {};
}
