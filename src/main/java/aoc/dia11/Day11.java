package aoc.dia11;

import aoc.core.Day;
import aoc.dia11.model.PathCounter;

public class Day11 implements Day {

    @Override
    public String part1(String input) {
        return String.valueOf(PathCounter.countPaths(Parser.parse(input)));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(PathCounter.countPathsThrough(Parser.parse(input), "dac", "fft"));
    }
}
