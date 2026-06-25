package aoc.dia8.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CircuitAnalyzer {

    public static long topThreeProduct(List<Point3D> points, int connections) {
        CircuitBoard board = new CircuitBoard(points.size());
        allEdges(points).stream().sorted().limit(connections).forEach(e -> board.union(e.a(), e.b()));
        List<Integer> sizes = board.circuitSizes();
        sizes.sort(Comparator.reverseOrder());
        return topThree(sizes);
    }

    private static long topThree(List<Integer> sizes) {
        if (sizes.size() < 3)
            throw new IllegalStateException("Menos de 3 circuitos (" + sizes.size() + "). ¿El input tiene suficientes puntos?");
        return (long) sizes.get(0) * sizes.get(1) * sizes.get(2);
    }

    public static long lastPairXProduct(List<Point3D> points) {
        CircuitBoard board = new CircuitBoard(points.size());
        for (Edge e : allEdges(points).stream().sorted().toList())
            if (board.union(e.a(), e.b()) && board.components() == 1)
                return (long) points.get(e.a()).x() * points.get(e.b()).x();
        throw new IllegalStateException("No se pudo conectar todos los circuitos");
    }

    private static List<Edge> allEdges(List<Point3D> pts) {
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < pts.size(); i++)
            for (int j = i + 1; j < pts.size(); j++)
                edges.add(new Edge(i, j, pts.get(i).distSq(pts.get(j))));
        return edges;
    }
}
