package io.github.nahkd123.bukkitcommandkit.processor.provided;

import java.util.Map;

public class ProvidedArgumentMethods {
	public static final Map<String, String> METHODS = Map.ofEntries(
		Map.entry("org.bukkit.entity.Player", """
			org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(args[argStart.get()]);
			if (player != null) argStart.incrementAndGet();
			return player;
			"""));
}
