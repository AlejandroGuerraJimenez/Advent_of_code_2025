package aoc.dia3;

import aoc.core.Day;
import aoc.dia3.model.BatteryBank;
import aoc.dia3.model.JoltageCalculator;

import java.util.List;

public class Day03 implements Day {

    @Override
    public String part1(String input) {
        return sumMaxJoltage(Parser.parse(input), 2);
    }

    @Override
    public String part2(String input) {
        return sumMaxJoltage(Parser.parse(input), 12);
    }

    private String sumMaxJoltage(List<BatteryBank> banks, int count) {
        long total = banks.stream()
                .mapToLong(bank -> JoltageCalculator.maxJoltage(bank, count))
                .sum();
        return String.valueOf(total);
    }
}
