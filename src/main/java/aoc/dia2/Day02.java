package aoc.dia2;

import aoc.core.Day;
import aoc.dia2.model.InvalidIdChecker;
import aoc.parse.LongRange;

import java.util.List;
import java.util.function.LongPredicate;

public class Day02 implements Day<List<LongRange>> {

    @Override
    public int number() {
        return 2;
    }

    @Override
    public List<LongRange> parse(String input) {
        return Parser.parse(input);
    }

    @Override
    public Object part1(List<LongRange> ranges) {
        return sumInvalidIds(ranges, InvalidIdChecker::isInvalid);
    }

    @Override
    public Object part2(List<LongRange> ranges) {
        return sumInvalidIds(ranges, InvalidIdChecker::isInvalidExtended);
    }

    private long sumInvalidIds(List<LongRange> ranges, LongPredicate checker) {
        return ranges.stream()
                .flatMap(range -> InvalidIdChecker.findInvalidIdsIn(range, checker).stream())
                .mapToLong(Long::longValue)
                .sum();
    }
}
