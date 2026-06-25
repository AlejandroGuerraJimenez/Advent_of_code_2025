package aoc.parse;

import java.util.List;

/** Utilidades para trocear la entrada en líneas. */
public final class Lines {

    private Lines() {}

    /** Líneas no vacías (descarta líneas en blanco). */
    public static List<String> nonBlank(String input) {
        return input.lines().filter(line -> !line.isBlank()).toList();
    }

    /** Todas las líneas tal cual, incluidas las vacías. */
    public static List<String> all(String input) {
        return input.lines().toList();
    }
}
