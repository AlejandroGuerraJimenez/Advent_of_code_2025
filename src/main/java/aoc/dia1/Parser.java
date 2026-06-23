package aoc.dia1;

import aoc.dia1.model.Direction;
import aoc.dia1.model.Rotation;

import java.util.List;

public class Parser {

    public static List<Rotation> parse(String input) {
        return input.lines()
                .filter(line -> !line.isBlank())
                .map(Parser::parseLine)
                .toList();
    }

    private static Rotation parseLine(String line) {
        Direction direction = line.charAt(0) == 'R' ? Direction.RIGHT : Direction.LEFT;
        int steps = Integer.parseInt(line.substring(1));

        return new Rotation(direction, steps);
    }

}
