package aoc.dia4;

import aoc.core.Day;
import aoc.dia4.model.ForkliftAccessChecker;
import aoc.parse.TextGrid;

public class Day04 implements Day<TextGrid> {

    @Override
    public int number() {
        return 4;
    }

    @Override
    public TextGrid parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(TextGrid grid) {
        return ForkliftAccessChecker.countAccessible(grid);
    }

    @Override
    public Object part2(TextGrid grid) {
        return ForkliftAccessChecker.countRemovable(grid);
    }
}
