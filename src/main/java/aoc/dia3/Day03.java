package aoc.dia3;

import aoc.core.Day;
import aoc.dia3.model.BatteryBank;
import aoc.dia3.model.JoltageCalculator;

import java.util.List;

public class Day03 implements Day<List<BatteryBank>> {

    private static final int PART1_BATTERIES = 2;
    private static final int PART2_BATTERIES = 12;

    @Override
    public int number() {
        return 3;
    }

    @Override
    public List<BatteryBank> parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(List<BatteryBank> banks) {
        return sumMaxJoltage(banks, PART1_BATTERIES);
    }

    @Override
    public Object part2(List<BatteryBank> banks) {
        return sumMaxJoltage(banks, PART2_BATTERIES);
    }

    private long sumMaxJoltage(List<BatteryBank> banks, int count) {
        return banks.stream()
                .mapToLong(bank -> JoltageCalculator.maxJoltage(bank, count))
                .sum();
    }
}
