package aoc.dia6;

import aoc.core.Day;
import aoc.dia6.model.WorksheetSolver;

public class Day06 implements Day {

    @Override
    public String part1(String input) {
        return String.valueOf(WorksheetSolver.grandTotal(Parser.parse(input)));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(WorksheetSolver.grandTotal(Parser.parseVertical(input)));
    }
}
