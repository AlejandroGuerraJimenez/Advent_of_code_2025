package aoc.dia12.adapter;

import aoc.dia12.model.Shape;

/** Adapta un bloque de texto 3×3 del input a un {@link Shape}. */
public final class ShapeParser {

    private ShapeParser() {}

    public static Shape parse(String[] lines, int headerIndex) {
        boolean[][] grid = new boolean[3][];
        for (int r = 0; r < 3; r++) grid[r] = parseRow(lines[headerIndex + 1 + r]);
        return Shape.from(grid);
    }

    private static boolean[] parseRow(String row) {
        boolean[] cells = new boolean[row.length()];
        for (int c = 0; c < row.length(); c++) cells[c] = row.charAt(c) == '#';
        return cells;
    }
}
