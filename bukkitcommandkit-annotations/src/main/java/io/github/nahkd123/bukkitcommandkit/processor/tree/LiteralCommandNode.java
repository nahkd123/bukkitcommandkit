package io.github.nahkd123.bukkitcommandkit.processor.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.nahkd123.bukkitcommandkit.processor.exec.CommandExecutor;

public class LiteralCommandNode implements CommandNode {
	private String literal;
	private List<CommandNode> children = new ArrayList<>();
	private Set<String> permissions = new HashSet<>();
	private List<CommandExecutor> executors = new ArrayList<>();

	public LiteralCommandNode(String literal) {
		this.literal = literal;
	}

	public String getLiteral() { return literal; }

	@Override
	public List<CommandNode> getChildren() { return children; }

	@Override
	public Set<String> getPermissions() { return permissions; }

	@Override
	public List<CommandExecutor> getExecutors() { return executors; }

	@Override
	public String toString() {
		return literal;
	}
}
