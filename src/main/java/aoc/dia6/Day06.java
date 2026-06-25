package aoc.dia6;

import aoc.core.Day;
import aoc.dia6.model.WorksheetSolver;
import aoc.dia6.model.Worksheets;

public class Day06 implements Day<Worksheets> {

    @Override
    public int number() {
        return 6;
    }

    @Override
    public Worksheets parse(String input) {
        return new Worksheets(Parser.parse(input), Parser.parseVertical(input));
    }

    @Override
    public Object part1(Worksheets worksheets) {
        return WorksheetSolver.grandTotal(worksheets.horizontal());
    }

    @Override
    public Object part2(Worksheets worksheets) {
        return WorksheetSolver.grandTotal(worksheets.vertical());
    }
}
