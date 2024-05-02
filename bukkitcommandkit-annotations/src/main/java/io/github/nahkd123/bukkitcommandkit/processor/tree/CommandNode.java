package io.github.nahkd123.bukkitcommandkit.processor.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.lang.model.type.PrimitiveType;

import io.github.nahkd123.bukkitcommandkit.processor.CommandInfo;
import io.github.nahkd123.bukkitcommandkit.processor.exec.CommandExecutor;
import io.github.nahkd123.bukkitcommandkit.processor.subcommand.SubcommandArgumentSegment;
import io.github.nahkd123.bukkitcommandkit.processor.subcommand.SubcommandLiteralSegment;
import io.github.nahkd123.bukkitcommandkit.processor.util.BukkitTypes;
import io.github.nahkd123.bukkitcommandkit.processor.util.Streams;

public interface CommandNode {
	public List<CommandNode> getChildren();

	/**
	 * <p>
	 * A set of permissions that the command sender must have. Limited to Bukkit
	 * permission nodes for now.
	 * </p>
	 * 
	 * @return A set of permission nodes.
	 */
	public Set<String> getPermissions();

	public List<CommandExecutor> getExecutors();

	default LiteralCommandNode walk(SubcommandLiteralSegment segment) {
		for (CommandNode child : getChildren()) {
			if (child instanceof LiteralCommandNode literal && literal.getLiteral().equals(segment.literal())) {
				return literal;
			}
		}

		LiteralCommandNode node = new LiteralCommandNode(segment.literal());
		getChildren().add(node);
		return node;
	}

	default ArgumentCommandNode walk(SubcommandArgumentSegment segment) {
		for (CommandNode child : getChildren()) {
			if (child instanceof ArgumentCommandNode arg && arg.getArgumentType().equals(segment.parameterType())) {
				return arg;
			}
		}

		ArgumentCommandNode node = new ArgumentCommandNode(segment.parameterType(), segment.label());
		getChildren().add(node);
		return node;
	}

	default Stream<String> generateOnCommandCode(CommandInfo command, List<String> args, AtomicInteger localsCounter) {
		String indent = command.indent;
		List<String> head = new ArrayList<>();

		for (String perm : getPermissions()) {
			head.add("if (!caller.hasPermission(\"" + perm + "\")) {"
				+ command.getNoPermissionMethod() + "(caller, \"" + perm + "\"); "
				+ "return true; "
				+ "}");
		}

		List<CommandExecutor> sortedExecutors = new ArrayList<>();
		sortedExecutors.addAll(getExecutors());
		Collections.sort(sortedExecutors, (a, b) -> {
			if (a.callerType() == null && b.callerType() == null) return 0;
			if (a.callerType() == null) return 1;
			if (b.callerType() == null) return -1;
			if (a.callerType().getQualifiedName().toString().equals(BukkitTypes.COMMAND_SENDER)) return 1;
			if (b.callerType().getQualifiedName().toString().equals(BukkitTypes.COMMAND_SENDER)) return -1;
			return 0;
		});

		// Execute
		head.add("if ($start.get() == $.length) {");

		for (CommandExecutor executor : sortedExecutors) {
			if (executor.callerType() == null ||
				executor.callerType().getQualifiedName().toString().equals(BukkitTypes.COMMAND_SENDER)) {
				head.add(indent
					+ "command." + executor.method().getSimpleName().toString() + "("
					+ executor.generateMethodInputs("caller", args)
					+ ");");
			} else {
				head.add(indent
					+ "if (caller instanceof " + executor.callerType().getQualifiedName() + " caller$casted) { "
					+ "command." + executor.method().getSimpleName().toString() + "("
					+ executor.generateMethodInputs("caller$casted", args)
					+ "); "
					+ "return true; "
					+ "}");
			}
		}

		// TODO show help docs?
		head.add("    return true;");
		head.add("}");

		// Child nodes
		Stream<String> stream = head.stream();

		for (CommandNode child : getChildren()) {
			if (child instanceof LiteralCommandNode literalNode) {
				stream = Streams.multiconcat(
					stream,
					Stream.of(
						"if ($[$start.get()].equals(\"" + literalNode.getLiteral() + "\")) {",
						indent + "$start.incrementAndGet();"),
					child.generateOnCommandCode(command, args, localsCounter).map(s -> indent + s),
					Stream.of(
						indent + "return true;",
						"}"));
			}

			if (child instanceof ArgumentCommandNode argumentNode) {
				List<String> newArgs = new ArrayList<>();
				String variableName = "$" + localsCounter.getAndIncrement();
				newArgs.addAll(args);
				newArgs.add(variableName);

				String argTypeAsStr = argumentNode.getArgumentType() instanceof PrimitiveType prim
					? command.processingEnv.getTypeUtils().boxedClass(prim).getQualifiedName().toString()
					: argumentNode.getArgumentType().toString();

				stream = Streams.multiconcat(
					stream,
					Stream.of(
						argTypeAsStr + " " + variableName + " = "
							+ command.getParseMethodFor(argumentNode.getArgumentType())
							+ "($, $start);",
						"if (" + variableName + " != null) {"),
					child.generateOnCommandCode(command, newArgs, localsCounter).map(s -> indent + s),
					Stream.of(
						indent + "return true;",
						"}"));
			}
		}

		stream = Stream.concat(
			stream,
			Stream.of(command.getUnknownParameterMethod() + "(caller, $, $start.get());"));
		return stream;
	}

