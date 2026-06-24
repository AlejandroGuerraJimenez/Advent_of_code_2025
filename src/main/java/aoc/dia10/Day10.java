package aoc.dia10;

import aoc.core.Day;
import aoc.dia10.model.ButtonSolver;
import aoc.dia10.model.JoltageSolver;

public class Day10 implements Day {

    @Override
    public String part1(String input) {
        return String.valueOf(Parser.parse(input).stream().mapToInt(ButtonSolver::minPresses).sum());
    }

    @Override
    public String part2(String input) {
        return String.valueOf(Parser.parse(input).stream().mapToInt(JoltageSolver::minPresses).sum());
    }
}
