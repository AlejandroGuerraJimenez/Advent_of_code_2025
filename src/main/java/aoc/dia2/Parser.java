package aoc.dia2;

import aoc.parse.LongRange;

import java.util.Arrays;
import java.util.List;

public class Parser {

    public static List<LongRange> parse(String input) {
        return Arrays.stream(input.strip().split(","))
                .map(LongRange::parse)
                .toList();
    }
}
