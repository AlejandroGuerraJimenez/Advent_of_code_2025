package aoc.dia7.model;

import java.util.*;

public class TachyonSimulator {

    public static int countSplits(Manifold m) {
        Queue<Position> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        queue.offer(m.start());
        return processQueue(m, queue, visited);
    }

    private static int processQueue(Manifold m, Queue<Position> q, Set<Long> visited) {
        int splits = 0;
        while (!q.isEmpty()) splits += processBeam(m, q, visited, q.poll());
        return splits;
    }

    private static int processBeam(Manifold m, Queue<Position> q, Set<Long> visited, Position start) {
        if (!m.inBounds(start) || visited.contains(start.key(m.width()))) return 0;
        Position hit = scanDownMarking(m, visited, start);
        return m.inBounds(hit) && !visited.contains(hit.key(m.width())) ? split(m, q, visited, hit) : 0;
    }

    private static Position scanDownMarking(Manifold m, Set<Long> visited, Position start) {
        int r = start.row(), c = start.col();
        while (m.inBounds(new Position(r, c)) && m.at(new Position(r, c)) != '^')
            visited.add(new Position(r++, c).key(m.width()));
        return new Position(r, c);
    }

    private static int split(Manifold m, Queue<Position> q, Set<Long> visited, Position p) {
        visited.add(p.key(m.width()));
        q.offer(new Position(p.row(), p.col() - 1));
        q.offer(new Position(p.row(), p.col() + 1));
        return 1;
    }

    public static long countTimelines(Manifold m) {
        return countFrom(m, m.start().row(), m.start().col(), new HashMap<>());
    }

    private static long countFrom(Manifold m, int row, int col, Map<Long, Long> memo) {
        if (!m.inBounds(new Position(row, col))) return 1L;
        long key = (long) row * m.width() + col;
        if (memo.containsKey(key)) return memo.get(key);
        return cache(key, compute(m, row, col, memo), memo);
    }

    private static long cache(long key, long value, Map<Long, Long> memo) {
        memo.put(key, value);
        return value;
    }

    private static long compute(Manifold m, int row, int col, Map<Long, Long> memo) {
        int r = row;
        while (m.inBounds(new Position(r, col)) && m.at(new Position(r, col)) != '^') r++;
        return !m.inBounds(new Position(r, col)) ? 1L
                : countFrom(m, r, col - 1, memo) + countFrom(m, r, col + 1, memo);
    }
}
