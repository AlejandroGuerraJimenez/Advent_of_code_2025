package aoc.dia8.model;

public record Edge(int a, int b, long distSq) implements Comparable<Edge> {

    @Override
    public int compareTo(Edge o) {
        return Long.compare(distSq, o.distSq);
    }
}
