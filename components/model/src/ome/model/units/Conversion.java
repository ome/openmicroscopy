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
 * TODO
 */
public abstract class Conversion {

    // Helper static methods which prevent the need for "new"
    // in the generated code.

    public static Conversion Add(Conversion...conversions) {
        return new Add(conversions);
    }

    public static Conversion Int(long i) {
        return new Int(i);
    }

    public static Conversion Int(String i) {
        return new Int(i);
    }

    public static Conversion Mul(Conversion...conversions) {
        return new Mul(conversions);
    }

    public static Conversion Pow(long num, int den) {
        return new Pow(num, den);
    }

    public static Conversion Rat(long num, long den) {
        return new Rat(num, den);
    }

    public static Conversion Rat(Conversion... conversions) {
        return new Rat(conversions);
    }

    public static Conversion Sym(String sym) {
        return new Sym(sym);
    }

    protected final Conversion[] conversions;

    public Conversion(Conversion...conversions) {
        this.conversions = conversions;
    }

    public abstract BigDecimal convert(double original);

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

        public BigDecimal convert(double original) {
            if (s == null) {
                return new BigDecimal(i);
            }
            return new BigDecimal(s);
        }
    }

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
                throw new IllegalArgumentException("Too man conversions: " +
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