	default Stream<String> generateTabCompleteCode(CommandInfo command, List<String> args, AtomicInteger localsCounter) {
		String indent = command.indent;
		List<String> head = new ArrayList<>();
		for (String perm : getPermissions())
			head.add("if (!caller.hasPermission(\"" + perm + "\")) return java.util.Collections.emptyList();");
		Stream<String> stream = head.stream();

		// Candidates at current
		List<String> candidates = new ArrayList<>();
		List<String> tabCompleteMethods = new ArrayList<>();

		for (CommandNode child : getChildren()) {
			if (child instanceof LiteralCommandNode literalNode) candidates.add(literalNode.getLiteral());
			if (child instanceof ArgumentCommandNode argumentNode)
				tabCompleteMethods.add(command.getTabCompleteMethodFor(argumentNode.getArgumentType()));
		}

		if ((candidates.size() + tabCompleteMethods.size()) > 0) {
			stream = Streams.multiconcat(
				stream,
				Stream.of(
					"if ($start.get() == $.length - 1) {",
					indent + "java.util.List<String> candidates = new java.util.ArrayList<>();"),
				candidates.stream().map(c -> indent + "candidates.add(\"" + c + "\");"),
				tabCompleteMethods.stream().map(m -> indent + m + "($, $start, candidates::add);"),
				Stream.of(
					indent
						+ "return candidates.stream().filter(s -> s.startsWith($[$start.get()])).collect(java.util.stream.Collectors.toList());",
					"}"));
		}

		// Child nodes
		for (CommandNode child : getChildren()) {
			if (child instanceof LiteralCommandNode literalNode) {
				stream = Streams.multiconcat(
					stream,
					Stream.of(
						"if ($[$start.get()].equals(\"" + literalNode.getLiteral() + "\")) {",
						indent + "$start.incrementAndGet();"),
					child.generateTabCompleteCode(command, args, localsCounter).map(s -> indent + s),
					Stream.of(
						indent + "return java.util.Collections.emptyList();",
						"}"));
			}

			if (child instanceof ArgumentCommandNode argumentNode) {
				List<String> newArgs = new ArrayList<>();
				String variableName = "$" + localsCounter.getAndIncrement();
				newArgs.addAll(args);
				newArgs.add(variableName);

				String argTypeAsStr = argumentNode.getArgumentType() instanceof PrimitiveType prim
					? command.processingEnv.getTypeUtils().boxedClass(prim).getQualifiedName().toString()
					: argumentNode.getArgumentType().toString();

				stream = Streams.multiconcat(
					stream,
					Stream.of(
						argTypeAsStr + " " + variableName + " = "
							+ command.getParseMethodFor(argumentNode.getArgumentType())
							+ "($, $start);",
						"if (" + variableName + " != null) {"),
					child.generateTabCompleteCode(command, newArgs, localsCounter).map(s -> indent + s),
					Stream.of(
						indent + "return java.util.Collections.emptyList();",
						"}"));
			}
		}

		return stream;
	}
}
