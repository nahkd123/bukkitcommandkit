package io.github.nahkd123.bukkitcommandkit.processor.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.type.TypeMirror;

import io.github.nahkd123.bukkitcommandkit.ArgumentProcessor;
import io.github.nahkd123.bukkitcommandkit.processor.exec.CommandExecutor;

public class ArgumentCommandNode implements CommandNode {
    private List<CommandNode> children = new ArrayList<>();
    private Set<String> permissions = new HashSet<>();
    private List<CommandExecutor> executors = new ArrayList<>();
    private TypeMirror argumentType;
    private String label;
    private ArgumentProcessor processor;

    public ArgumentCommandNode(TypeMirror argumentType, String label, ArgumentProcessor processor) {
        this.argumentType = argumentType;
        this.label = label;
        this.processor = processor;
    }

    public TypeMirror getArgumentType() { return argumentType; }

    public String getLabel() { return label; }

    @Override
    public List<CommandNode> getChildren() { return children; }

    @Override
    public Set<String> getPermissions() { return permissions; }

    @Override
    public List<CommandExecutor> getExecutors() { return executors; }

    public ArgumentProcessor getProcessor() { return processor; }

    @Override
    public String toString() {
        return "<" + argumentType + " " + label + ">";
    }
}
