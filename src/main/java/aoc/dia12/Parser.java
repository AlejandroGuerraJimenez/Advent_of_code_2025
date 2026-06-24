package aoc.dia12;

import aoc.dia12.model.Region;
import aoc.dia12.model.Shape;

import java.util.ArrayList;
import java.util.List;

public final class Parser {

    public record Input(Shape[] shapes, List<Region> regions) {}

    public static Input parse(String input) {
        String[] lines = input.replace("\r\n", "\n").split("\n");
        List<Shape> shapes = new ArrayList<>();
        List<Region> regions = new ArrayList<>();

        int i = 0;
        while (i < lines.length) {
            String line = lines[i].trim();
            if (line.matches("\\d+:")) {
                boolean[][] grid = new boolean[3][];
                for (int r = 0; r < 3; r++) {
                    String row = lines[i + 1 + r];
                    grid[r] = new boolean[row.length()];
                    for (int c = 0; c < row.length(); c++) grid[r][c] = row.charAt(c) == '#';
                }
                shapes.add(Shape.from(grid));
                i += 4;
            } else if (line.matches("\\d+x\\d+:.*")) {
                regions.add(parseRegion(line, shapes.size()));
                i++;
            } else {
                i++;
            }
        }
        return new Input(shapes.toArray(new Shape[0]), regions);
    }

    private static Region parseRegion(String line, int shapeCount) {
        String dims = line.substring(0, line.indexOf(':'));
        String[] wh = dims.split("x");
        int width = Integer.parseInt(wh[0].trim());
        int height = Integer.parseInt(wh[1].trim());

        String[] tokens = line.substring(line.indexOf(':') + 1).trim().split("\\s+");
        int[] counts = new int[shapeCount];
        for (int s = 0; s < shapeCount && s < tokens.length; s++) counts[s] = Integer.parseInt(tokens[s]);
        return new Region(width, height, counts);
    }
}
