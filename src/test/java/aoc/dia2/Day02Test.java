package aoc.dia2;

import aoc.parse.LongRange;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day02Test {

    @Test
    void parte1InputReal() {
        Day02 day = new Day02();
        List<LongRange> ranges = day.parse(TestInputs.day(2));
        assertEquals(24747430309L, day.part1(ranges));
    }

    @Test
    void parte2InputReal() {
        Day02 day = new Day02();
        List<LongRange> ranges = day.parse(TestInputs.day(2));
        assertEquals(30962646823L, day.part2(ranges));
    }
}
