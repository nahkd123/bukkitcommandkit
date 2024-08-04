package io.github.nahkd123.bukkitcommandkit.processor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import io.github.nahkd123.bukkitcommandkit.ArgConstraint;
import io.github.nahkd123.bukkitcommandkit.ArgumentProcessor;
import io.github.nahkd123.bukkitcommandkit.Hook;
import io.github.nahkd123.bukkitcommandkit.HookType;
import io.github.nahkd123.bukkitcommandkit.Sender;
import io.github.nahkd123.bukkitcommandkit.Subcommand;
import io.github.nahkd123.bukkitcommandkit.processor.exec.CommandExecutor;
import io.github.nahkd123.bukkitcommandkit.processor.provided.ProvidedArgumentMethods;
import io.github.nahkd123.bukkitcommandkit.processor.provided.ProvidedTabCompleteMethods;
import io.github.nahkd123.bukkitcommandkit.processor.subcommand.SubcommandArgumentSegment;
import io.github.nahkd123.bukkitcommandkit.processor.subcommand.SubcommandLiteralSegment;
import io.github.nahkd123.bukkitcommandkit.processor.subcommand.SubcommandSegment;
import io.github.nahkd123.bukkitcommandkit.processor.tree.CommandNode;
import io.github.nahkd123.bukkitcommandkit.processor.tree.RootCommandNode;
import io.github.nahkd123.bukkitcommandkit.processor.util.BukkitTypes;
import io.github.nahkd123.bukkitcommandkit.processor.util.Streams;

public class CommandInfo {
    public ProcessingEnvironment processingEnv;

    public TypeElement owningClass;
    public String outputClassName;
    public final RootCommandNode root = new RootCommandNode();

    // Hooks
    public String noPermissionMethod = null;
    public String unknownParameterMethod = null;
    public final Map<TypeMirror, String> tabCompleters = new HashMap<>();
    public final Map<TypeMirror, String> argumentParsers = new HashMap<>();

    // Code generation
    public String indent = "    ";
    public final Map<String, Stream<String>> generatedMethods = new HashMap<>();

    public void collectHook(Hook annotation, ExecutableElement method) {
        if (annotation.value() == HookType.NO_PERMISSION) {
            if (method.getParameters().size() != 2 ||
                !method.getParameters().get(0).asType().toString().equals(BukkitTypes.COMMAND_SENDER) ||
                !method.getParameters().get(1).asType().toString().equals("java.lang.String")) {
                processingEnv.getMessager().printMessage(
                    Kind.ERROR,
                    "NO_PERMISSION hook must have exactly 2 arguments "
                        + BukkitTypes.COMMAND_SENDER + " and String, respectively",
                    method,
                    findMirror(method.getAnnotationMirrors(), annotation.annotationType()));
                return;
            }

            noPermissionMethod = "command." + method.getSimpleName();
        }

        if (annotation.value() == HookType.UNKNOWN_ARGUMENT) {
            if (method.getParameters().size() != 3 ||
                !method.getParameters().get(0).asType().toString().equals(BukkitTypes.COMMAND_SENDER) ||
                method.getParameters().get(1).asType().getKind() != TypeKind.ARRAY ||
                method.getParameters().get(2).asType().getKind() != TypeKind.INT) {
                processingEnv.getMessager().printMessage(
                    Kind.ERROR,
                    "UNKNOWN_ARGUMENT hook must have exactly 3 arguments "
                        + BukkitTypes.COMMAND_SENDER + ", String[] and int, respectively",
                    method,
                    findMirror(method.getAnnotationMirrors(), annotation.annotationType()));
                return;
            }

            unknownParameterMethod = "command." + method.getSimpleName();
        }
    }

