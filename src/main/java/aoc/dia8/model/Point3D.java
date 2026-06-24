package aoc.dia8.model;

public record Point3D(int x, int y, int z) {

    public long distSq(Point3D o) {
        long dx = x - o.x, dy = y - o.y, dz = z - o.z;
        return dx * dx + dy * dy + dz * dz;
    }
}
