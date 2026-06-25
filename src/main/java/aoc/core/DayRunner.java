package aoc.core;

import aoc.registry.DayRegistry;

import java.io.PrintStream;
import java.util.function.Supplier;

/**
 * Ejecuta un día: lee la entrada, la parsea una vez y resuelve ambas partes
 * aislando los errores de cada parte.
 */
public class DayRunner {

    private final DayRegistry registry;
    private final InputReader inputReader;
    private final PrintStream out;

    public DayRunner(DayRegistry registry, InputReader inputReader) {
        this(registry, inputReader, System.out);
    }

    public DayRunner(DayRegistry registry, InputReader inputReader, PrintStream out) {
        this.registry = registry;
        this.inputReader = inputReader;
        this.out = out;
    }

    public void run(int dayNumber) {
        Day<?> day = registry.get(dayNumber);
        if (day == null) {
            out.println("El día " + dayNumber + " no está implementado");
            return;
        }
        execute(day, inputReader.read(dayNumber));
    }

    private <T> void execute(Day<T> day, String input) {
        T model = parseOrReport(day, input);
        if (model == null) return;
        report(1, () -> day.part1(model));
        report(2, () -> day.part2(model));
    }

    private <T> T parseOrReport(Day<T> day, String input) {
        try {
            return day.parse(input);
        } catch (Exception e) {
            out.println("El parseo del día " + day.number() + " falló: " + e.getMessage());
            return null;
        }
    }

    private void report(int part, Supplier<Object> task) {
        try {
            out.println("Parte " + part + ": " + task.get());
        } catch (Exception e) {
            out.println("Parte " + part + " falló: " + e.getMessage());
        }
    }
}
