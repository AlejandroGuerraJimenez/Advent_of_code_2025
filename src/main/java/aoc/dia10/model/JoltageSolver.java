package aoc.dia10.model;

import aoc.dia10.optimization.BranchAndBoundSolver;
import aoc.dia10.optimization.Rational;

import java.util.Arrays;

/** Fachada: adapta una {@link Machine} al ILP de joltage y lo resuelve con B&B. */
public class JoltageSolver {

    public static int minPresses(Machine m) {
        int k = m.joltages().size(), n = m.buttons().size();
        Rational[][] a = buildMatrix(m, k, n);
        Rational[] b = m.joltages().stream().map(Rational::of).toArray(Rational[]::new);
        Rational[] c = new Rational[n];
        Arrays.fill(c, Rational.ONE);
        return BranchAndBoundSolver.solve(a, b, c, k, n);
    }

    private static Rational[][] buildMatrix(Machine m, int k, int n) {
        Rational[][] a = new Rational[k][n];
        for (int i = 0; i < k; i++)
            for (int j = 0; j < n; j++)
                a[i][j] = Rational.of((m.buttons().get(j) >> i) & 1);
        return a;
    }
}
