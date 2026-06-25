package aoc.parse;

import java.util.List;

/**
 * Rejilla de caracteres a partir de líneas de texto. El ancho es el de la fila
 * más larga; las celdas fuera de una fila corta devuelven el carácter de relleno
 * ({@code empty}), de modo que tolera filas de longitud desigual.
 */
public record TextGrid(List<String> rows, int width, char empty) {

    public static TextGrid fromLines(List<String> rows) {
        int width = rows.stream().mapToInt(String::length).max().orElse(0);
        return new TextGrid(rows, width, '.');
    }

    public int height() {
        return rows.size();
    }

    public char at(int row, int col) {
        String line = rows.get(row);
        return col < line.length() ? line.charAt(col) : empty;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < height() && col >= 0 && col < width;
    }
}
