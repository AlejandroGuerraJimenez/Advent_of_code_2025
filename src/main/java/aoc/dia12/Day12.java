package aoc.dia12;

import aoc.core.Day;
import aoc.dia12.model.Packer;
import aoc.dia12.model.Region;

public class Day12 implements Day {

    @Override
    public String part1(String input) {
        Parser.Input parsed = Parser.parse(input);
        long count = 0;
        for (Region region : parsed.regions())
            if (Packer.fits(region, parsed.shapes())) count++;
        return String.valueOf(count);
    }

    @Override
    public String part2(String input) {
        return "Free star (collected by completing the other days)";
    }
}
