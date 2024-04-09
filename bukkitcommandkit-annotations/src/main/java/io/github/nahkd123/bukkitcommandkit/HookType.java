package io.github.nahkd123.bukkitcommandkit;

/**
 * <p>
 * See {@link Hook} for more information.
 * </p>
 */
public enum HookType {
	/**
	 * <p>
	 * The generated code will call your hook if player doesn't have permission.
	 * </p>
	 * <p>
	 * <b>Parameters</b>: {@code org.bukkit.command.CommandSender} and
	 * {@link String}, respectively, with the first parameter for sender that
	 * executed the command, and the second parameter for the permission that player
	 * is missing.
	 * </p>
	 */
	NO_PERMISSION,

	/**
	 * <p>
	 * The generated code will call your hook if unknown argument is found (a.k.a
	 * can't be parsed and no such literal found.
	 * </p>
	 * <p>
	 * <b>Parameters</b>: {@code org.bukkit.command.CommandSender}, {@code String[]}
	 * and {@code int}, respectively. The first parameter is the sender that
	 * executed the command, the second parameter is the input arguments, and the
	 * third parameter is the index of the argument that the generated code can't
	 * parse.
	 * </p>
	 */
	UNKNOWN_ARGUMENT;
}
