package io.github.nahkd123.bukkitcommandkit.demo;

import org.bukkit.plugin.java.JavaPlugin;

public class MyPlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		getCommand("mycommand").setExecutor(new MyCommandGenerated(new MyCommand(this)));
	}
}
