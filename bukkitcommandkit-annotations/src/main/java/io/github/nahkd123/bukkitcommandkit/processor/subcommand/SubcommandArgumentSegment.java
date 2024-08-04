package io.github.nahkd123.bukkitcommandkit.processor.subcommand;

import java.util.Objects;

import javax.lang.model.type.TypeMirror;

import io.github.nahkd123.bukkitcommandkit.ArgConstraint;
import io.github.nahkd123.bukkitcommandkit.ArgumentProcessor;

public record SubcommandArgumentSegment(String label, TypeMirror parameterType, ArgConstraint constraint, ArgumentProcessor processor) implements SubcommandSegment {
    public SubcommandArgumentSegment {
        Objects.requireNonNull(parameterType, "parameterType can't be null");
    }
}
