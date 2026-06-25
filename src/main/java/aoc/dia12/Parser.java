package aoc.dia12;

import aoc.dia12.adapter.PuzzleAdapter;
import aoc.dia12.model.PuzzleInput;

/** Fachada de parseo del día 12; delega en {@link PuzzleAdapter}. */
public final class Parser {

    private Parser() {}

    public static PuzzleInput parse(String input) {
        return PuzzleAdapter.parse(input);
    }
}
