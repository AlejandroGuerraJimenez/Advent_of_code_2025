package aoc.parse;

/** Rango inclusivo de enteros largos [start, end]. */
public record LongRange(long start, long end) {

    /** Parsea un token "a-b". */
    public static LongRange parse(String token) {
        String[] parts = token.trim().split("-");
        return new LongRange(Long.parseLong(parts[0].trim()), Long.parseLong(parts[1].trim()));
    }

    public boolean contains(long value) {
        return value >= start && value <= end;
    }

    /** Número de enteros del rango. */
    public long length() {
        return end - start + 1;
    }

    /** True si los rangos se solapan o son adyacentes (tocan sin hueco). */
    public boolean connectsWith(LongRange other) {
        return other.start <= end + 1 && start <= other.end + 1;
    }

    /** Unión de dos rangos (asume que conectan). */
    public LongRange union(LongRange other) {
        return new LongRange(Math.min(start, other.start), Math.max(end, other.end));
    }
}
