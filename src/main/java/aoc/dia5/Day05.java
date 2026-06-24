package aoc.dia5;

import aoc.core.Day;
import aoc.dia5.model.FreshnessChecker;

public class Day05 implements Day {

    @Override
    public String part1(String input) {
        return String.valueOf(FreshnessChecker.countFresh(Parser.parse(input)));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(FreshnessChecker.countAllFresh(Parser.parse(input).freshRanges()));
    }
}
