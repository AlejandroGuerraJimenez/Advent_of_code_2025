package aoc.dia9;

import aoc.core.Day;
import aoc.dia9.model.RectangleSolver;
import aoc.dia9.model.Tile;

import java.util.List;

public class Day09 implements Day<List<Tile>> {

    @Override
    public int number() {
        return 9;
    }

    @Override
    public List<Tile> parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(List<Tile> tiles) {
        return RectangleSolver.maxArea(tiles);
    }

    @Override
    public Object part2(List<Tile> tiles) {
        return RectangleSolver.maxValidArea(tiles);
    }
}
