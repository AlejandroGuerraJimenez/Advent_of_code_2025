package aoc;

import aoc.core.DayRunner;
import aoc.core.InputReader;
import aoc.dia1.Day01;
import aoc.dia2.Day02;
import aoc.dia3.Day03;
import aoc.registry.DayRegistry;

import java.util.Map;
import java.util.Scanner;

public class Main {

    static void main(String[] args) {
        int dayNumber = args.length > 0
                ? Integer.parseInt(args[0])
                : askDayNumber();

        DayRegistry registry = buildRegistry();
        InputReader inputReader = new InputReader();

        DayRunner runner = new DayRunner(registry, inputReader);

        runner.run(dayNumber);
    }

    private static int askDayNumber() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("¿Qué día quieres ejecutar? ");
        return Integer.parseInt(scanner.nextLine().trim());
    }

    private static DayRegistry buildRegistry() {

        return new DayRegistry(
                Map.of(
                    1, new Day01(),
                    2, new Day02(),
                    3, new Day03()
                )
        );
    }
}
