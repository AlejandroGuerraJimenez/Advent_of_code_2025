package aoc.dia12.model;

/**
 * Una región bajo un árbol: una rejilla de ancho x alto más el número de regalos
 * de cada índice de figura que deben colocarse dentro.
 */
public record Region(int width, int height, int[] counts) {

    public int area() {
        return width * height;
    }
}
