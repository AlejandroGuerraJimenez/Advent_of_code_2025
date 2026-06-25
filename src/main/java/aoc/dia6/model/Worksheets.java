package aoc.dia6.model;

/**
 * Las dos lecturas del mismo input: la horizontal (parte 1) y la vertical
 * de derecha a izquierda (parte 2). Se parsean una sola vez.
 */
public record Worksheets(MathWorksheet horizontal, MathWorksheet vertical) {}
