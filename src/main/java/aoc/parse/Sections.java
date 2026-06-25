package aoc.parse;

import java.util.ArrayList;
import java.util.List;

/** Divide la entrada en secciones separadas por líneas en blanco. */
public final class Sections {

    private Sections() {}

    public static List<List<String>> splitByBlankLine(String input) {
        List<List<String>> sections = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : input.split("\n", -1)) current = absorb(sections, current, line);
        if (!current.isEmpty()) sections.add(current);
        return sections;
    }

    private static List<String> absorb(List<List<String>> sections, List<String> current, String line) {
        if (!line.isBlank()) { current.add(line); return current; }
        if (current.isEmpty()) return current;
        sections.add(current);
        return new ArrayList<>();
    }
}
