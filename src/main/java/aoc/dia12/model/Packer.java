package aoc.dia12.model;

/**
 * Decides whether all required presents fit inside a region without their solid
 * cells overlapping (empty cells are allowed: this is packing, not exact cover).
 * <p>
 * Strategy: a depth-first search driven by the first still-empty cell in scan
 * order. That cell is either covered by a present (whose first solid cell is
 * anchored there, guaranteeing no cell before it is touched) or declared a
 * permanent hole. The area check (required cells <= grid area) is a rigorous
 * necessary condition applied first, and the hole budget bounds how many cells
 * may be left empty.
 */
public final class Packer {

    private final int w;
    private final int h;
    private final boolean[][] occupied;
    private final Shape[] shapes;

    private Packer(int w, int h, Shape[] shapes) {
        this.w = w;
        this.h = h;
        this.shapes = shapes;
        this.occupied = new boolean[h][w];
    }

    public static boolean fits(Region region, Shape[] shapes) {
        int required = requiredCells(region, shapes);
        if (required > region.area()) return false;
        Packer packer = new Packer(region.width(), region.height(), shapes);
        return packer.search(0, region.counts().clone(), region.area() - required);
    }

    private static int requiredCells(Region region, Shape[] shapes) {
        int required = 0;
        for (int s = 0; s < shapes.length; s++) required += region.counts()[s] * shapes[s].cells();
        return required;
    }

    private boolean search(int from, int[] remaining, int holeBudget) {
        int pos = nextEmpty(from);
        if (allPlaced(remaining)) return true;
        if (pos >= w * h) return false;
        return tryPresents(pos, remaining, holeBudget) || tryHole(pos, remaining, holeBudget);
    }

    private int nextEmpty(int from) {
        int pos = from;
        while (pos < w * h && occupied[pos / w][pos % w]) pos++;
        return pos;
    }

    private boolean tryPresents(int pos, int[] remaining, int holeBudget) {
        for (int s = 0; s < shapes.length; s++)
            if (remaining[s] > 0 && tryShape(s, pos, remaining, holeBudget)) return true;
        return false;
    }

    private boolean tryShape(int s, int pos, int[] remaining, int holeBudget) {
        for (int[][] orientation : shapes[s].orientations())
            if (placeable(orientation, pos) && attempt(s, orientation, pos, remaining, holeBudget)) return true;
        return false;
    }

    private boolean attempt(int s, int[][] orientation, int pos, int[] remaining, int holeBudget) {
        toggle(orientation, pos, true);
        remaining[s]--;
        boolean ok = search(pos, remaining, holeBudget);
        remaining[s]++;
        toggle(orientation, pos, false);
        return ok;
    }

    private boolean tryHole(int pos, int[] remaining, int holeBudget) {
        return holeBudget > 0 && search(pos + 1, remaining, holeBudget - 1);
    }

    private boolean placeable(int[][] orientation, int pos) {
        for (int[] cell : absoluteCells(orientation, pos))
            if (outOfBounds(cell) || occupied[cell[0]][cell[1]]) return false;
        return true;
    }

    private void toggle(int[][] orientation, int pos, boolean value) {
        for (int[] cell : absoluteCells(orientation, pos)) occupied[cell[0]][cell[1]] = value;
    }

    private int[][] absoluteCells(int[][] orientation, int pos) {
        int rOff = pos / w - orientation[0][0], cOff = pos % w - orientation[0][1];
        int[][] cells = new int[orientation.length][];
        for (int i = 0; i < orientation.length; i++)
            cells[i] = new int[]{orientation[i][0] + rOff, orientation[i][1] + cOff};
        return cells;
    }

    private boolean outOfBounds(int[] cell) {
        return cell[0] < 0 || cell[0] >= h || cell[1] < 0 || cell[1] >= w;
    }

    private boolean allPlaced(int[] remaining) {
        for (int n : remaining) if (n != 0) return false;
        return true;
    }
}
