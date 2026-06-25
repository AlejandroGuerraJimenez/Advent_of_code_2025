package aoc.dia3;

import aoc.dia3.model.BatteryBank;
import aoc.test.TestInputs;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Day03Test {

    @Test
    void parte1InputReal() {
        Day03 day = new Day03();
        List<BatteryBank> banks = day.parse(TestInputs.day(3));
        assertEquals(16927L, day.part1(banks));
    }

    @Test
    void parte2InputReal() {
        Day03 day = new Day03();
        List<BatteryBank> banks = day.parse(TestInputs.day(3));
        assertEquals(167384358365132L, day.part2(banks));
    }
}
