package aoc.dia2.model;

import aoc.parse.LongRange;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongPredicate;

public class InvalidIdChecker {

    public static boolean isInvalid(long id) {
        String s = Long.toString(id);
        if (s.length() % 2 != 0) return false;
        int half = s.length() / 2;
        return s.substring(0, half).equals(s.substring(half));
    }

    public static boolean isInvalidExtended(long id) {
        char[] digits = Long.toString(id).toCharArray();
        for (int len = 1; len <= digits.length / 2; len++)
            if (digits.length % len == 0 && isRepeatingPattern(digits, len)) return true;
        return false;
    }

    private static boolean isRepeatingPattern(char[] digits, int patternLen) {
        for (int i = 0; i < digits.length; i++)
            if (digits[i] != digits[i % patternLen]) return false;
        return true;
    }

    public static List<Long> findInvalidIdsIn(LongRange range, LongPredicate isInvalid) {
        List<Long> result = new ArrayList<>();
        for (long id = range.start(); id <= range.end(); id++)
            if (isInvalid.test(id)) result.add(id);
        return result;
    }
}
