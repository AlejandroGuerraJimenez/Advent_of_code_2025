package aoc.dia10.optimization;

import java.math.BigInteger;

/** Fracción exacta con aritmética racional (numerador/denominador normalizados). */
public final class Rational {

    public static final Rational ZERO = new Rational(BigInteger.ZERO, BigInteger.ONE);
    public static final Rational ONE  = new Rational(BigInteger.ONE,  BigInteger.ONE);

    private final BigInteger n, d; // d > 0, gcd(|n|, d) = 1

    private Rational(BigInteger n, BigInteger d) { this.n = n; this.d = d; }

    public static Rational of(long v) {
        return v == 0 ? ZERO : v == 1 ? ONE : new Rational(BigInteger.valueOf(v), BigInteger.ONE);
    }

    public static Rational make(BigInteger n, BigInteger d) {
        if (d.signum() < 0) { n = n.negate(); d = d.negate(); }
        if (n.signum() == 0) return ZERO;
        BigInteger g = n.abs().gcd(d);
        return new Rational(n.divide(g), d.divide(g));
    }

    public Rational add(Rational o) { return make(n.multiply(o.d).add(o.n.multiply(d)),        d.multiply(o.d)); }
    public Rational sub(Rational o) { return make(n.multiply(o.d).subtract(o.n.multiply(d)),    d.multiply(o.d)); }
    public Rational mul(Rational o) { return make(n.multiply(o.n),                              d.multiply(o.d)); }
    public Rational div(Rational o) { return make(n.multiply(o.d),                              d.multiply(o.n)); }

    public boolean neg()  { return n.signum() < 0; }
    public boolean zero() { return n.signum() == 0; }
    public boolean pos()  { return n.signum() > 0; }

    public BigInteger floor() {
        if (n.signum() >= 0) return n.divide(d);
        return n.subtract(d).add(BigInteger.ONE).divide(d);
    }

    public boolean isInt() { return n.remainder(d.abs()).signum() == 0; }

    public boolean lt(Rational o) { return n.multiply(o.d).compareTo(o.n.multiply(d)) < 0; }
}
