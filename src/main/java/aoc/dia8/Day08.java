package aoc.dia8;

import aoc.core.Day;
import aoc.dia8.model.CircuitAnalyzer;
import aoc.dia8.model.Point3D;

import java.util.List;

public class Day08 implements Day<List<Point3D>> {

    private static final int PART1_THRESHOLD = 1000;

    @Override
    public int number() {
        return 8;
    }

    @Override
    public List<Point3D> parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(List<Point3D> points) {
        return CircuitAnalyzer.topThreeProduct(points, PART1_THRESHOLD);
    }

    @Override
    public Object part2(List<Point3D> points) {
        return CircuitAnalyzer.lastPairXProduct(points);
    }
}
