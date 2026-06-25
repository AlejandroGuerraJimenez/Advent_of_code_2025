package aoc.dia5;

import aoc.dia5.model.IngredientDatabase;
import aoc.parse.LongRange;
import aoc.parse.Sections;

import java.util.List;

public class Parser {

    public static IngredientDatabase parse(String input) {
        List<List<String>> sections = Sections.splitByBlankLine(input);
        List<LongRange> ranges = sections.get(0).stream().map(LongRange::parse).toList();
        List<Long> ids = sections.get(1).stream().map(Long::parseLong).toList();
        return new IngredientDatabase(ranges, ids);
    }
}
