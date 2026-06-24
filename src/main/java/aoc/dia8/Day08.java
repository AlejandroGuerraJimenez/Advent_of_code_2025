package aoc.dia8;

import aoc.core.Day;
import aoc.dia8.model.CircuitAnalyzer;

public class Day08 implements Day {

    @Override
    public String part1(String input) {
        return String.valueOf(CircuitAnalyzer.topThreeProduct(Parser.parse(input), 1000));
    }

    @Override
    public String part2(String input) {
        return String.valueOf(CircuitAnalyzer.lastPairXProduct(Parser.parse(input)));
    }
}
