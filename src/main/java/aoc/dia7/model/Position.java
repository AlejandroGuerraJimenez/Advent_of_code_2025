package aoc.dia7.model;

public record Position(int row, int col) {
    public long key(int width) { return (long) row * width + col; }
}
