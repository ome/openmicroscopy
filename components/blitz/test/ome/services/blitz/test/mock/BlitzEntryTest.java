/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.mock;

import ome.services.blitz.Entry;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.Test;

/**
 * Creates sessions and tests the various ways they can be destroyed. Initially
 * (Oct. 2008) this was used to manually inspect in a profiler whether or not
 * all related instances were being properly cleaned up.
 */
public class BlitzEntryTest extends MockObjectTestCase {

    @Test(groups = "integration")
    public void testCreation() throws Exception {
        final Entry e = new Entry("OMERO.blitz.test");
        class Work extends Thread {
            @Override
            public void run() {
                e.start();
            }
        }
        Work work = new Work();
        work.start();

        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 5000L) {
            // try
        }

        // Shutdown & test
        e.shutdown(false);

        assertEquals(0, e.status());
    }

}
