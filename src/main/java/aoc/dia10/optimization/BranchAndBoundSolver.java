package aoc.dia10.optimization;

import java.util.Arrays;

/** Branch-and-Bound sobre un LP entero resuelto con {@link SimplexSolver}. */
public final class BranchAndBoundSolver {

    private BranchAndBoundSolver() {}

    /** Minimiza c^T x sujeto a Ax = b, x >= 0 entero; {@code Integer.MAX_VALUE} si no hay solución. */
    public static int solve(Rational[][] a, Rational[] b, Rational[] c, int k, int n) {
        return bb(a, b, c, k, n, 0);
    }

    private static int bb(Rational[][] a, Rational[] b, Rational[] c, int k, int n, int lbSum) {
        Rational[] x = SimplexSolver.solve(a, b, c, k, n);
        if (x == null) return Integer.MAX_VALUE;
        int fj = fracVar(x, c, n);
        if (fj < 0) return lbSum + objVal(x, c, n);
        return Math.min(branchRight(a, b, c, k, n, lbSum, x, fj),
                        branchLeft(a, b, c, k, n, lbSum, x, fj));
    }

    private static int branchRight(Rational[][] a, Rational[] b, Rational[] c, int k, int n, int lbSum, Rational[] x, int fj) {
        long fv = x[fj].floor().longValue();
        Rational[] bR = subtractColumn(a, b, k, fj, Rational.of(fv + 1));
        if (bR == null) return Integer.MAX_VALUE;
        return bb(a, bR, c, k, n, lbSum + (int) (fv + 1));
    }

    private static Rational[] subtractColumn(Rational[][] a, Rational[] b, int k, int fj, Rational lb) {
        Rational[] bR = b.clone();
        for (int i = 0; i < k; i++) bR[i] = bR[i].sub(a[i][fj].mul(lb));
        for (Rational bi : bR) if (bi.neg()) return null;
        return bR;
    }

    private static int branchLeft(Rational[][] a, Rational[] b, Rational[] c, int k, int n, int lbSum, Rational[] x, int fj) {
        long fv = x[fj].floor().longValue();
        Rational[][] aL = addUBRow(a, k, n, fj);
        Rational[] bL = Arrays.copyOf(b, k + 1);
        bL[k] = Rational.of(fv);
        Rational[] cL = Arrays.copyOf(c, n + 1);
        cL[n] = Rational.ZERO;
        return bb(aL, bL, cL, k + 1, n + 1, lbSum);
    }

    private static Rational[][] addUBRow(Rational[][] a, int k, int n, int fj) {
        Rational[][] aL = new Rational[k + 1][];
        for (int i = 0; i < k; i++) aL[i] = withZeroSlack(a[i], n);
        aL[k] = upperBoundRow(n, fj);
        return aL;
    }

    private static Rational[] withZeroSlack(Rational[] row, int n) {
        Rational[] copy = Arrays.copyOf(row, n + 1);
        copy[n] = Rational.ZERO;
        return copy;
    }

    private static Rational[] upperBoundRow(int n, int fj) {
        Rational[] row = new Rational[n + 1];
        Arrays.fill(row, Rational.ZERO);
        row[fj] = Rational.ONE;
        row[n] = Rational.ONE;
        return row;
    }

    private static int fracVar(Rational[] x, Rational[] c, int n) {
        for (int j = 0; j < n; j++) if (c[j].pos() && !x[j].isInt()) return j;
        return -1;
    }

    private static int objVal(Rational[] x, Rational[] c, int n) {
        Rational s = Rational.ZERO;
        for (int j = 0; j < n; j++) s = s.add(c[j].mul(x[j]));
        return s.floor().intValue();
    }
}
