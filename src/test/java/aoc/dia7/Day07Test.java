package aoc.dia7;

import aoc.dia7.model.Manifold;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day07Test {

    @Test
    void parte1InputReal() {
        Day07 day = new Day07();
        Manifold manifold = day.parse(TestInputs.day(7));
        assertEquals(1543, day.part1(manifold));
    }

    @Test
    void parte2InputReal() {
        Day07 day = new Day07();
        Manifold manifold = day.parse(TestInputs.day(7));
        assertEquals(3223365367809L, day.part2(manifold));
    }
}
