package aoc.dia7;

import aoc.dia7.model.Manifold;
import aoc.parse.Lines;
import aoc.parse.TextGrid;

public class Parser {

    public static Manifold parse(String input) {
        return new Manifold(TextGrid.fromLines(Lines.all(input)));
    }
}
