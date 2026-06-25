package aoc;

import aoc.core.DayRunner;
import aoc.core.InputReader;
import aoc.registry.DayRegistry;

import java.util.Scanner;

public class Main {

    static void main(String[] args) {
        int dayNumber = args.length > 0
                ? Integer.parseInt(args[0])
                : askDayNumber();

        DayRunner runner = new DayRunner(DayRegistry.createDefault(), new InputReader());
        runner.run(dayNumber);
    }

    private static int askDayNumber() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("¿Qué día quieres ejecutar? ");
        return Integer.parseInt(scanner.nextLine().trim());
    }
}
