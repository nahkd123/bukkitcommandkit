package io.github.nahkd123.bukkitcommandkit;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * <p>
 * A method parameter that is annotated with {@link ArgumentProcessor}
 * annotation will uses a custom argument processor. The processor can handle
 * parsing and may also handle tab completion.
 * </p>
 * 
 * @see #parser()
 * @see #tabCompleter()
 */
@Documented
@Retention(SOURCE)
@Target(PARAMETER)
public @interface ArgumentProcessor {
    /**
     * <p>
     * A name of the argument parse method to parse argument. The method must be
     * placed in the same class and have the signature
     * {@code (String[], AtomicInteger)}. The first parameter is the entire command
     * input, and the second parameter is {@link AtomicInteger} whose value points
     * to current position in the {@code String[]} array. The value from second
     * parameter may be increased, allowing other argument parsers to parse next
     * argument. If the parser method returns {@code null}, the argument is deemed
     * as invalid.
     * </p>
     * 
     * @return The name of parser method.
     */
    public String parser();

    /**
     * <p>
     * A name of he argument tab complete method to provide tab completion. The
     * method must be placed in the same class and have the signature
     * {@code (String[], AtomicInteger, Consumer<String>)}. The first parameter is
     * the entire command input, the second parameter is {@link AtomicInteger} whose
     * value points to current position in the {@code String[]} array, and the third
     * parameter is the {@link Consumer} that accept suggestions based on user's
     * input. The value from second parameter may be increased, allowing other
     * argument tab complete processors to provide proper tab complete.
     * </p>
     * 
     * @return The name of tab complete method.
     */
    public String tabCompleter() default "";
}
