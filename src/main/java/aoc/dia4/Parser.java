package aoc.dia4;

import aoc.parse.Lines;
import aoc.parse.TextGrid;

public class Parser {

    public static TextGrid parse(String input) {
        return TextGrid.fromLines(Lines.nonBlank(input));
    }
}
