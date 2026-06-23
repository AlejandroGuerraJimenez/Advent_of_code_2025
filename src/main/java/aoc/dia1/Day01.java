package aoc.dia1;

import aoc.core.Day;
import aoc.dia1.model.Dial;
import aoc.dia1.model.Rotation;

import java.util.List;

public class Day01 implements Day {

    @Override
    public String part1(String input) {
        List<Rotation> rotations = Parser.parse(input);
        Dial dial = new Dial();
        int count = 0;

        for (Rotation rotation : rotations) {
            dial.rotate(rotation);
            if (dial.isZero()) {
                count++;
            }
        }

        return String.valueOf(count);
    }

    @Override
    public String part2(String input) {
        List<Rotation> rotations = Parser.parse(input);
        Dial dial = new Dial();
        int count = 0;

        for (Rotation rotation : rotations) {
            count += dial.rotate(rotation);
        }

        return String.valueOf(count);
    }

}
