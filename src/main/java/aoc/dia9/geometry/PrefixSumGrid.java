package aoc.dia9.geometry;

/** Suma acumulada 2D sobre celdas interiores (borde o no exterior). */
public final class PrefixSumGrid {

    private final int[][] prefix;

    public PrefixSumGrid(boolean[][] boundary, boolean[][] exterior) {
        prefix = build(boundary, exterior);
    }

    public int query(int c1, int r1, int c2, int r2) {
        return prefix[c2][r2] - get(prefix, c1 - 1, r2) - get(prefix, c2, r1 - 1) + get(prefix, c1 - 1, r1 - 1);
    }

    private static int[][] build(boolean[][] boundary, boolean[][] exterior) {
        int[][] prefix = new int[boundary.length][boundary[0].length];
        for (int c = 0; c < boundary.length; c++)
            for (int r = 0; r < boundary[0].length; r++)
                prefix[c][r] = (boundary[c][r] || !exterior[c][r] ? 1 : 0)
                        + get(prefix, c - 1, r) + get(prefix, c, r - 1) - get(prefix, c - 1, r - 1);
        return prefix;
    }

    private static int get(int[][] p, int c, int r) { return (c < 0 || r < 0) ? 0 : p[c][r]; }
}
