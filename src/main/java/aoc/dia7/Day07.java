package aoc.dia7;

import aoc.core.Day;
import aoc.dia7.model.Manifold;
import aoc.dia7.model.TachyonSimulator;

public class Day07 implements Day<Manifold> {

    @Override
    public int number() {
        return 7;
    }

    @Override
    public Manifold parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(Manifold manifold) {
        return TachyonSimulator.countSplits(manifold);
    }

    @Override
    public Object part2(Manifold manifold) {
        return TachyonSimulator.countTimelines(manifold);
    }
}
