package aoc.dia11;

import aoc.dia11.model.Graph;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day11Test {

    @Test
    void parte1InputReal() {
        Day11 day = new Day11();
        Graph graph = day.parse(TestInputs.day(11));
        assertEquals(714L, day.part1(graph));
    }

    @Test
    void parte2InputReal() {
        Day11 day = new Day11();
        Graph graph = day.parse(TestInputs.day(11));
        assertEquals(333852915427200L, day.part2(graph));
    }
}
