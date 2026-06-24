package aoc.dia10.model;

import java.util.List;

public record Machine(int lights, int target, List<Integer> buttons, List<Integer> joltages) {}
