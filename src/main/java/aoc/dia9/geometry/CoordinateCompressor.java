package aoc.dia9.geometry;

import aoc.dia9.model.Tile;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

/** Comprime coordenadas del polígono a índices de rejilla 2×. */
public final class CoordinateCompressor {

    private final int[] xs, ys;

    public CoordinateCompressor(List<Tile> tiles) {
        xs = unique(tiles, Tile::x);
        ys = unique(tiles, Tile::y);
    }

    public int cols() { return 2 * xs.length - 1; }
    public int rows() { return 2 * ys.length - 1; }

    public int cx(int x) { return 2 * Arrays.binarySearch(xs, x); }
    public int cy(int y) { return 2 * Arrays.binarySearch(ys, y); }

    private static int[] unique(List<Tile> tiles, ToIntFunction<Tile> f) {
        return tiles.stream().mapToInt(f).distinct().sorted().toArray();
    }
}
