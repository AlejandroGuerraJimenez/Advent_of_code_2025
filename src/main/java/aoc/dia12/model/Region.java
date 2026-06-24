package aoc.dia12.model;

/**
 * A region under a tree: a width x height grid plus the number of presents of
 * each shape index that must be placed inside it.
 */
public record Region(int width, int height, int[] counts) {

    public int area() {
        return width * height;
    }
}
