/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.model.units;

import java.math.BigDecimal;
import java.math.MathContext;


/**
 * Base-functor like object which can be used for preparing complex
 * equations for converting from one unit to another. Primarily these
 * classes and static methods are used via code-generation. Sympy-generated
 * strings are placed directly into code. If the proper imports are in place,
 * then a top-level {@link Conversion} (usually of type {@link Add} or
 * {@link Mul} is returned from the evaluation.
 */
public abstract class Conversion {

    // Helper static methods which prevent the need for "new"
    // in the generated code.

    /**
     * Static helper for creating {@link Add} instances.
     */
    public static Conversion Add(Conversion...conversions) {
        return new Add(conversions);
    }

    /**
     * Static helper for creating {@link Int} instances.
     */
    public static Conversion Int(long i) {
        return new Int(i);
    }

    /**
     * Static helper for creating {@link Int} instances.
     */
    public static Conversion Int(String i) {
        return new Int(i);
    }

    /**
     * Static helper for creating {@link Mul} instances.
     */
    public static Conversion Mul(Conversion...conversions) {
        return new Mul(conversions);
    }

    /**
     * Static helper for creating {@link Pow} instances.
     */
    public static Conversion Pow(long num, int den) {
        return new Pow(num, den);
    }

    /**
     * Static helper for creating {@link Rat} instances.
     */
    public static Conversion Rat(long num, long den) {
        return new Rat(num, den);
    }

    /**
     * Static helper for creating {@link Rat} instances.
     */
    public static Conversion Rat(Conversion... conversions) {
        return new Rat(conversions);
    }

    /**
     * Static helper for creating {@link Sym} instances.
     */
    public static Conversion Sym(String sym) {
        return new Sym(sym);
    }

    /**
     * Conversions, if any, which are passed into the constructor
     * of this instance. If none are passed, then the implementation
     * has a short-cut form, e.g. taking an {@link Integer} rather than
     * an {@link Int}.
     */
    protected final Conversion[] conversions;

    /**
     * Primary constructor for a {@link Conversion} object. No processing
     * happens during constructor. Instead, the {@link #convert(double)}
     * method will handle descending through the recursive structure.
     *
     * @param conversions can be empty.
     */
    public Conversion(Conversion...conversions) {
        this.conversions = conversions;
    }

    /**
     * Primary operator for {@link Conversion} instances.
     * @param original A unit value which is to be processed through the
     *    tree-like representation of this equation. Only {@link Sym} objects
     *    will actually use the "original" value.
     * @return a {@link BigDecimal} result from the calculation. If this value
     *    maps to {@link Double#NEGATIVE_INFINITY} or
     *    {@link Double#POSITIVE_INFINITY}, then a {@link BigResult} exception
     *    should be thrown before returning to clients.
     */
    public abstract BigDecimal convert(double original);

    /**
     * Sums all {@link Conversion} instances via {@link BigDecimal#add(BigDecimal)}.
     */
    public static class Add extends Conversion {

        public Add(Conversion[] conversions) {
            super(conversions);
        }

        public BigDecimal convert(double original) {
            BigDecimal big = BigDecimal.ZERO;
            for (Conversion c : conversions) {
                big = big.add(c.convert(original));
            }
            return big;
        }

    }

    /**
     * Simply is a representation of a possibly large integer.
     */
    public static class Int extends Conversion {

        private final long i;

        private final String s;

        public Int(long i) {
            this.i = i;
            this.s = null;
        }

        public Int(String s) {
            this.s = s;
            this.i = 0;
        }

        /**
         * Returns a {@link BigDecimal} representation of this int.
         * Original argument is ignored.
         */
        public BigDecimal convert(double original) {
            if (s == null) {
                return new BigDecimal(i);
            }
            return new BigDecimal(s);
        }
    }

    /**
     * Multiplies all {@link Conversion} instances via
     * {@link BigDecimal#multiply(BigDecimal)}.
     */
    public static class Mul extends Conversion {

        public Mul(Conversion[] conversions) {
            super(conversions);
        }

        public BigDecimal convert(double original) {
            BigDecimal big = BigDecimal.ONE;
            for (Conversion c : conversions) {
                big = big.multiply(c.convert(original));
            }
            return big;
        }
    }

    /**
     * Exponentiates two {@link Conversion} instances via
     * {@link BigDecimal#pow(int)}.
     */
    public static class Pow extends Conversion {

        private final long base;

        private final int exp;

        public Pow(long base, int exp) {
            this.base = base;
            this.exp = exp;
        }

        public BigDecimal convert(double original) {
            return new BigDecimal(base).pow(exp);
        }

    }

    /**
     * Divides two {@link Conversion} instances via
     * {@link BigDecimal#divide(BigDecimal, MathContext)}.
     */
    public static class Rat extends Conversion {

        private final long num, denom;

        private final boolean delay;

        public Rat(long num, long denom) {
            this.num = num;
            this.denom = denom;
            this.delay = false;
        }

        public Rat(Conversion...conversions) {
            super(conversions);
            this.num = 0;
            this.denom = 0;
            if (conversions.length != 2) {
                throw new IllegalArgumentException(
                    "Too many conversions: " +
                    conversions.length);
            }
            this.delay = true;
        }

        public BigDecimal convert(double original) {
            if (!delay) {
                return new BigDecimal(num).divide(new BigDecimal(denom),
                        MathContext.DECIMAL128);
            } else {
                return conversions[0].convert(original).divide(
                        conversions[1].convert(original),
                        MathContext.DECIMAL128);
            }
        }
    }

    /**
     * Simply represents the variable of the source unit so that
     * {@link Sym#convert(double)} just returns the value passed in.
     */
    public static class Sym extends Conversion {

        public Sym(char sym) {
            // no-op
        }

        public Sym(String sym) {
            // no-op
        }

        public BigDecimal convert(double original) {
            return new BigDecimal(original);
        }
    }
}
