package aoc.dia4.model;

import java.util.List;

public record Grid(List<String> rows) {

    public int height() { return rows.size(); }
    public int width()  { return rows.isEmpty() ? 0 : rows.get(0).length(); }
    public char at(int row, int col) { return rows.get(row).charAt(col); }
    public boolean inBounds(int row, int col) {
        return row >= 0 && row < height() && col >= 0 && col < width();
    }
}
