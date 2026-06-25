package aoc.dia1;

import aoc.dia1.model.Rotation;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day01Test {

    @Test
    void parte1InputReal() {
        Day01 day = new Day01();
        List<Rotation> rotations = day.parse(TestInputs.day(1));
        assertEquals(980, day.part1(rotations));
    }

    @Test
    void parte2InputReal() {
        Day01 day = new Day01();
        List<Rotation> rotations = day.parse(TestInputs.day(1));
        assertEquals(5961, day.part2(rotations));
    }
}
