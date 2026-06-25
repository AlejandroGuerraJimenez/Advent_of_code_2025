package aoc.dia3;

import aoc.dia3.model.BatteryBank;
import aoc.parse.Lines;

import java.util.List;

public class Parser {

    public static List<BatteryBank> parse(String input) {
        return Lines.nonBlank(input).stream()
                .map(BatteryBank::new)
                .toList();
    }
}
