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

import java.math.BigDecimal;

import ome.model.units.Conversion;

import org.testng.Assert;
import org.testng.annotations.Test;


public class ConversionsTest {

    @Test
    public void testSimpleAdd() throws Exception {
        Conversion add = Conversion.Add(Conversion.Rat(1, 2), Conversion.Rat(1,2));
        double whole = add.convert(-1).doubleValue(); // -1 is ignored
        Assert.assertEquals(whole, 1.0, 0.0001);
    }

    @Test
    public void testSimpleMul() throws Exception {
        Conversion mul = Conversion.Mul(Conversion.Int(1000000), Conversion.Sym("megas"));
        double seconds = mul.convert(5.0).doubleValue();
        Assert.assertEquals(seconds, 5000000.0, 0.0001);
    }

    @Test
    public void testSimpleInt() throws Exception {
        Conversion i = Conversion.Int(123);
        double x = i.convert(-1).doubleValue(); // -1 is ignored
        Assert.assertEquals(x, 123.0, 0.0001);
    }

    @Test
    public void testBigInt() throws Exception {
        String big = "123456789012345678901234567891234567890";
        big = big + big + big + big + big;
        Conversion i = Conversion.Mul(Conversion.Int(big), Conversion.Int(big));
        BigDecimal rv = i.convert(-1);
        Assert.assertEquals(rv.doubleValue(), Double.POSITIVE_INFINITY);
    }

    @Test
    public void testSimplePow() throws Exception {
        Conversion p = Conversion.Pow(3, 2);
        double x = p.convert(-1).doubleValue(); // -1 is ignored
        Assert.assertEquals(x, 9.0, 0.0001);
    }

    @Test
    public void testSimpleRat() throws Exception {
        Conversion r = Conversion.Rat(1, 3);
        double x = r.convert(-1).doubleValue(); // -1 is ignored
        Assert.assertEquals(x, 0.33333333, 0.0001);
    }

    @Test
    public void testDelayedRat() throws Exception {
        Conversion r = Conversion.Rat(Conversion.Int(1), Conversion.Int(3));
        double x = r.convert(-1).doubleValue(); // -1 is ignored
        Assert.assertEquals(x, 0.33333333, 0.0001);
    }

    @Test
    public void testSimpleSym() throws Exception {
        Conversion sym = Conversion.Sym("x");
        double x = sym.convert(5.0).doubleValue();
        Assert.assertEquals(x, 5.0, 0.0001);
    }
    
    @Test
    public void testFahrenheit() throws Exception {
        Conversion ftoc = Conversion.Add(Conversion.Mul(Conversion.Rat(5, 9),
                Conversion.Sym("f")), Conversion.Rat(-160, 9));
        Assert.assertEquals(ftoc.convert(32.0).doubleValue(), 0.0, 0.0001);
        Assert.assertEquals(ftoc.convert(212.0).doubleValue(), 100.0, 0.0001);
        Assert.assertEquals(ftoc.convert(-40.0).doubleValue(), -40.0, 0.0001);
    }

}