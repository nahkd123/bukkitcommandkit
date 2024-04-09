package io.github.nahkd123.bukkitcommandkit.processor.util;

import java.util.stream.Stream;

public class Streams {
	@SafeVarargs
	public static <T> Stream<T> multiconcat(Stream<? extends T>... streams) {
		return Stream.of(streams).flatMap(s -> s);
	}
}
