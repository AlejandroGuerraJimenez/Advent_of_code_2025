package aoc.dia4.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ForkliftAccessChecker {

    private static final int MAX_ADJACENT = 4;
    private static final int[][] DIRS = {
        {-1,-1}, {-1,0}, {-1,1},
        { 0,-1},         { 0,1},
        { 1,-1}, { 1,0}, { 1,1}
    };

    // --- Parte 1: snapshot inmutable ---

    public static long countAccessible(Grid grid) {
        return IntStream.range(0, grid.height())
                .mapToLong(r -> countAccessibleInRow(grid, r))
                .sum();
    }

    private static long countAccessibleInRow(Grid grid, int row) {
        return IntStream.range(0, grid.width())
                .filter(c -> isAccessible(grid, row, c))
                .count();
    }

    private static boolean isAccessible(Grid grid, int row, int col) {
        return grid.at(row, col) == '@' && adjacentRollCount(grid, row, col) < MAX_ADJACENT;
    }

    private static long adjacentRollCount(Grid grid, int row, int col) {
        return Arrays.stream(DIRS)
                .filter(d -> grid.inBounds(row + d[0], col + d[1]))
                .filter(d -> grid.at(row + d[0], col + d[1]) == '@')
                .count();
    }

    // --- Parte 2: simulación iterativa sobre grid mutable ---

    public static long countRemovable(Grid grid) {
        char[][] cells = mutableCopy(grid);
        long total = 0, removed;
        do { total += (removed = removeRound(cells)); } while (removed > 0);
        return total;
    }

    private static char[][] mutableCopy(Grid grid) {
        return grid.rows().stream().map(String::toCharArray).toArray(char[][]::new);
    }

    private static long removeRound(char[][] cells) {
        List<int[]> targets = collectAccessible(cells);
        targets.forEach(p -> cells[p[0]][p[1]] = '.');
        return targets.size();
    }

    private static List<int[]> collectAccessible(char[][] cells) {
        List<int[]> result = new ArrayList<>();
        for (int r = 0; r < cells.length; r++)
            for (int c = 0; c < cells[r].length; c++)
                if (isRollAccessible(cells, r, c)) result.add(new int[]{r, c});
        return result;
    }

    private static boolean isRollAccessible(char[][] cells, int r, int c) {
        return cells[r][c] == '@' && neighborRolls(cells, r, c) < MAX_ADJACENT;
    }

    private static long neighborRolls(char[][] cells, int r, int c) {
        return Arrays.stream(DIRS)
                .filter(d -> isRollAt(cells, r + d[0], c + d[1]))
                .count();
    }

    private static boolean isRollAt(char[][] cells, int r, int c) {
        return r >= 0 && r < cells.length && c >= 0 && c < cells[r].length && cells[r][c] == '@';
    }
}
