package aoc.dia7.model;

import java.util.List;

public record Manifold(List<String> rows) {

    public int height() { return rows.size(); }
    public int width()  { return rows.stream().mapToInt(String::length).max().orElse(0); }
    public char at(Position p) {
        String row = rows.get(p.row());
        return p.col() < row.length() ? row.charAt(p.col()) : '.';
    }

    public boolean inBounds(Position p) {
        return p.row() >= 0 && p.row() < height() && p.col() >= 0 && p.col() < width();
    }

    public Position start() {
        for (int r = 0; r < height(); r++)
            for (int c = 0; c < width(); c++)
                if (at(new Position(r, c)) == 'S') return new Position(r, c);
        throw new IllegalStateException("No S found");
    }
}
