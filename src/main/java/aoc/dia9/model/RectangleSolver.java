package aoc.dia9.model;

import java.util.List;

public class RectangleSolver {

    public static long maxArea(List<Tile> tiles) {
        long max = 0;
        for (int i = 0; i < tiles.size(); i++)
            for (int j = i + 1; j < tiles.size(); j++)
                max = Math.max(max, area(tiles.get(i), tiles.get(j)));
        return max;
    }

    public static long maxValidArea(List<Tile> tiles) {
        CompressedPolygon poly = new CompressedPolygon(tiles);
        long max = 0;
        for (int i = 0; i < tiles.size(); i++)
            for (int j = i + 1; j < tiles.size(); j++)
                if (poly.isValid(tiles.get(i), tiles.get(j))) max = Math.max(max, area(tiles.get(i), tiles.get(j)));
        return max;
    }

    private static long area(Tile a, Tile b) {
        return (long) (Math.abs(a.x() - b.x()) + 1) * (Math.abs(a.y() - b.y()) + 1);
    }
}
