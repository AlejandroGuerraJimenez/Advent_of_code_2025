package aoc.dia4;

import aoc.dia4.model.Grid;

import java.util.List;

public class Parser {

    public static Grid parse(String input) {
        List<String> rows = input.lines()
                .filter(line -> !line.isBlank())
                .toList();
        return new Grid(rows);
    }
}
