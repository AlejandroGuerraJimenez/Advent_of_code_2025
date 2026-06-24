package aoc.dia10.model;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Exact-rational Branch-and-Bound ILP solver for Day 10 Part 2.
 * Uses a two-phase simplex with BigInteger fractions to avoid all floating-point
 * errors. Critically, degenerate artificials are expelled from the basis after
 * Phase 1 and before Phase 2 to prevent constraint violations.
 */
public class JoltageSolver {

    // ── Exact rational arithmetic ─────────────────────────────────────────────
    private static final class Q {
        static final Q ZERO = new Q(BigInteger.ZERO, BigInteger.ONE);
        static final Q ONE  = new Q(BigInteger.ONE,  BigInteger.ONE);

        final BigInteger n, d; // d > 0, gcd(|n|, d) = 1

        private Q(BigInteger n, BigInteger d) { this.n = n; this.d = d; }

        static Q of(long v) {
            return v == 0 ? ZERO : v == 1 ? ONE : new Q(BigInteger.valueOf(v), BigInteger.ONE);
        }

        static Q make(BigInteger n, BigInteger d) {
            if (d.signum() < 0) { n = n.negate(); d = d.negate(); }
            if (n.signum() == 0) return ZERO;
            BigInteger g = n.abs().gcd(d);
            return new Q(n.divide(g), d.divide(g));
        }

        Q add(Q o) { return make(n.multiply(o.d).add(o.n.multiply(d)),        d.multiply(o.d)); }
        Q sub(Q o) { return make(n.multiply(o.d).subtract(o.n.multiply(d)),    d.multiply(o.d)); }
        Q mul(Q o) { return make(n.multiply(o.n),                              d.multiply(o.d)); }
        Q div(Q o) { return make(n.multiply(o.d),                              d.multiply(o.n)); }

        boolean neg()  { return n.signum() < 0; }
        boolean zero() { return n.signum() == 0; }
        boolean pos()  { return n.signum() > 0; }

        BigInteger floor() {
            if (n.signum() >= 0) return n.divide(d);
            return n.subtract(d).add(BigInteger.ONE).divide(d);
        }

        boolean isInt() { return n.remainder(d.abs()).signum() == 0; }

        boolean lt(Q o) { return n.multiply(o.d).compareTo(o.n.multiply(d)) < 0; }
    }

    // ── Public API ────────────────────────────────────────────────────────────
    public static int minPresses(Machine m) {
        int k = m.joltages().size(), n = m.buttons().size();
        Q[][] A = buildA(m, k, n);
        Q[]   b = m.joltages().stream().map(Q::of).toArray(Q[]::new);
        Q[]   c = new Q[n]; Arrays.fill(c, Q.ONE);
        return bb(A, b, c, k, n, 0);
    }

    // ── B&B ──────────────────────────────────────────────────────────────────
    private static int bb(Q[][] A, Q[] b, Q[] c, int k, int n, int lbSum) {
        Q[] x = solveLP(A, b, c, k, n);
        if (x == null) return Integer.MAX_VALUE;
        int fj = fracVar(x, c, n);
        if (fj < 0) return lbSum + objVal(x, c, n);
        return Math.min(branchRight(A, b, c, k, n, lbSum, x, fj),
                        branchLeft (A, b, c, k, n, lbSum, x, fj));
    }

    private static int branchRight(Q[][] A, Q[] b, Q[] c, int k, int n, int lbSum, Q[] x, int fj) {
        long fv = x[fj].floor().longValue();
        Q    lb = Q.of(fv + 1);
        Q[]  bR = b.clone();
        for (int i = 0; i < k; i++) bR[i] = bR[i].sub(A[i][fj].mul(lb));
        for (Q bi : bR) if (bi.neg()) return Integer.MAX_VALUE;
        return bb(A, bR, c, k, n, lbSum + (int)(fv + 1));
    }

    private static int branchLeft(Q[][] A, Q[] b, Q[] c, int k, int n, int lbSum, Q[] x, int fj) {
        long  fv = x[fj].floor().longValue();
        Q[][] AL = addUBRow(A, k, n, fj);
        Q[]   bL = Arrays.copyOf(b, k + 1); bL[k] = Q.of(fv);
        Q[]   cL = Arrays.copyOf(c, n + 1); cL[n] = Q.ZERO;
        return bb(AL, bL, cL, k + 1, n + 1, lbSum);
    }

    private static Q[][] addUBRow(Q[][] A, int k, int n, int fj) {
        Q[][] AL = new Q[k + 1][n + 1];
        for (int i = 0; i < k; i++) {
            AL[i] = Arrays.copyOf(A[i], n + 1);
            AL[i][n] = Q.ZERO;
        }
        AL[k] = new Q[n + 1]; Arrays.fill(AL[k], Q.ZERO);
        AL[k][fj] = Q.ONE; AL[k][n] = Q.ONE;
        return AL;
    }

    private static int fracVar(Q[] x, Q[] c, int n) {
        for (int j = 0; j < n; j++) if (c[j].pos() && !x[j].isInt()) return j;
        return -1;
    }

    private static int objVal(Q[] x, Q[] c, int n) {
        Q s = Q.ZERO;
        for (int j = 0; j < n; j++) s = s.add(c[j].mul(x[j]));
        return s.floor().intValue();
    }

