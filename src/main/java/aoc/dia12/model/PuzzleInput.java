package aoc.dia12.model;

import java.util.List;

/** Modelo parseado del día 12: figuras disponibles y regiones a empaquetar. */
public record PuzzleInput(Shape[] shapes, List<Region> regions) {}
