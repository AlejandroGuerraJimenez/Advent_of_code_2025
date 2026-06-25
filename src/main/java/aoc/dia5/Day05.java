package aoc.dia5;

import aoc.core.Day;
import aoc.dia5.model.FreshnessChecker;
import aoc.dia5.model.IngredientDatabase;

public class Day05 implements Day<IngredientDatabase> {

    @Override
    public int number() {
        return 5;
    }

    @Override
    public IngredientDatabase parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(IngredientDatabase database) {
        return FreshnessChecker.countFresh(database);
    }

    @Override
    public Object part2(IngredientDatabase database) {
        return FreshnessChecker.countAllFresh(database);
    }
}
