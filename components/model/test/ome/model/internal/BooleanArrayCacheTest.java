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

package ome.model.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for the the {@link BooleanArrayCache}.
 * Setup and teardown provide each test method with a fresh cache.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.2
 */
public class BooleanArrayCacheTest {

    private BooleanArrayCache cache = null;

    /**
     * Set up a cache for each test method.
     */
    @BeforeMethod
    public void setupNewCache() {
        cache = new BooleanArrayCache();
    }

    /**
     * Retire the cache after each test method.
     */
    @AfterClass
    public void teardownCache() {
        cache = null;
    }

    /**
     * Generate a Boolean array from a string. The <em>last</em> character of the string corresponds to the array's zero index.
     * If passed {@code null} then also returns {@code null}.
     * @param bits a bit string
     * @return the corresponding Boolean array
     */
    private static boolean[] fromString(String bits) {
        if (bits == null) {
            return null;
        }
        bits = new StringBuilder(bits).reverse().toString();
        final boolean[] rv = new boolean[bits.length()];
        for (int index = 0; index < rv.length; index++) {
            rv[index] = bits.charAt(index) != '0';
        }
        return rv;
    }

    /**
     * Test for cache hits with bit-strings that differ only in how much {@code false}-padding they have at higher indices.
     * @param x one array
     * @param y another array
     */
    @Test(dataProvider = "similar arrays")
    public void testSimilar(String x, String y) {
        final boolean[] cachedX = cache.getArrayFor(fromString(x));
        final boolean[] cachedY = cache.getArrayFor(fromString(y));
        Assert.assertTrue(cachedX == cachedY);
    }

    /**
     * Test for cache misses with bit-strings that differ in ways beyond how much {@code false}-padding they have at higher indices.
     * @param x one array
     * @param y another array
     */
    @Test(dataProvider = "differing arrays")
    public void testDifferent(String x, String y) {
        final boolean[] cachedX = cache.getArrayFor(fromString(x));
        final boolean[] cachedY = cache.getArrayFor(fromString(y));
        Assert.assertFalse(cachedX == cachedY);
    }

    /**
     * Turn sets of bit-strings into ordered pairs of different bit-strings.
     * @param sets of bit-strings
     * @return ordered pairs of bit-strings
     */
    private static Object[][] providePairs(Object[][] sets) {
        final List<Object[]> pairs = new ArrayList<Object[]>();
        for (final Object[] set : sets) {
            for (int index1 = 0; index1 < set.length; index1++) {
                for (int index2 = 0; index2 < set.length; index2++) {
                    if (index1 != index2) {
                        pairs.add(new Object[] {set[index1], set[index2]});
                    }
                }
            }
        }
        return pairs.toArray(new Object[pairs.size()][]);
    }

    /**
     * @return test cases for {@link #testSimilar(String, String)}
     */
    @DataProvider(name = "similar arrays")
    public Object[][] provideSimilar() {
        final String[][] sets = new String[][] {
                {null, "", "0", "00"},
                {"1", "01", "001"},
                {"10", "010"},
                {"101", "0101"},
                {"110", "0110"}};
        return providePairs(sets);
    }

    /**
     * @return test cases for {@link #testDifferent(String, String)}
     */
    @DataProvider(name = "differing arrays")
    public Object[][] provideDifferent() {
        final String[][] sets = new String[][] {
                {"0", "1", "10", "100"},
                {"01", "10", "11"},
                {"101", "010"},
                {"10", "01", "1010", "0101"}};
        return providePairs(sets);
    }

    /**
     * Test that array mutation is detected by the cache.
     */
    @Test(expectedExceptions = IllegalStateException.class)
    public void testCacheViolation() {
        boolean[] array = new boolean[] {false, true, false, true};
        cache.getArrayFor(array);
        array[0] = true;
        array = new boolean[] {false, true, false, true};
        cache.getArrayFor(array);
    }

    /**
     * Test that an array can be cached even when it uses all the bits of a {@code long}.
     */
    @Test
    public void testMaximumBooleans() {
        final boolean[] initialArray = new boolean[64];
        Arrays.fill(initialArray, true);
        final boolean[] cachedArray = cache.getArrayFor(initialArray);
        Assert.assertEquals(cachedArray.length, initialArray.length);
    }

    /**
     * Test that an array cannot be cached when it uses more bits than a {@code long} affords.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testTooManyBooleans() {
        final boolean[] array = new boolean[65];
        Arrays.fill(array, true);
        cache.getArrayFor(array);
    }
}
