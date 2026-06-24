package aoc.dia9;

import aoc.core.Day;
import aoc.dia9.model.RectangleSolver;

public class Day09 implements Day {

    @Override
    public String part1(String input) {
        return String.valueOf(RectangleSolver.maxArea(Parser.parse(input)));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(RectangleSolver.maxValidArea(Parser.parse(input)));
    }
}
