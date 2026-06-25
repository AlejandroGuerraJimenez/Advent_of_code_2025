package aoc.dia12;

import aoc.core.Day;
import aoc.dia12.model.Packer;
import aoc.dia12.model.PuzzleInput;
import aoc.dia12.model.Region;

public class Day12 implements Day<PuzzleInput> {

    @Override
    public int number() {
        return 12;
    }

    @Override
    public PuzzleInput parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(PuzzleInput parsed) {
        long count = 0;
        for (Region region : parsed.regions())
            if (Packer.fits(region, parsed.shapes())) count++;
        return count;
    }

    @Override
    public Object part2(PuzzleInput parsed) {
        return "Estrella gratis (se obtiene completando los demás días)";
    }
}
