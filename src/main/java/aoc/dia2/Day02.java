package aoc.dia2;

import aoc.core.Day;
import aoc.dia2.model.IdRange;
import aoc.dia2.model.InvalidIdChecker;

import java.util.List;
import java.util.function.LongPredicate;

public class Day02 implements Day {

    @Override
    public String part1(String input) {
        return sumInvalidIds(Parser.parse(input), InvalidIdChecker::isInvalid);
    }

    @Override
    public String part2(String input) {
        return sumInvalidIds(Parser.parse(input), InvalidIdChecker::isInvalidExtended);
    }

    private String sumInvalidIds(List<IdRange> ranges, LongPredicate checker) {
        long sum = ranges.stream()
                .flatMap(range -> InvalidIdChecker.findInvalidIdsIn(range, checker).stream())
                .mapToLong(Long::longValue)
                .sum();
        return String.valueOf(sum);
    }
}
