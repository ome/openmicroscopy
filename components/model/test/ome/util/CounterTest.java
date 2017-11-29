/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.util;

import java.math.BigInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test of the behavior of {@link Counter}.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.2
 */
public class CounterTest {

    /**
     * Test that the count stays in step as expected.
     * Given the {@code byte[]}-based implementation, crosses a couple of byte overflow boundaries in testing.
     */
    @Test
    public void testCounting() {
        final Counter actual = new Counter();
        for (int expected = 0; expected < 75000; expected++, actual.increment()) {
            Assert.assertEquals(actual.asBigInteger(), BigInteger.valueOf(expected));
        }
    }

    /**
     * Test the reset behavior of counters.
     */
    @Test
    public void testReset() {
        final Counter actual = new Counter();
        Assert.assertEquals(actual.asBigInteger(), BigInteger.ZERO);
        actual.increment();
        Assert.assertEquals(actual.asBigInteger(), BigInteger.ONE);
        actual.reset();
        Assert.assertEquals(actual.asBigInteger(), BigInteger.ZERO);
        actual.increment();
        Assert.assertEquals(actual.asBigInteger(), BigInteger.ONE);
    }
}
