package aoc.dia10;

import aoc.dia10.model.Machine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern BUTTON = Pattern.compile("\\(([^)]+)\\)");

    public static List<Machine> parse(String input) {
        return input.lines().filter(l -> !l.isBlank()).map(Parser::parseLine).toList();
    }

    private static Machine parseLine(String line) {
        String diagram = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
        return new Machine(diagram.length(), toTargetMask(diagram), extractButtons(line), extractJoltages(line));
    }

    private static List<Integer> extractJoltages(String line) {
        String inner = line.substring(line.indexOf('{') + 1, line.indexOf('}'));
        return Arrays.stream(inner.split(",")).map(s -> Integer.parseInt(s.trim())).toList();
    }

    private static int toTargetMask(String diagram) {
        int mask = 0;
        for (int i = 0; i < diagram.length(); i++)
            if (diagram.charAt(i) == '#') mask |= (1 << i);
        return mask;
    }

    private static List<Integer> extractButtons(String line) {
        List<Integer> buttons = new ArrayList<>();
        Matcher m = BUTTON.matcher(line);
        while (m.find()) buttons.add(toButtonMask(m.group(1)));
        return buttons;
    }

    private static int toButtonMask(String spec) {
        int mask = 0;
        for (String part : spec.split(","))
            mask |= (1 << Integer.parseInt(part.trim()));
        return mask;
    }
}
