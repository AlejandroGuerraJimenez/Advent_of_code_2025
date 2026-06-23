package aoc.dia3.model;

public class JoltageCalculator {

    public static long maxJoltage(BatteryBank bank, int count) {
        return Long.parseLong(selectDigits(bank.digits(), count));
    }

    private static String selectDigits(String digits, int count) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        for (int k = 0; k < count; k++)
            start = appendBestDigit(sb, digits, start, digits.length() - (count - k)) + 1;
        return sb.toString();
    }

    private static int appendBestDigit(StringBuilder sb, String digits, int from, int to) {
        int pos = maxDigitPos(digits, from, to);
        sb.append(digits.charAt(pos));
        return pos;
    }

    private static int maxDigitPos(String digits, int from, int to) {
        int best = from;
        for (int i = from + 1; i <= to; i++)
            if (digits.charAt(i) > digits.charAt(best)) best = i;
        return best;
    }
}
