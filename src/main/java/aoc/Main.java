package aoc;

import aoc.core.DayRunner;
import aoc.core.InputReader;
import aoc.dia1.Day01;
import aoc.registry.DayRegistry;

import java.util.Map;

public class Main {

    public static void main(String[] args) {
        int dayNumber = args.length > 0 ? Integer.parseInt(args[0]) : 1;

        DayRegistry registry = buildRegistry();
        InputReader inputReader = new InputReader();

        DayRunner runner = new DayRunner(registry, inputReader);

        runner.run(dayNumber);
    }

    private static DayRegistry buildRegistry() {

        return new DayRegistry(
                Map.of(
                    1, new Day01()
                )
        );
    }
}