    // ── Two-phase exact simplex ───────────────────────────────────────────────
    private static Q[] solveLP(Q[][] A, Q[] b, Q[] c, int k, int n) {
        for (Q bi : b) if (bi.neg()) return null;

        int    cols  = n + k;
        Q[][]  tab   = buildTab(A, b, k, n, cols);
        int[]  basis = IntStream.range(n, cols).toArray(); // start: artificials

        // Phase 1: drive sum of artificials to 0
        simplex(tab, basis, k, cols, cols);
        if (tab[k][cols].neg()) return null; // infeasible

        // Expel degenerate artificials before Phase 2
        expelArtificials(tab, basis, k, n, cols);

        // Phase 2: minimise c^T * x (over original variables only)
        setupP2(tab, basis, c, k, n, cols);
        simplex(tab, basis, k, cols, n);

        Q[] x = new Q[n]; Arrays.fill(x, Q.ZERO);
        for (int i = 0; i < k; i++) if (basis[i] < n) x[basis[i]] = tab[i][cols];
        return x;
    }

    /**
     * After Phase 1, any artificial still in the basis must be at value 0
     * (degenerate). Drive each out by pivoting in an original variable.
     * If no original variable has a non-zero coefficient in that row, the
     * constraint is linearly dependent — mark the row as inactive (set k to
     * skip it in Phase 2) by replacing the artificial with a dummy slack.
     */
    private static void expelArtificials(Q[][] tab, int[] basis, int k, int n, int cols) {
        for (int i = 0; i < k; i++) {
            if (basis[i] < n) continue; // already an original variable
            // Artificial in basis at (hopefully) 0. Pivot out with any original var.
            int pivot = -1;
            for (int j = 0; j < n; j++) {
                if (!tab[i][j].zero()) { pivot = j; break; }
            }
            if (pivot >= 0) {
                // Find which row is currently the leaving row
                // (It's row i itself since we want to pivot the artificial out)
                pivotRow(tab, basis, k, cols, i, pivot);
            }
            // If pivot == -1: row is entirely zero in original vars (redundant constraint).
            // Leave the artificial in the basis at 0; it won't affect Phase 2 since
            // basis[i] >= n means we skip it in setupP2.
        }
    }

    private static Q[][] buildTab(Q[][] A, Q[] b, int k, int n, int cols) {
        Q[][] tab = new Q[k + 1][cols + 1];
        for (Q[] row : tab) Arrays.fill(row, Q.ZERO);
        for (int i = 0; i < k; i++) {
            System.arraycopy(A[i], 0, tab[i], 0, n);
            tab[i][n + i] = Q.ONE; // artificial
            tab[i][cols]  = b[i];
        }
        for (int i = n; i < cols; i++) tab[k][i] = Q.ONE; // Phase-1 obj costs
        for (int i = 0; i < k; i++)                        // reduce to non-basic form
            for (int c = 0; c <= cols; c++)
                tab[k][c] = tab[k][c].sub(tab[i][c]);
        return tab;
    }

    private static void setupP2(Q[][] tab, int[] basis, Q[] c, int k, int n, int cols) {
        Arrays.fill(tab[k], Q.ZERO);
        System.arraycopy(c, 0, tab[k], 0, n);
        for (int i = 0; i < k; i++) {
            Q bc = (basis[i] < n) ? c[basis[i]] : Q.ZERO;
            if (bc.zero()) continue;
            for (int col = 0; col <= cols; col++)
                tab[k][col] = tab[k][col].sub(bc.mul(tab[i][col]));
        }
    }

    private static void simplex(Q[][] tab, int[] basis, int k, int cols, int maxCol) {
        for (int e = enter(tab, k, maxCol); e >= 0; e = enter(tab, k, maxCol)) {
            int l = leave(tab, k, cols, e);
            if (l < 0) break;
            pivotRow(tab, basis, k, cols, l, e);
        }
    }

    /** Bland's rule: smallest-index column with strictly negative reduced cost. */
    private static int enter(Q[][] tab, int k, int maxCol) {
        for (int j = 0; j < maxCol; j++) if (tab[k][j].neg()) return j;
        return -1;
    }

    private static int leave(Q[][] tab, int k, int cols, int e) {
        int l = -1;
        for (int i = 0; i < k; i++) {
            if (!tab[i][e].pos()) continue;
            if (l < 0) { l = i; continue; }
            if (tab[i][cols].div(tab[i][e]).lt(tab[l][cols].div(tab[l][e]))) l = i;
        }
        return l;
    }

    private static void pivotRow(Q[][] tab, int[] basis, int k, int cols, int l, int e) {
        basis[l] = e;
        Q p = tab[l][e];
        for (int c = 0; c <= cols; c++) tab[l][c] = tab[l][c].div(p);
        for (int i = 0; i <= k; i++) {
            if (i == l) continue;
            Q f = tab[i][e]; if (f.zero()) continue;
            for (int c = 0; c <= cols; c++)
                tab[i][c] = tab[i][c].sub(f.mul(tab[l][c]));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static Q[][] buildA(Machine m, int k, int n) {
        Q[][] A = new Q[k][n];
        for (int i = 0; i < k; i++)
            for (int j = 0; j < n; j++)
                A[i][j] = Q.of((m.buttons().get(j) >> i) & 1);
        return A;
    }
}
