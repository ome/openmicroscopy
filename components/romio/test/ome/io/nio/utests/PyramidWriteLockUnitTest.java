/*
 *   $Id$
 *
 *   Copyright 2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio.utests;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ome.conditions.LockTimeout;
import ome.io.bioformats.BfPyramidPixelBuffer;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the locking logic for creating {@link BfPyramidPixelBuffer} instances.
 * @see ticket:5083
 * @since 4.3
 */
public class PyramidWriteLockUnitTest extends AbstractPyramidPixelBufferUnitTest {

    @BeforeMethod
    public void setup() {
        createService();
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (pixelBuffer != null) {
            pixelBuffer.close();
        }
        deleteRoot();
    }

    /**
     * Tests that creation of a pyramid pixel buffer is guarded by a write lock.
     * While writing is taking place, no other pyr. pixel buffer can be created.
     * After the first read, the writer is cleaned up including releasing the
     * lock.
     *
     * @see ticket:5083
     */
    @Test(groups = "ticket:5083", expectedExceptions = LockTimeout.class)
    public void testPyramidWriteLock() throws Exception {
        pixelBuffer = service._getPixelBuffer(pixels, true);
        final CountDownLatch latch = new CountDownLatch(1);
        final Runnable run = new Runnable() {
            public void run() {
                try {
                    latch.await(10000, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace(); // ok.
                }
            }
        };

        Thread t = new Thread() {
            public void run() {
                writeTiles(new ArrayList<String>(), run);
            };
        };
        t.start();

        BfPyramidPixelBuffer pixelBuffer2 = null;
        try {
            pixelBuffer2 = (BfPyramidPixelBuffer) service._getPixelBuffer(pixels, true);
        } finally {
            latch.countDown();
            t.join();
            if (pixelBuffer2 != null) {
               pixelBuffer2.close();
            }
        }

    }
}
