package aoc.dia12.adapter;

import aoc.dia12.model.PuzzleInput;
import aoc.dia12.model.Region;
import aoc.dia12.model.Shape;

import java.util.ArrayList;
import java.util.List;

/** Adapta la entrada cruda del día 12 al {@link PuzzleInput} del dominio. */
public final class PuzzleAdapter {

    private PuzzleAdapter() {}

    public static PuzzleInput parse(String input) {
        String[] lines = input.split("\n");
        List<Shape> shapes = new ArrayList<>();
        List<Region> regions = new ArrayList<>();
        parseAll(lines, shapes, regions);
        return new PuzzleInput(shapes.toArray(new Shape[0]), regions);
    }

    private static void parseAll(String[] lines, List<Shape> shapes, List<Region> regions) {
        int i = 0;
        while (i < lines.length) i = parseEntry(lines, i, shapes, regions);
    }

    private static int parseEntry(String[] lines, int i, List<Shape> shapes, List<Region> regions) {
        String line = lines[i].trim();
        if (line.matches("\\d+:")) {
            shapes.add(ShapeParser.parse(lines, i));
            return i + 4;
        }
        if (line.matches("\\d+x\\d+:.*")) regions.add(RegionParser.parse(line, shapes.size()));
        return i + 1;
    }
}
