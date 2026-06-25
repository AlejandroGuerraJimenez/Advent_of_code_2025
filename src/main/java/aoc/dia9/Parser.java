package aoc.dia9;

import aoc.dia9.model.Tile;
import aoc.parse.Coordinates;
import aoc.parse.Lines;

import java.util.List;

public class Parser {

    public static List<Tile> parse(String input) {
        return Lines.nonBlank(input).stream().map(Parser::parseLine).toList();
    }

    private static Tile parseLine(String line) {
        int[] c = Coordinates.ints(line);
        return new Tile(c[0], c[1]);
    }
}
