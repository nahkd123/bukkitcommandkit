package io.github.nahkd123.bukkitcommandkit.processor.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.nahkd123.bukkitcommandkit.processor.exec.CommandExecutor;

public class RootCommandNode implements CommandNode {
	private List<CommandNode> children = new ArrayList<>();
	private Set<String> permissions = new HashSet<>();
	private List<CommandExecutor> executors = new ArrayList<>();

	@Override
	public List<CommandNode> getChildren() { return children; }

	@Override
	public Set<String> getPermissions() { return permissions; }

	@Override
	public List<CommandExecutor> getExecutors() { return executors; }
}
