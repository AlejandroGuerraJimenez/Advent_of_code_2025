package aoc.dia6.model;

public class WorksheetSolver {

    public static long grandTotal(MathWorksheet ws) {
        return ws.problems().stream().mapToLong(WorksheetSolver::solve).sum();
    }

    private static long solve(Problem p) {
        var nums = p.numbers().stream().mapToLong(Long::longValue);
        return p.operator() == '*' ? nums.reduce(1L, (a, b) -> a * b) : nums.sum();
    }
}
