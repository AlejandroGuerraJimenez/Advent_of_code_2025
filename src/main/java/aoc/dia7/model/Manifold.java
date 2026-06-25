package aoc.dia7.model;

import aoc.parse.TextGrid;

/**
 * El campo de tachyones. Envuelve un {@link TextGrid} y añade el acceso basado
 * en {@link Position} y la localización del punto de partida 'S'.
 */
public record Manifold(TextGrid grid) {

    public int height() { return grid.height(); }

    public int width() { return grid.width(); }

    public char at(Position p) { return grid.at(p.row(), p.col()); }

    public boolean inBounds(Position p) { return grid.inBounds(p.row(), p.col()); }

    public Position start() {
        for (int r = 0; r < height(); r++)
            for (int c = 0; c < width(); c++)
                if (at(new Position(r, c)) == 'S') return new Position(r, c);
        throw new IllegalStateException("No se encontró la posición inicial 'S'");
    }
}
