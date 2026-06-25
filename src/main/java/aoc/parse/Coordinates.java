package aoc.parse;

/** Parseo de coordenadas separadas por comas (2D, 3D, …). */
public final class Coordinates {

    private Coordinates() {}

    /** Convierte "a, b, c" en {a, b, c}. */
    public static int[] ints(String line) {
        String[] parts = line.split(",");
        int[] values = new int[parts.length];
        for (int i = 0; i < parts.length; i++) values[i] = Integer.parseInt(parts[i].trim());
        return values;
    }
}
