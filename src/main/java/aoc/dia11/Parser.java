package aoc.dia11;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Parser {

    public static Map<String, List<String>> parse(String input) {
        return input.lines()
                .filter(l -> !l.isBlank())
                .collect(Collectors.toMap(
                        l -> l.substring(0, l.indexOf(':')),
                        l -> Arrays.asList(l.substring(l.indexOf(':') + 2).split(" "))
                ));
    }
}
