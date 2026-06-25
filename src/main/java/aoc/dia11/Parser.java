package aoc.dia11;

import aoc.dia11.model.Graph;
import aoc.parse.Lines;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Parser {

    public static Graph parse(String input) {
        Map<String, List<String>> adjacency = Lines.nonBlank(input).stream()
                .collect(Collectors.toMap(
                        l -> l.substring(0, l.indexOf(':')),
                        l -> Arrays.asList(l.substring(l.indexOf(':') + 2).split(" "))
                ));
        return new Graph(adjacency);
    }
}
