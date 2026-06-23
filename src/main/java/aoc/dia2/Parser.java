package aoc.dia2;

import aoc.dia2.model.IdRange;

import java.util.Arrays;
import java.util.List;

public class Parser {

    public static List<IdRange> parse(String input) {
        return Arrays.stream(input.strip().split(","))
                .map(Parser::parseRange)
                .toList();
    }

    private static IdRange parseRange(String range) {
        String[] parts = range.split("-");
        return new IdRange(
                Long.parseLong(parts[0]),
                Long.parseLong(parts[1])
        );
    }
}
