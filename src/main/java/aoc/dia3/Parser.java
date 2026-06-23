package aoc.dia3;

import aoc.dia3.model.BatteryBank;

import java.util.List;

public class Parser {

    public static List<BatteryBank> parse(String input) {
        return input.lines()
                .filter(line -> !line.isBlank())
                .map(BatteryBank::new)
                .toList();
    }
}
