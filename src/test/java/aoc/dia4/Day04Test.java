package aoc.dia4;

import aoc.parse.TextGrid;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day04Test {

    @Test
    void parte1InputReal() {
        Day04 day = new Day04();
        TextGrid grid = day.parse(TestInputs.day(4));
        assertEquals(1489L, day.part1(grid));
    }

    @Test
    void parte2InputReal() {
        Day04 day = new Day04();
        TextGrid grid = day.parse(TestInputs.day(4));
        assertEquals(8890L, day.part2(grid));
    }
}
