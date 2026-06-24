package aoc.dia12.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A present shape (polyomino inside a 3x3 box). Pre-computes every distinct
 * orientation (4 rotations x optional flip), each stored as a scan-ordered list
 * of {row, col} offsets normalised so the top-left of its bounding box is (0,0).
 */
public final class Shape {

    private final List<int[][]> orientations;
    private final int cells;

    private Shape(List<int[][]> orientations, int cells) {
        this.orientations = orientations;
        this.cells = cells;
    }

    public int cells() {
        return cells;
    }

    public List<int[][]> orientations() {
        return orientations;
    }

    public static Shape from(boolean[][] grid) {
        List<int[]> base = filledCells(grid);
        return new Shape(distinctOrientations(base), base.size());
    }

    private static List<int[]> filledCells(boolean[][] grid) {
        List<int[]> base = new ArrayList<>();
        for (int r = 0; r < grid.length; r++)
            for (int c = 0; c < grid[r].length; c++)
                if (grid[r][c]) base.add(new int[]{r, c});
        return base;
    }

    private static List<int[][]> distinctOrientations(List<int[]> base) {
        List<int[][]> distinct = new ArrayList<>();
        List<int[]> current = base;
        for (int flip = 0; flip < 2; flip++) {
            current = collectRotations(distinct, current);
            current = flip(current);
        }
        return distinct;
    }

    private static List<int[]> collectRotations(List<int[][]> distinct, List<int[]> current) {
        for (int rot = 0; rot < 4; rot++) {
            addUnique(distinct, normalise(current));
            current = rotate(current);
        }
        return current;
    }

    private static void addUnique(List<int[][]> distinct, int[][] orientation) {
        if (distinct.stream().noneMatch(o -> Arrays.deepEquals(o, orientation))) distinct.add(orientation);
    }

    private static int[][] normalise(List<int[]> coords) {
        int minR = min(coords, 0), minC = min(coords, 1);
        int[][] out = map(coords, cell -> new int[]{cell[0] - minR, cell[1] - minC});
        Arrays.sort(out, (a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]);
        return out;
    }

    private static int min(List<int[]> coords, int axis) {
        return coords.stream().mapToInt(cell -> cell[axis]).min().orElse(0);
    }

    private static List<int[]> rotate(List<int[]> coords) {
        return Arrays.asList(map(coords, cell -> new int[]{cell[1], -cell[0]}));
    }

    private static List<int[]> flip(List<int[]> coords) {
        return Arrays.asList(map(coords, cell -> new int[]{cell[0], -cell[1]}));
    }

    private static int[][] map(List<int[]> coords, UnaryOperator<int[]> op) {
        return coords.stream().map(op).toArray(int[][]::new);
    }
}
