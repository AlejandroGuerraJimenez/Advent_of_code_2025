package aoc.dia8;

import aoc.dia8.model.Point3D;
import aoc.parse.Coordinates;
import aoc.parse.Lines;

import java.util.List;

public class Parser {

    public static List<Point3D> parse(String input) {
        return Lines.nonBlank(input).stream().map(Parser::parseLine).toList();
    }

    private static Point3D parseLine(String line) {
        int[] c = Coordinates.ints(line);
        return new Point3D(c[0], c[1], c[2]);
    }
}
