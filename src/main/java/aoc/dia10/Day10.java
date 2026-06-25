package aoc.dia10;

import aoc.core.Day;
import aoc.dia10.model.ButtonSolver;
import aoc.dia10.model.JoltageSolver;
import aoc.dia10.model.Machine;

import java.util.List;

public class Day10 implements Day<List<Machine>> {

    @Override
    public int number() {
        return 10;
    }

    @Override
    public List<Machine> parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(List<Machine> machines) {
        return machines.stream().mapToInt(ButtonSolver::minPresses).sum();
    }

    @Override
    public Object part2(List<Machine> machines) {
        return machines.stream().mapToInt(JoltageSolver::minPresses).sum();
    }
}
