package io.github.nahkd123.bukkitcommandkit.processor.exec;

import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public record CommandExecutor(TypeElement callerType, ExecutableElement method, int senderPosition) {
	public String generateMethodInputs(String callerVariable, List<String> args) {
		String out = "";
		int counter = 0;

		for (int i = 0; i < method.getParameters().size(); i++) {
			out += i == 0 ? "" : ", ";
			out += i == senderPosition ? callerVariable : args.get(counter++);
		}

		return out;
	}
}
