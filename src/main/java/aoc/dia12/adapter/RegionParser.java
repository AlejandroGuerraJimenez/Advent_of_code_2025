package aoc.dia12.adapter;

import aoc.dia12.model.Region;

/** Adapta una línea de región ({@code WxH: counts}) a un {@link Region}. */
public final class RegionParser {

    private RegionParser() {}

    public static Region parse(String line, int shapeCount) {
        int colon = line.indexOf(':');
        int[] wh = parseDimensions(line.substring(0, colon));
        int[] counts = parseCounts(line.substring(colon + 1), shapeCount);
        return new Region(wh[0], wh[1], counts);
    }

    private static int[] parseDimensions(String dims) {
        String[] wh = dims.split("x");
        return new int[]{Integer.parseInt(wh[0].trim()), Integer.parseInt(wh[1].trim())};
    }

    private static int[] parseCounts(String rest, int shapeCount) {
        String[] tokens = rest.trim().split("\\s+");
        int[] counts = new int[shapeCount];
        for (int s = 0; s < shapeCount && s < tokens.length; s++)
            counts[s] = Integer.parseInt(tokens[s]);
        return counts;
    }
}
