package aoc.dia6;

import aoc.dia6.model.MathWorksheet;
import aoc.dia6.model.Problem;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public static MathWorksheet parse(String input) {
        List<String> rows = input.lines().filter(l -> !l.isBlank()).toList();
        return new MathWorksheet(findGroups(rows).stream().map(g -> parseProblem(rows, g)).toList());
    }

    private static List<int[]> findGroups(List<String> rows) {
        int width = rows.stream().mapToInt(String::length).max().orElse(0);
        List<int[]> groups = new ArrayList<>();
        int start = -1;
        for (int c = 0; c <= width; c++)
            start = updateGroups(groups, rows, c, width, start);
        return groups;
    }

    private static int updateGroups(List<int[]> groups, List<String> rows, int c, int width, int start) {
        boolean sep = isSeparatorColumn(rows, c, width);
        if (!sep && start == -1) return c;
        if (sep && start != -1) { groups.add(new int[]{start, c - 1}); return -1; }
        return start;
    }

    private static boolean isSeparatorColumn(List<String> rows, int col, int width) {
        if (col >= width) return true;
        return rows.stream().allMatch(r -> col >= r.length() || r.charAt(col) == ' ');
    }

    public static MathWorksheet parseVertical(String input) {
        List<String> rows = input.lines().filter(l -> !l.isBlank()).toList();
        return new MathWorksheet(findGroups(rows).stream().map(g -> parseProblemVertical(rows, g)).toList());
    }

    private static Problem parseProblem(List<String> rows, int[] group) {
        char op = slice(rows.getLast(), group).trim().charAt(0);
        List<Long> numbers = rows.subList(0, rows.size() - 1).stream()
                .map(r -> slice(r, group).trim())
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .toList();
        return new Problem(numbers, op);
    }

    private static Problem parseProblemVertical(List<String> rows, int[] group) {
        char op = slice(rows.getLast(), group).trim().charAt(0);
        List<Long> numbers = buildNumbersRtl(rows.subList(0, rows.size() - 1), group);
        return new Problem(numbers, op);
    }

    private static List<Long> buildNumbersRtl(List<String> numberRows, int[] group) {
        List<Long> numbers = new ArrayList<>();
        for (int col = group[1]; col >= group[0]; col--) {
            String digits = columnDigits(numberRows, col);
            if (!digits.isEmpty()) numbers.add(Long.parseLong(digits));
        }
        return numbers;
    }

    private static String columnDigits(List<String> rows, int col) {
        StringBuilder sb = new StringBuilder();
        for (String row : rows)
            if (col < row.length() && row.charAt(col) != ' ') sb.append(row.charAt(col));
        return sb.toString();
    }

    private static String slice(String row, int[] group) {
        int from = Math.min(group[0], row.length());
        int to = Math.min(group[1] + 1, row.length());
        return from >= to ? "" : row.substring(from, to);
    }
}
