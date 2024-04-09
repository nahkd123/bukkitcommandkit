package io.github.nahkd123.bukkitcommandkit.processor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;

import io.github.nahkd123.bukkitcommandkit.Command;
import io.github.nahkd123.bukkitcommandkit.Hook;
import io.github.nahkd123.bukkitcommandkit.Subcommand;

@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.nahkd123.bukkitcommandkit.Command")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class CommandAnnotationProcessor extends AbstractProcessor {
	private Map<Element, CommandInfo> commands = new HashMap<>();

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element elem : roundEnv.getElementsAnnotatedWith(Command.class)) {
			if (elem instanceof TypeElement cmd) collectCommand(cmd.getAnnotation(Command.class), cmd);
		}

		if (roundEnv.processingOver()) onCollectDone();
		return false;
	}

	public void onCollectDone() {
		commands.values().forEach(cmd -> {
			Stream<String> linesStream = cmd.generateSource();

			try {
				JavaFileObject source = processingEnv.getFiler().createSourceFile(cmd.outputClassName, cmd.owningClass);

				try (Writer writer = source.openWriter()) {
					Iterator<String> iter = linesStream.iterator();
					while (iter.hasNext()) writer.append(iter.next() + "\n");
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	public void collectCommand(Command command, TypeElement element) {
		if (commands.containsKey(element)) {
			processingEnv.getMessager().printMessage(Kind.NOTE, element + " is already processed! Skipping...");
			return;
		}

		processingEnv.getMessager().printMessage(Kind.OTHER, "Processing " + element + "...");
		CommandInfo info = new CommandInfo();
		info.processingEnv = processingEnv;
		info.owningClass = element;
		info.outputClassName = command.generatedClass().isEmpty()
			? element.getQualifiedName().toString() + "Generated"
			: command.generatedClass();
		info.root.getPermissions().addAll(List.of(command.permission()));

		for (Element child : element.getEnclosedElements()) {
			if (child instanceof ExecutableElement method) {
				if (child.getAnnotation(Subcommand.class) != null) {
					Subcommand subcommand = child.getAnnotation(Subcommand.class);
					info.collectSubcommand(subcommand, method);
				}

				if (child.getAnnotation(Hook.class) != null) {
					Hook hook = child.getAnnotation(Hook.class);
					info.collectHook(hook, method);
				}
			}
		}

		commands.put(element, info);
	}
}
