package aoc.dia5;

import aoc.dia5.model.FreshRange;
import aoc.dia5.model.IngredientDatabase;

import java.util.List;

public class Parser {

    public static IngredientDatabase parse(String input) {
        List<String> lines = input.lines().toList();
        int blankIdx = lines.indexOf("");
        return new IngredientDatabase(parseRanges(lines, blankIdx), parseIds(lines, blankIdx));
    }

    private static List<FreshRange> parseRanges(List<String> lines, int blankIdx) {
        return lines.subList(0, blankIdx).stream()
                .map(Parser::parseRange)
                .toList();
    }

    private static List<Long> parseIds(List<String> lines, int blankIdx) {
        return lines.subList(blankIdx + 1, lines.size()).stream()
                .filter(l -> !l.isBlank())
                .map(Long::parseLong)
                .toList();
    }

    private static FreshRange parseRange(String line) {
        String[] parts = line.split("-");
        return new FreshRange(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }
}