    public void collectSubcommand(Subcommand annotation, ExecutableElement method) {
        if (method.getSimpleName().isEmpty()) return;

        for (String rawPath : annotation.value()) {
            // Step 1: Convert string to proper subcommand path
            List<SubcommandSegment> path = new ArrayList<>();
            List<VariableElement> variables = new ArrayList<>();
            variables.addAll(method.getParameters());
            VariableElement caller = null;

            // We need to filter out caller first
            Iterator<VariableElement> iter = variables.iterator();
            while (iter.hasNext()) {
                VariableElement current = iter.next();

                if (current.getAnnotation(Sender.class) != null) {
                    // Sender senderInfo = current.getAnnotation(Sender.class);
                    if (caller != null) {
                        processingEnv.getMessager().printMessage(
                            Kind.MANDATORY_WARNING,
                            "The method's signature can't have more than 1 @Sender",
                            current,
                            findMirror(method.getAnnotationMirrors(), annotation.annotationType()));
                        return;
                    }

                    caller = current;
                    iter.remove(); // Avoid sender param being picked up
                }
            }

            for (String rawSegment : rawPath.split(" ")) {
                if (rawSegment.isBlank()) {
                    processingEnv.getMessager().printMessage(
                        Kind.MANDATORY_WARNING,
                        "The subcommand path may have duplicated spaces",
                        method,
                        findMirror(method.getAnnotationMirrors(), annotation.annotationType()));
                    return;
                }

                if (rawSegment.startsWith("<") && rawSegment.endsWith(">")) {
                    // Argument
                    if (variables.size() == 0) {
                        processingEnv.getMessager().printMessage(
                            Kind.MANDATORY_WARNING,
                            "The number of method parameters must match subcommand path's command parameters",
                            method,
                            findMirror(method.getAnnotationMirrors(), annotation.annotationType()));
                        return;
                    }

                    VariableElement param = variables.remove(0);
                    ArgConstraint constraint = param.getAnnotation(ArgConstraint.class);
                    ArgumentProcessor processor = param.getAnnotation(ArgumentProcessor.class);
                    String label = rawSegment.substring(1, rawSegment.length() - 1);
                    path.add(new SubcommandArgumentSegment(label, param.asType(), constraint, processor));
                } else {
                    path.add(new SubcommandLiteralSegment(rawSegment));
                }
            }

            if (variables.size() != 0) {
                processingEnv.getMessager().printMessage(
                    Kind.MANDATORY_WARNING,
                    "The number of method parameters must match subcommand path's command parameters",
                    method,
                    findMirror(method.getAnnotationMirrors(), annotation.annotationType()));
                return;
            }

            collectSubcommand(annotation, caller, path, method);
        }
    }

    public void collectSubcommand(Subcommand annotation, VariableElement caller, List<SubcommandSegment> path, ExecutableElement method) {
        // Step 2: Walk in the tree
        CommandNode node = root;

        for (SubcommandSegment segment : path) {
            if (segment instanceof SubcommandLiteralSegment literal) node = node.walk(literal);
            else if (segment instanceof SubcommandArgumentSegment argument) node = node.walk(argument);
            else {
                processingEnv.getMessager().printMessage(Kind.OTHER, segment.getClass().getName()
                    + " is not implemented");
                return;
            }
        }

        // Step 2.1: Collect tab complete/argument parser if needed

        // Step 3: Attach executor to the node
        for (String perm : annotation.permission()) node.getPermissions().add(perm);
        TypeElement callerType = caller != null
            ? (TypeElement) processingEnv.getTypeUtils().asElement(caller.asType())
            : null;
        node.getExecutors().add(new CommandExecutor(callerType, method, method.getParameters().indexOf(caller)));
    }

    public Stream<String> generateSource() {
        String[] split = outputClassName.split("\\.");
        String name = split[split.length - 1];
        String packageName = outputClassName.substring(0, outputClassName.length() - name.length() - 1);

        return Streams.multiconcat(
            Stream.of(
                "package " + packageName + ";",
                "",
                "public class " + name + " implements "
                    + BukkitTypes.COMMAND_EXECUTOR + ", "
                    + BukkitTypes.TAB_COMPLETER + " {",
                indent + owningClass.getQualifiedName() + " command;",
                "",
                indent + "public " + name + "(" + owningClass.getQualifiedName() + " command) {",
                indent + indent + "this.command = command;",
                indent + "}",
                "",
                indent + "@Override",
                indent + "public boolean onCommand("
                    + "org.bukkit.command.CommandSender caller, "
                    + "org.bukkit.command.Command bukkitCommand, "
                    + "String alias, "
                    + "String[] $"
                    + ") {",
                indent + indent + "java.util.concurrent.atomic.AtomicInteger $start = "
                    + "new java.util.concurrent.atomic.AtomicInteger(0);"),
            root.generateOnCommandCode(this, List.of(), new AtomicInteger(0)).map(s -> indent + indent + s),
            Stream.of(
                indent + indent + "return true;",
                indent + "}",
                "",
                indent + "@Override",
                indent + "public java.util.List<String> onTabComplete("
                    + "org.bukkit.command.CommandSender caller, "
                    + "org.bukkit.command.Command bukkitCommand, "
                    + "String alias, "
                    + "String[] $"
                    + ") {",
                indent + indent + "java.util.concurrent.atomic.AtomicInteger $start = "
                    + "new java.util.concurrent.atomic.AtomicInteger(0);"),
            root.generateTabCompleteCode(this, List.of(), new AtomicInteger(0)).map(s -> indent + indent + s),
            Stream.of(
                indent + indent + "return java.util.Collections.emptyList();",
                indent + "}",
                ""),
            generatedMethods.entrySet().stream().flatMap(e -> e.getValue()).map(s -> indent + s),
            Stream.of("}"));
    }

    private AnnotationMirror findMirror(List<? extends AnnotationMirror> mirrors, Class<? extends Annotation> type) {
        String typeStr = type.getName();
        for (AnnotationMirror mirror : mirrors)
            if (mirror.getAnnotationType().toString().equals(typeStr)) return mirror;
        return null;
    }

