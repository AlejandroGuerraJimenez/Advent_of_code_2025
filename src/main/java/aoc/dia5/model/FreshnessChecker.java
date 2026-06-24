package aoc.dia5.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FreshnessChecker {

    public static long countFresh(IngredientDatabase db) {
        return db.ingredientIds().stream()
                .filter(id -> isFresh(id, db.freshRanges()))
                .count();
    }

    public static long countAllFresh(List<FreshRange> ranges) {
        return mergeRanges(ranges).stream()
                .mapToLong(r -> r.end() - r.start() + 1)
                .sum();
    }

    private static boolean isFresh(long id, List<FreshRange> ranges) {
        return ranges.stream().anyMatch(r -> r.contains(id));
    }

    private static List<FreshRange> mergeRanges(List<FreshRange> ranges) {
        List<FreshRange> sorted = ranges.stream()
                .sorted(Comparator.comparingLong(FreshRange::start))
                .toList();
        List<FreshRange> result = new ArrayList<>();
        result.add(sorted.getFirst());
        sorted.subList(1, sorted.size()).forEach(r -> mergeInto(result, r));
        return result;
    }

    private static void mergeInto(List<FreshRange> merged, FreshRange next) {
        FreshRange last = merged.getLast();
        if (next.start() <= last.end() + 1)
            merged.set(merged.size() - 1, new FreshRange(last.start(), Math.max(last.end(), next.end())));
        else
            merged.add(next);
    }
}
