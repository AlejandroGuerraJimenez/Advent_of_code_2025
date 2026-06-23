package aoc.dia1.model;

public class Dial {

    private static final int POSITIONS = 100;
    private static final int START = 50;

    private int position = START;

    public int rotate(Rotation r) {
        int delta = r.direction() == Direction.RIGHT ? 1 : -1;
        int count = 0;
        for (int step = 0; step < r.steps(); step++) {
            move(delta);
            if (isZero()) {
                count++;
            }
        }
        return count;
    }

    private void move(int delta) {
        position = ((position + delta) % POSITIONS + POSITIONS) % POSITIONS;
    }

    public boolean isZero() {
        return position == 0;
    }
}
