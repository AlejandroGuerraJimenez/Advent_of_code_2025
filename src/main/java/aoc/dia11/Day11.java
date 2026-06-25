package aoc.dia11;

import aoc.core.Day;
import aoc.dia11.model.Graph;
import aoc.dia11.model.PathCounter;

public class Day11 implements Day<Graph> {

    @Override
    public int number() {
        return 11;
    }

    @Override
    public Graph parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(Graph graph) {
        return PathCounter.countPaths(graph);
    }

    @Override
    public Object part2(Graph graph) {
        return PathCounter.countPathsThrough(graph, "dac", "fft");
    }
}
