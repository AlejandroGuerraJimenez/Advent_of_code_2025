package aoc.dia5.model;

public record FreshRange(long start, long end) {

    public boolean contains(long id) {
        return id >= start && id <= end;
    }
}
