package aoc;

import aoc.core.DayRunner;
import aoc.core.InputReader;
import aoc.dia1.Day01;
import aoc.dia2.Day02;
import aoc.dia3.Day03;
import aoc.dia4.Day04;
import aoc.dia5.Day05;
import aoc.dia6.Day06;
import aoc.dia7.Day07;
import aoc.dia8.Day08;
import aoc.dia9.Day09;
import aoc.dia10.Day10;
import aoc.dia11.Day11;
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
                Map.ofEntries(
                    Map.entry(1,  new Day01()),
                    Map.entry(2,  new Day02()),
                    Map.entry(3,  new Day03()),
                    Map.entry(4,  new Day04()),
                    Map.entry(5,  new Day05()),
                    Map.entry(6,  new Day06()),
                    Map.entry(7,  new Day07()),
                    Map.entry(8,  new Day08()),
                    Map.entry(9,  new Day09()),
                    Map.entry(10, new Day10()),
                    Map.entry(11, new Day11())
                )
        );
    }
}
