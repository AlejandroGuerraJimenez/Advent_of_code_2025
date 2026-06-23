package aoc.core;

import aoc.registry.DayRegistry;

public class DayRunner {

    private final DayRegistry registry;
    private final InputReader inputReader;

    public DayRunner(DayRegistry registry, InputReader inputReader){
        this.registry = registry;
        this.inputReader = inputReader;
    }

    public void run(int dayNumber) {
        String input = inputReader.read(dayNumber);
        Day day = registry.get(dayNumber);

        System.out.println(day.part1(input));
        System.out.println(day.part2(input));
    }

}
