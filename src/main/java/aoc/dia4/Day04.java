package aoc.dia4;

import aoc.core.Day;
import aoc.dia4.model.ForkliftAccessChecker;

public class Day04 implements Day {

    @Override
    public String part1(String input) {
        return String.valueOf(ForkliftAccessChecker.countAccessible(Parser.parse(input)));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(ForkliftAccessChecker.countRemovable(Parser.parse(input)));
    }
}
