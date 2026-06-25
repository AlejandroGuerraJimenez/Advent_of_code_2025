package aoc.dia5.model;

import aoc.parse.LongRange;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FreshnessChecker {

    public static long countFresh(IngredientDatabase db) {
        return db.ingredientIds().stream()
                .filter(id -> isFresh(id, db.freshRanges()))
                .count();
    }

    public static long countAllFresh(IngredientDatabase db) {
        return mergeRanges(db.freshRanges()).stream()
                .mapToLong(LongRange::length)
                .sum();
    }

    private static boolean isFresh(long id, List<LongRange> ranges) {
        return ranges.stream().anyMatch(r -> r.contains(id));
    }

    private static List<LongRange> mergeRanges(List<LongRange> ranges) {
        List<LongRange> sorted = ranges.stream()
                .sorted(Comparator.comparingLong(LongRange::start))
                .toList();
        List<LongRange> result = new ArrayList<>();
        result.add(sorted.getFirst());
        sorted.subList(1, sorted.size()).forEach(r -> mergeInto(result, r));
        return result;
    }

    private static void mergeInto(List<LongRange> merged, LongRange next) {
        LongRange last = merged.getLast();
        if (last.connectsWith(next))
            merged.set(merged.size() - 1, last.union(next));
        else
            merged.add(next);
    }
}