    public String getNoPermissionMethod() {
        if (noPermissionMethod == null) {
            noPermissionMethod = "generated$noPermission";
            generatedMethods.put(noPermissionMethod, Stream.of(
                "private void generated$noPermission("
                    + BukkitTypes.COMMAND_SENDER + " sender, "
                    + "String permission) {",
                indent + "sender.sendMessage(\"\\u00A7cYou don't have \" + permission + \" permission!\");",
                "}"));
        }

        return noPermissionMethod;
    }

    public String getUnknownParameterMethod() {
        if (unknownParameterMethod == null) {
            unknownParameterMethod = "generated$unknownParameter";
            generatedMethods.put(unknownParameterMethod, Stream.of(
                "private void generated$unknownParameter("
                    + BukkitTypes.COMMAND_SENDER + " sender, "
                    + "String[] args, "
                    + "int position) {",
                indent + "sender.sendMessage(\"\\u00A7cI don't know how to read \" + args[position] + \".\");",
                "}"));
        }

        return unknownParameterMethod;
    }

    public String getParseMethodFor(TypeMirror type) {
        String method = argumentParsers.get(type);
        String typeName = type.toString();

        if (method == null) {
            method = "generated$argument$" + type.toString().replace('.', '$');
            String signature = "private " + type + " "
                + method
                + "(String[] args, java.util.concurrent.atomic.AtomicInteger argStart)";

            if (type instanceof PrimitiveType prim) {
                signature = "private " + processingEnv.getTypeUtils().boxedClass(prim) + " "
                    + method
                    + "(String[] args, java.util.concurrent.atomic.AtomicInteger argStart)";

                String primitiveParseMethod = switch (type.getKind()) {
                case BYTE -> "Byte.parseByte";
                case SHORT -> "Short.parseShort";
                case INT -> "Integer.parseInt";
                case LONG -> "Long.parseLong";
                case FLOAT -> "Float.parseFloat";
                case DOUBLE -> "Double.parseDouble";
                case BOOLEAN -> "Boolean.parseBoolean";
                default -> throw new IllegalArgumentException("Not implemented kind: " + type.getKind());
                };

                generatedMethods.put(method, Stream.of(
                    signature + " {",
                    indent + "try {",
                    indent + indent + type + " out = " + primitiveParseMethod + "(args[argStart.get()]);",
                    indent + indent + "argStart.incrementAndGet();",
                    indent + indent + "return out;",
                    indent + "} catch (IllegalArgumentException e) {",
                    indent + indent + "return null;",
                    indent + "}",
                    "}"));
                return method;
            }

            if (type.getKind() == TypeKind.DECLARED) {
                TypeElement elem = (TypeElement) processingEnv.getTypeUtils().asElement(type);

                if (elem.getKind() == ElementKind.ENUM) {
                    generatedMethods.put(method, Stream.of(
                        signature + " {",
                        indent + "try {",
                        indent + indent + type + " out = " + type + ".valueOf(args[argStart.get()]);",
                        indent + indent + "argStart.incrementAndGet();",
                        indent + indent + "return out;",
                        indent + "} catch (IllegalArgumentException e) {",
                        indent + indent + "return null;",
                        indent + "}",
                        "}"));
                    return method;
                }
            }

            String provided = ProvidedArgumentMethods.METHODS.get(typeName);
            if (provided != null) {
                generatedMethods.put(method, Streams.multiconcat(
                    Stream.of(signature + " {"),
                    Stream.of(provided.split("\n")).filter(s -> !s.isBlank()).map(s -> indent + s),
                    Stream.of("}")));
                return method;
            }

            // TODO always return null this time
            generatedMethods.put(method, Stream.of(
                signature + " {",
                indent + "return null;",
                "}"));
        }

        return method;
    }

    public String getTabCompleteMethodFor(TypeMirror type) {
        String method = tabCompleters.get(type);
        String typeName = type.toString();

        if (method == null) {
            method = "generated$tabComplete$" + type.toString().replace('.', '$');
            String signature = "private void " + method + "("
                + "String[] args, "
                + "java.util.concurrent.atomic.AtomicInteger argStart, "
                + "java.util.function.Consumer<String> suggest)";

            if (type instanceof PrimitiveType prim) {
                generatedMethods.put(method, Stream.of(signature + " { suggest.accept(\"<" + prim + "...>\"); }"));
                return method;
            }

            if (type.getKind() == TypeKind.DECLARED) {
                TypeElement elem = (TypeElement) processingEnv.getTypeUtils().asElement(type);

                if (elem.getKind() == ElementKind.ENUM) {
                    generatedMethods.put(method, Stream.of(
                        signature + " {",
                        indent + "for (" + type + " value : " + type + ".values()) suggest.accept(value.toString());",
                        "}"));
                    return method;
                }
            }

            String provided = ProvidedTabCompleteMethods.METHODS.get(typeName);
            if (provided != null) {
                generatedMethods.put(method, Streams.multiconcat(
                    Stream.of(signature + " {"),
                    Stream.of(provided.split("\n")).filter(s -> !s.isBlank()).map(s -> indent + s),
                    Stream.of("}")));
                return method;
            }

            generatedMethods.put(method, Stream.of(signature + " {}"));
        }

        return method;
    }
}
