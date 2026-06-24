package aoc.dia9.model;

import java.util.*;
import java.util.function.ToIntFunction;

public class CompressedPolygon {

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private final int[] xs, ys;
    private final int[][] prefix;

    public CompressedPolygon(List<Tile> tiles) {
        xs = unique(tiles, Tile::x);
        ys = unique(tiles, Tile::y);
        boolean[][] b = buildBoundary(tiles, 2 * xs.length - 1, 2 * ys.length - 1);
        prefix = buildPrefix(b, floodExterior(b));
    }

    public boolean isValid(Tile a, Tile b) {
        int c1 = cx(Math.min(a.x(), b.x())), c2 = cx(Math.max(a.x(), b.x()));
        int r1 = cy(Math.min(a.y(), b.y())), r2 = cy(Math.max(a.y(), b.y()));
        return query(c1, r1, c2, r2) == (c2 - c1 + 1) * (r2 - r1 + 1);
    }

    private boolean[][] buildBoundary(List<Tile> tiles, int cols, int rows) {
        boolean[][] b = new boolean[cols][rows];
        for (int i = 0; i < tiles.size(); i++)
            markSegment(b, tiles.get(i), tiles.get((i + 1) % tiles.size()));
        return b;
    }

    private void markSegment(boolean[][] b, Tile from, Tile to) {
        int c1 = cx(Math.min(from.x(), to.x())), c2 = cx(Math.max(from.x(), to.x()));
        int r1 = cy(Math.min(from.y(), to.y())), r2 = cy(Math.max(from.y(), to.y()));
        for (int c = c1; c <= c2; c++) for (int r = r1; r <= r2; r++) b[c][r] = true;
    }

    private boolean[][] floodExterior(boolean[][] b) {
        boolean[][] ext = new boolean[b.length][b[0].length];
        Queue<int[]> q = new LinkedList<>();
        seedBorder(q, ext, b);
        while (!q.isEmpty()) spread(q, ext, b, b.length, b[0].length);
        return ext;
    }

    private void seedBorder(Queue<int[]> q, boolean[][] ext, boolean[][] b) {
        int cols = b.length, rows = b[0].length;
        for (int c = 0; c < cols; c++) { tryEnqueue(q, ext, b, c, 0); tryEnqueue(q, ext, b, c, rows - 1); }
        for (int r = 1; r < rows - 1; r++) { tryEnqueue(q, ext, b, 0, r); tryEnqueue(q, ext, b, cols - 1, r); }
    }

    private void tryEnqueue(Queue<int[]> q, boolean[][] ext, boolean[][] b, int c, int r) {
        if (!b[c][r] && !ext[c][r]) { ext[c][r] = true; q.offer(new int[]{c, r}); }
    }

    private void spread(Queue<int[]> q, boolean[][] ext, boolean[][] b, int cols, int rows) {
        int[] cell = q.poll();
        if (cell == null) return;
        for (int[] d : DIRS) {
            int nc = cell[0] + d[0], nr = cell[1] + d[1];
            if (nc >= 0 && nc < cols && nr >= 0 && nr < rows) tryEnqueue(q, ext, b, nc, nr);
        }
    }

    private int[][] buildPrefix(boolean[][] b, boolean[][] ext) {
        int[][] p = new int[b.length][b[0].length];
        for (int c = 0; c < b.length; c++)
            for (int r = 0; r < b[0].length; r++)
                p[c][r] = (b[c][r] || !ext[c][r] ? 1 : 0) + get(p, c - 1, r) + get(p, c, r - 1) - get(p, c - 1, r - 1);
        return p;
    }

    private int get(int[][] p, int c, int r) { return (c < 0 || r < 0) ? 0 : p[c][r]; }

    private int query(int c1, int r1, int c2, int r2) {
        return prefix[c2][r2] - get(prefix, c1 - 1, r2) - get(prefix, c2, r1 - 1) + get(prefix, c1 - 1, r1 - 1);
    }

    private int cx(int x) { return 2 * Arrays.binarySearch(xs, x); }
    private int cy(int y) { return 2 * Arrays.binarySearch(ys, y); }

    private static int[] unique(List<Tile> tiles, ToIntFunction<Tile> f) {
        return tiles.stream().mapToInt(f).distinct().sorted().toArray();
    }
}
