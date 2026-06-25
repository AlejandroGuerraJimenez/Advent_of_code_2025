package aoc.dia8;

import aoc.dia8.model.Point3D;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day08Test {

    @Test
    void parte1InputReal() {
        Day08 day = new Day08();
        List<Point3D> points = day.parse(TestInputs.day(8));
        assertEquals(62186L, day.part1(points));
    }

    @Test
    void parte2InputReal() {
        Day08 day = new Day08();
        List<Point3D> points = day.parse(TestInputs.day(8));
        assertEquals(8420405530L, day.part2(points));
    }
}
