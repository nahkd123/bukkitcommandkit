package io.github.nahkd123.bukkitcommandkit;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * A method with {@link Subcommand} annotation will be called by generated code
 * if the called called the command that matches with the subcommand contract
 * defined in {@link #value()}.
 * </p>
 * 
 * @see #value()
 * @see #permission()
 */
@Documented
@Retention(SOURCE)
@Target(METHOD)
public @interface Subcommand {
	/**
	 * <p>
	 * A list of command contracts. For each contract, the string is a sequence of
	 * words that follow this format: {@code <literal> | "<" <label> ">"}. The
	 * sequence of words are separated by spaces and each word must be separated by
	 * <b>exactly</b> 1 space character. For example:
	 * {@code give <player> item <item>}.
	 * </p>
	 * <p>
	 * For each word in contract, if the word starts with {@code <} and ends with
	 * {@code >}, that word indicates the argument and the string inside it is the
	 * argument label. If not, that word is a literal.
	 * </p>
	 * <p>
	 * The sequence of argument words matches with the parameters of your method
	 * respectively (except for parameter annotated with {@link Sender}, which is
	 * hidden to {@link Subcommand}. For example, if the contract is
	 * {@code give <player> item <amount>}, the method parameters can be
	 * {@code (@Sender CommandSender, Player, int)}, {@code (Player, int)},
	 * {@code (Player, @Sender CommandSender, int)} or
	 * {@code (Player, int, @Sender CommandSender)}.
	 * </p>
	 */
	public String[] value();

	/**
	 * <p>
	 * A list of permissions that player must have to use this subcommand or make it
	 * appears in tab complete candidates list. If caller don't have permission, the
	 * generated code will either call your class annotated with {@link Hook} using
	 * {@link HookType#NO_PERMISSION}, or call the generated code that tell player
	 * that they don't have specific permission.
	 * </p>
	 */
	public String[] permission() default {};

	public String[] sampleUsages() default {};
}
