package io.github.nahkd123.bukkitcommandkit.processor.provided;

import java.util.Map;

public class ProvidedTabCompleteMethods {
	// Method signature: (String[] args, AtomicInteger argStart, Consumer<String>
	// suggest)

	public static final Map<String, String> METHODS = Map.ofEntries(
		Map.entry("org.bukkit.entity.Player", """
			org.bukkit.Bukkit.getOnlinePlayers().stream()
			.map(p -> p.getName())
			.filter(n -> n.startsWith(args[argStart.get()]))
			.forEach(suggest);
			"""));
}
