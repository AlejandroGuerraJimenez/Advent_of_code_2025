package aoc.dia9.geometry;

import aoc.dia9.model.Tile;

import java.util.List;

/** Rejilla booleana con el borde del polígono marcado. */
public final class BoundaryGrid {

    private final boolean[][] cells;

    public BoundaryGrid(CoordinateCompressor coords, List<Tile> tiles) {
        cells = new boolean[coords.cols()][coords.rows()];
        for (int i = 0; i < tiles.size(); i++)
            markSegment(coords, tiles.get(i), tiles.get((i + 1) % tiles.size()));
    }

    public boolean[][] cells() { return cells; }

    private void markSegment(CoordinateCompressor coords, Tile from, Tile to) {
        int c1 = coords.cx(Math.min(from.x(), to.x()));
        int c2 = coords.cx(Math.max(from.x(), to.x()));
        int r1 = coords.cy(Math.min(from.y(), to.y()));
        int r2 = coords.cy(Math.max(from.y(), to.y()));
        for (int c = c1; c <= c2; c++)
            for (int r = r1; r <= r2; r++) cells[c][r] = true;
    }
}
