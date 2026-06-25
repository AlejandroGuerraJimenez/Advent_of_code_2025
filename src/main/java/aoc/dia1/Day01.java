package aoc.dia1;

import aoc.core.Day;
import aoc.dia1.model.Dial;
import aoc.dia1.model.Rotation;

import java.util.List;

public class Day01 implements Day<List<Rotation>> {

    @Override
    public int number() {
        return 1;
    }

    @Override
    public List<Rotation> parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(List<Rotation> rotations) {
        Dial dial = new Dial();
        int count = 0;
        for (Rotation rotation : rotations) {
            dial.rotate(rotation);
            if (dial.isZero()) count++;
        }
        return count;
    }

    @Override
    public Object part2(List<Rotation> rotations) {
        Dial dial = new Dial();
        int count = 0;
        for (Rotation rotation : rotations) count += dial.rotate(rotation);
        return count;
    }
}
