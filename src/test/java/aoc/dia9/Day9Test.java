package aoc.dia9;

import aoc.dia9.model.Tile;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day09Test {

    @Test
    void parte1InputReal() {
        Day09 day = new Day09();
        List<Tile> tiles = day.parse(TestInputs.day(9));
        assertEquals(4759531084L, day.part1(tiles));
    }

    @Test
    void parte2InputReal() {
        Day09 day = new Day09();
        List<Tile> tiles = day.parse(TestInputs.day(9));
        assertEquals(1539238860L, day.part2(tiles));
    }
}
