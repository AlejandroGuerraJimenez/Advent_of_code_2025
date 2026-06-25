package aoc.dia10.optimization;

import java.util.Arrays;
import java.util.stream.IntStream;

/** Simplex exacto de dos fases con aritmética {@link Rational}. */
public final class SimplexSolver {

    private SimplexSolver() {}

    /** Resuelve el LP; devuelve {@code null} si no es factible. */
    public static Rational[] solve(Rational[][] a, Rational[] b, Rational[] c, int k, int n) {
        for (Rational bi : b) if (bi.neg()) return null;
        int cols = n + k;
        Rational[][] tab = buildTab(a, b, k, n, cols);
        int[] basis = IntStream.range(n, cols).toArray();
        return runPhases(tab, basis, c, k, n, cols);
    }

    private static Rational[] runPhases(Rational[][] tab, int[] basis, Rational[] c, int k, int n, int cols) {
        if (!phase1(tab, basis, k, n, cols)) return null;
        setupP2(tab, basis, c, k, n, cols);
        simplex(tab, basis, k, cols, n);
        return extractSolution(tab, basis, k, n, cols);
    }

    private static boolean phase1(Rational[][] tab, int[] basis, int k, int n, int cols) {
        simplex(tab, basis, k, cols, cols);
        if (tab[k][cols].neg()) return false;
        expelArtificials(tab, basis, k, n, cols);
        return true;
    }

    private static Rational[] extractSolution(Rational[][] tab, int[] basis, int k, int n, int cols) {
        Rational[] x = new Rational[n];
        Arrays.fill(x, Rational.ZERO);
        for (int i = 0; i < k; i++) if (basis[i] < n) x[basis[i]] = tab[i][cols];
        return x;
    }

    /**
     * Tras la Fase 1, expulsa artificiales degeneradas de la base antes de la Fase 2.
     */
    private static void expelArtificials(Rational[][] tab, int[] basis, int k, int n, int cols) {
        for (int i = 0; i < k; i++)
            if (basis[i] >= n) expelRow(tab, basis, k, n, cols, i);
    }

    private static void expelRow(Rational[][] tab, int[] basis, int k, int n, int cols, int i) {
        int pivot = firstNonZero(tab[i], n);
        if (pivot >= 0) pivotRow(tab, basis, k, cols, i, pivot);
    }

    private static int firstNonZero(Rational[] row, int n) {
        for (int j = 0; j < n; j++) if (!row[j].zero()) return j;
        return -1;
    }

    private static Rational[][] buildTab(Rational[][] a, Rational[] b, int k, int n, int cols) {
        Rational[][] tab = new Rational[k + 1][cols + 1];
        for (Rational[] row : tab) Arrays.fill(row, Rational.ZERO);
        fillConstraints(tab, a, b, k, n, cols);
        buildPhase1Objective(tab, k, n, cols);
        return tab;
    }

    private static void fillConstraints(Rational[][] tab, Rational[][] a, Rational[] b, int k, int n, int cols) {
        for (int i = 0; i < k; i++) {
            System.arraycopy(a[i], 0, tab[i], 0, n);
            tab[i][n + i] = Rational.ONE;
            tab[i][cols]  = b[i];
        }
    }

    private static void buildPhase1Objective(Rational[][] tab, int k, int n, int cols) {
        for (int i = n; i < cols; i++) tab[k][i] = Rational.ONE;
        for (int i = 0; i < k; i++)
            for (int c = 0; c <= cols; c++) tab[k][c] = tab[k][c].sub(tab[i][c]);
    }

    private static void setupP2(Rational[][] tab, int[] basis, Rational[] c, int k, int n, int cols) {
        Arrays.fill(tab[k], Rational.ZERO);
        System.arraycopy(c, 0, tab[k], 0, n);
        for (int i = 0; i < k; i++) reduceBasisCost(tab, basis, c, k, i, n, cols);
    }

    private static void reduceBasisCost(Rational[][] tab, int[] basis, Rational[] c, int k, int i, int n, int cols) {
        Rational bc = (basis[i] < n) ? c[basis[i]] : Rational.ZERO;
        if (bc.zero()) return;
        for (int col = 0; col <= cols; col++) tab[k][col] = tab[k][col].sub(bc.mul(tab[i][col]));
    }

    private static void simplex(Rational[][] tab, int[] basis, int k, int cols, int maxCol) {
        for (int e = enter(tab, k, maxCol); e >= 0; e = enter(tab, k, maxCol)) {
            int l = leave(tab, k, cols, e);
            if (l < 0) break;
            pivotRow(tab, basis, k, cols, l, e);
        }
    }

    /** Regla de Bland: columna de menor índice con coste reducido estrictamente negativo. */
    private static int enter(Rational[][] tab, int k, int maxCol) {
        for (int j = 0; j < maxCol; j++) if (tab[k][j].neg()) return j;
        return -1;
    }

    private static int leave(Rational[][] tab, int k, int cols, int e) {
        int l = -1;
        for (int i = 0; i < k; i++)
            if (tab[i][e].pos()) l = better(tab, cols, e, l, i);
        return l;
    }

    private static int better(Rational[][] tab, int cols, int e, int l, int i) {
        if (l < 0) return i;
        return tab[i][cols].div(tab[i][e]).lt(tab[l][cols].div(tab[l][e])) ? i : l;
    }

    private static void pivotRow(Rational[][] tab, int[] basis, int k, int cols, int l, int e) {
        basis[l] = e;
        normaliseRow(tab[l], tab[l][e], cols);
        for (int i = 0; i <= k; i++) if (i != l) eliminate(tab[i], tab[l], e, cols);
    }

    private static void normaliseRow(Rational[] row, Rational pivot, int cols) {
        for (int c = 0; c <= cols; c++) row[c] = row[c].div(pivot);
    }

    private static void eliminate(Rational[] row, Rational[] pivot, int e, int cols) {
        Rational f = row[e];
        if (f.zero()) return;
        for (int c = 0; c <= cols; c++) row[c] = row[c].sub(f.mul(pivot[c]));
    }
}
