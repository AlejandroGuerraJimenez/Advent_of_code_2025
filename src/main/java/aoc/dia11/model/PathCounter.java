package aoc.dia11.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PathCounter {

    // Part 1: count all paths from "you" to "out"
    public static long countPaths(Map<String, List<String>> graph) {
        return dfs("you", graph, new HashMap<>());
    }

    private static long dfs(String node, Map<String, List<String>> graph, Map<String, Long> memo) {
        if ("out".equals(node)) return 1;
        if (memo.containsKey(node)) return memo.get(node);
        long count = 0;
        for (String next : graph.getOrDefault(node, List.of()))
            count += dfs(next, graph, memo);
        memo.put(node, count);
        return count;
    }

    // Part 2: count paths from "svr" to "out" that visit both req1 and req2
    public static long countPathsThrough(Map<String, List<String>> graph, String req1, String req2) {
        Map<String, Integer> bits = Map.of(req1, 1, req2, 2);
        int startMask = bits.getOrDefault("svr", 0);
        // memo key: node + "|" + mask (mask is 0-3, so 4 states per node)
        Map<String, long[]> memo = new HashMap<>();
        return dfs2("svr", bits, graph, memo, startMask);
    }

    private static long dfs2(String node, Map<String, Integer> bits,
                              Map<String, List<String>> graph,
                              Map<String, long[]> memo, int mask) {
        if ("out".equals(node)) return mask == 3 ? 1 : 0;
        long[] cached = memo.get(node);
        if (cached != null && cached[mask] >= 0) return cached[mask];
        if (cached == null) {
            cached = new long[]{-1, -1, -1, -1};
            memo.put(node, cached);
        }
        long count = 0;
        for (String next : graph.getOrDefault(node, List.of())) {
            int newMask = mask | bits.getOrDefault(next, 0);
            count += dfs2(next, bits, graph, memo, newMask);
        }
        cached[mask] = count;
        return count;
    }
}
