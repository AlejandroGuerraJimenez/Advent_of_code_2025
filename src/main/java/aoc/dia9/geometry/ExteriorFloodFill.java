package aoc.dia9.geometry;

import java.util.LinkedList;
import java.util.Queue;

/** Marca celdas exteriores al polígono mediante flood fill desde el borde. */
public final class ExteriorFloodFill {

    private static final int[][] DIRS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    private ExteriorFloodFill() {}

    public static boolean[][] mark(boolean[][] boundary) {
        boolean[][] exterior = new boolean[boundary.length][boundary[0].length];
        Queue<int[]> queue = new LinkedList<>();
        seedBorder(queue, exterior, boundary);
        while (!queue.isEmpty()) spread(queue, exterior, boundary, boundary.length, boundary[0].length);
        return exterior;
    }

    private static void seedBorder(Queue<int[]> q, boolean[][] ext, boolean[][] b) {
        int cols = b.length, rows = b[0].length;
        for (int c = 0; c < cols; c++) { tryEnqueue(q, ext, b, c, 0); tryEnqueue(q, ext, b, c, rows - 1); }
        for (int r = 1; r < rows - 1; r++) { tryEnqueue(q, ext, b, 0, r); tryEnqueue(q, ext, b, cols - 1, r); }
    }

    private static void tryEnqueue(Queue<int[]> q, boolean[][] ext, boolean[][] b, int c, int r) {
        if (!b[c][r] && !ext[c][r]) { ext[c][r] = true; q.offer(new int[]{c, r}); }
    }

    private static void spread(Queue<int[]> q, boolean[][] ext, boolean[][] b, int cols, int rows) {
        int[] cell = q.poll();
        if (cell == null) return;
        for (int[] d : DIRS) {
            int nc = cell[0] + d[0], nr = cell[1] + d[1];
            if (nc >= 0 && nc < cols && nr >= 0 && nr < rows) tryEnqueue(q, ext, b, nc, nr);
        }
    }
}
