package aoc.dia5;

import aoc.dia5.model.IngredientDatabase;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day05Test {

    @Test
    void parte1InputReal() {
        Day05 day = new Day05();
        IngredientDatabase db = day.parse(TestInputs.day(5));
        assertEquals(674L, day.part1(db));
    }

    @Test
    void parte2InputReal() {
        Day05 day = new Day05();
        IngredientDatabase db = day.parse(TestInputs.day(5));
        assertEquals(352509891817881L, day.part2(db));
    }
}
