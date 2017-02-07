/*
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.SimpleBackOff;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the locking logic for creating {@link BfPyramidPixelBuffer} instances.
 *
 * @see ticket:5910
 * @since Beta4.3.1
 */
@Test(groups = "ticket:5910")
public class SimpleBackOffUnitTest {

    public void testSimple() {
        long start = System.currentTimeMillis();
        SimpleBackOff backOff = new SimpleBackOff();
        long stop = System.currentTimeMillis();
        int count = backOff.getCount();
        double factor = backOff.getScalingFactor() * count;
        double warmup = backOff.getWarmUpFactor() * count;
        double actual = ((double) stop - start);
        double expected = factor + warmup;
        double delta = 1000.0; // one sec.
        Assert.assertEquals(expected, actual, delta);
    }
}
