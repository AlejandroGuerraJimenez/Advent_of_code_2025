package aoc.dia7;

import aoc.core.Day;
import aoc.dia7.model.TachyonSimulator;

public class Day07 implements Day {

    @Override
    public String part1(String input) {
        return String.valueOf(TachyonSimulator.countSplits(Parser.parse(input)));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(TachyonSimulator.countTimelines(Parser.parse(input)));
    }
}
