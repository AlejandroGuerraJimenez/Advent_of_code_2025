package aoc.dia8.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class CircuitBoard {

    private final int[] parent;
    private final int[] rank;
    private int components;

    public CircuitBoard(int n) {
        this.parent = IntStream.range(0, n).toArray();
        this.rank = new int[n];
        this.components = n;
    }

    public int components() { return components; }

    public int find(int i) {
        if (parent[i] != i) parent[i] = find(parent[i]);
        return parent[i];
    }

    public boolean union(int a, int b) {
        int ra = find(a), rb = find(b);
        if (ra == rb) return false;
        if (rank[ra] >= rank[rb]) { parent[rb] = ra; if (rank[ra] == rank[rb]) rank[ra]++; }
        else parent[ra] = rb;
        components--;
        return true;
    }

    public List<Integer> circuitSizes() {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 0; i < parent.length; i++) counts.merge(find(i), 1, Integer::sum);
        return new ArrayList<>(counts.values());
    }
}
