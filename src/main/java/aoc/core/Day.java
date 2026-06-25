package aoc.core;

/**
 * Un reto diario de Advent of Code. La entrada se parsea UNA sola vez y ambas
 * partes se resuelven sobre el modelo ya parseado, evitando reparsear.
 *
 * @param <T> tipo del modelo parseado compartido por las dos partes
 */
public interface Day<T> {

    /** Número del día (1..25). */
    int number();

    /** Convierte la entrada cruda en el modelo del día. Se ejecuta una sola vez. */
    T parse(String input);

    /** Resuelve la parte 1 sobre el modelo ya parseado. */
    Object part1(T model);

    /** Resuelve la parte 2 sobre el modelo ya parseado. */
    Object part2(T model);
}
