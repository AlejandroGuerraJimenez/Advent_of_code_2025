package aoc.dia10;

import aoc.dia10.model.Machine;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day10Test {

    @Test
    void parte1InputReal() {
        Day10 day = new Day10();
        List<Machine> machines = day.parse(TestInputs.day(10));
        assertEquals(415, day.part1(machines));
    }

    @Test
    void parte2InputReal() {
        Day10 day = new Day10();
        List<Machine> machines = day.parse(TestInputs.day(10));
        assertEquals(16663, day.part2(machines));
    }
}
