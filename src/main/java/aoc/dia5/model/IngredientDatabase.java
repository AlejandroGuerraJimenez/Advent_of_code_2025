package aoc.dia5.model;

import aoc.parse.LongRange;

import java.util.List;

public record IngredientDatabase(List<LongRange> freshRanges, List<Long> ingredientIds) {}
