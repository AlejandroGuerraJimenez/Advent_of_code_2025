package aoc.dia5.model;

import java.util.List;

public record IngredientDatabase(List<FreshRange> freshRanges, List<Long> ingredientIds) {}
