package io.github.nahkd123.bukkitcommandkit;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * A method parameter that is annotated with {@link Sender} will be marked as
 * command sender. The parameter's type must be a subclass of
 * {@code org.bukkit.command.CommandSender} interface, like {@code Player} for
 * example.
 * </p>
 * <p>
 * If the parameter is annotated with {@link Sender}, the parameter will become
 * invisible in the command contract defined in {@link Subcommand#value()},
 * because the parameter is not part of the command.
 * </p>
 * <p>
 * A subcommand may be defined multiple times with different sender type, so
 * that you can perform custom logic if the command is not executed by player.
 * </p>
 */
@Documented
@Retention(SOURCE)
@Target(PARAMETER)
public @interface Sender {
}
