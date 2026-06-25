package aoc.dia11.model;

import java.util.List;
import java.util.Map;

/** Grafo dirigido de nodos identificados por nombre. Inmutable. */
public record Graph(Map<String, List<String>> adjacency) {

    /** Vecinos (aristas salientes) de un nodo; lista vacía si no tiene. */
    public List<String> neighbors(String node) {
        return adjacency.getOrDefault(node, List.of());
    }
}
