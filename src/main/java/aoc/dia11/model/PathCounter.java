package aoc.dia11.model;

import java.util.HashMap;
import java.util.Map;

public class PathCounter {

    /** Parte 1: cuenta todos los caminos de "you" a "out". */
    public static long countPaths(Graph graph) {
        return dfs("you", graph, new HashMap<>());
    }

    private static long dfs(String node, Graph graph, Map<String, Long> memo) {
        if ("out".equals(node)) return 1;
        if (memo.containsKey(node)) return memo.get(node);
        long count = 0;
        for (String next : graph.neighbors(node))
            count += dfs(next, graph, memo);
        memo.put(node, count);
        return count;
    }

    /** Parte 2: cuenta los caminos de "svr" a "out" que pasan por req1 y req2. */
    public static long countPathsThrough(Graph graph, String req1, String req2) {
        Map<String, Integer> bits = Map.of(req1, 1, req2, 2);
        int startMask = bits.getOrDefault("svr", 0);
        // Clave de memo: nodo + máscara (0-3, 4 estados por nodo)
        Map<String, long[]> memo = new HashMap<>();
        return dfs2("svr", bits, graph, memo, startMask);
    }

    private static long dfs2(String node, Map<String, Integer> bits,
                              Graph graph, Map<String, long[]> memo, int mask) {
        if ("out".equals(node)) return mask == 3 ? 1 : 0;
        long[] cached = memo.get(node);
        if (cached != null && cached[mask] >= 0) return cached[mask];
        if (cached == null) {
            cached = new long[]{-1, -1, -1, -1};
            memo.put(node, cached);
        }
        long count = 0;
        for (String next : graph.neighbors(node)) {
            int newMask = mask | bits.getOrDefault(next, 0);
            count += dfs2(next, bits, graph, memo, newMask);
        }
        cached[mask] = count;
        return count;
    }
}
