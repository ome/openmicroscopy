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

package ome.model.utests;

import static ome.model.units.Conversion.Add;
import static ome.model.units.Conversion.Int;
import static ome.model.units.Conversion.Mul;
import static ome.model.units.Conversion.Pow;
import static ome.model.units.Conversion.Rat;
import static ome.model.units.Conversion.Sym;

import java.math.BigDecimal;

import junit.framework.TestCase;
import ome.model.units.Conversion;

import org.testng.annotations.Test;


public class ConversionsTest extends TestCase {

    @Test
    public void testSimpleAdd() throws Exception {
        Conversion add = Add(Rat(1, 2), Rat(1,2));
        double whole = add.convert(-1).doubleValue(); // -1 is ignored
        assertEquals(1.0, whole, 0.0001);
    }

    @Test
    public void testSimpleMul() throws Exception {
        Conversion mul = Mul(Int(1000000), Sym("megas"));
        double seconds = mul.convert(5.0).doubleValue();
        assertEquals(5000000.0, seconds, 0.0001);
    }

    @Test
    public void testSimpleInt() throws Exception {
        Conversion i = Int(123);
        double x = i.convert(-1).doubleValue(); // -1 is ignored
        assertEquals(123.0, x, 0.0001);
    }

    @Test
    public void testBigInt() throws Exception {
        String big = "123456789012345678901234567891234567890";
        big = big + big + big + big + big;
        Conversion i = Mul(Int(big), Int(big));
        BigDecimal rv = i.convert(-1);
        assertEquals(Double.POSITIVE_INFINITY, rv.doubleValue());
    }

    @Test
    public void testSimplePow() throws Exception {
        Conversion p = Pow(3, 2);
        double x = p.convert(-1).doubleValue(); // -1 is ignored
        assertEquals(9.0, x, 0.0001);
    }

    @Test
    public void testSimpleRat() throws Exception {
        Conversion r = Rat(1, 3);
        double x = r.convert(-1).doubleValue(); // -1 is ignored
        assertEquals(0.33333333, x, 0.0001);
    }

    @Test
    public void testDelayedRat() throws Exception {
        Conversion r = Rat(Int(1), Int(3));
        double x = r.convert(-1).doubleValue(); // -1 is ignored
        assertEquals(0.33333333, x, 0.0001);
    }

    @Test
    public void testSimpleSym() throws Exception {
        Conversion sym = Sym("x");
        double x = sym.convert(5.0).doubleValue();
        assertEquals(5.0, x, 0.0001);
    }
    
    @Test
    public void testFahrenheit() throws Exception {
        Conversion ftoc = Add(Mul(Rat(5, 9), Sym("f")), Rat(-160, 9));
        assertEquals(0.0, ftoc.convert(32.0).doubleValue(), 0.0001);
        assertEquals(100.0, ftoc.convert(212.0).doubleValue(), 0.0001);
        assertEquals(-40.0, ftoc.convert(-40.0).doubleValue(), 0.0001);
    }

}