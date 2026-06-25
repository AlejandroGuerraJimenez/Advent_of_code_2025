package aoc.dia9.model;

import aoc.dia9.geometry.BoundaryGrid;
import aoc.dia9.geometry.CoordinateCompressor;
import aoc.dia9.geometry.ExteriorFloodFill;
import aoc.dia9.geometry.PrefixSumGrid;

import java.util.List;

/** Fachada: comprueba si un rectángulo está totalmente dentro del polígono. */
public class CompressedPolygon {

    private final CoordinateCompressor coords;
    private final PrefixSumGrid prefix;

    public CompressedPolygon(List<Tile> tiles) {
        coords = new CoordinateCompressor(tiles);
        boolean[][] boundary = new BoundaryGrid(coords, tiles).cells();
        prefix = new PrefixSumGrid(boundary, ExteriorFloodFill.mark(boundary));
    }

    public boolean isValid(Tile a, Tile b) {
        int c1 = coords.cx(Math.min(a.x(), b.x()));
        int c2 = coords.cx(Math.max(a.x(), b.x()));
        int r1 = coords.cy(Math.min(a.y(), b.y()));
        int r2 = coords.cy(Math.max(a.y(), b.y()));
        return prefix.query(c1, r1, c2, r2) == (c2 - c1 + 1) * (r2 - r1 + 1);
    }
}
