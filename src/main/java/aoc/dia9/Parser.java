package aoc.dia9;

import aoc.dia9.model.Tile;

import java.util.List;

public class Parser {

    public static List<Tile> parse(String input) {
        return input.lines().filter(l -> !l.isBlank()).map(Parser::parseLine).toList();
    }

    private static Tile parseLine(String line) {
        String[] p = line.split(",");
        return new Tile(Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()));
    }
}
