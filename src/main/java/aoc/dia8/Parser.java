package aoc.dia8;

import aoc.dia8.model.Point3D;

import java.util.List;

public class Parser {

    public static List<Point3D> parse(String input) {
        return input.lines().filter(l -> !l.isBlank()).map(Parser::parseLine).toList();
    }

    private static Point3D parseLine(String line) {
        String[] p = line.split(",");
        return new Point3D(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()), Integer.parseInt(p[2].trim()));
    }
}
