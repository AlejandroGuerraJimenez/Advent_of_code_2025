package aoc.dia6;

import aoc.dia6.model.Worksheets;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day06Test {

    @Test
    void parte1InputReal() {
        Day06 day = new Day06();
        Worksheets worksheets = day.parse(TestInputs.day(6));
        assertEquals(6503327062445L, day.part1(worksheets));
    }

    @Test
    void parte2InputReal() {
        Day06 day = new Day06();
        Worksheets worksheets = day.parse(TestInputs.day(6));
        assertEquals(9640641878593L, day.part2(worksheets));
    }
}
