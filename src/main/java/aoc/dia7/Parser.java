package aoc.dia7;

import aoc.dia7.model.Manifold;

import java.util.List;

public class Parser {

    public static Manifold parse(String input) {
        List<String> rows = input.lines().toList();
        return new Manifold(rows);
    }
}
