package io.leangen.graphql.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A collection of utility methods
 */
public class Utils {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Optional<T> or(Optional<T> left, Optional<T> right) {
        return left.isPresent() ? left : right;
    }

    public static boolean notEmpty(String string) {
        return string != null && !string.isEmpty();
    }
    
    @SafeVarargs
    public static <T> Stream<T> concat(Stream<T>... streams) {
        return Arrays.stream(streams).reduce(Stream::concat).orElse(Stream.empty());
    }
}
