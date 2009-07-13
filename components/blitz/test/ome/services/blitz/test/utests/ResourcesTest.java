/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.utests;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;
import omero.util.Resources;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResourcesTest extends TestCase {

    int MAX_WAIT = 3;

    ScheduledExecutorService s;
    Resources r;
    TestEntry e;

    @BeforeMethod
    void startup() {
        s = Executors.newSingleThreadScheduledExecutor();
        r = new Resources(1, s);
    }

    @AfterMethod
    void shutdown() {
        if (r != null) {
            r.cleanup();
        }
    }

    /**
     * Can be used to pause the execution of the
     * {@link ScheduledExecutorService} by inserting a blocking task into the
     * single thread. Once {@link CountDownLatch#countDown()} is called on the
     * return value, then execution can resume.
     */
    CountDownLatch pause() throws Exception {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);
        s.execute(new Runnable() {
            public void run() {
                entered.countDown();
                try {
                    exit.await();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });
        assertTrue(entered.await(10, TimeUnit.SECONDS));
        return exit;
    }

    class TestEntry implements Resources.Entry {

        volatile CountDownLatch checkLatch = new CountDownLatch(2);
        volatile CountDownLatch cleanLatch = new CountDownLatch(1);

        volatile boolean checkValue = true;
        volatile boolean throwOnCheck = false;
        volatile boolean throwOnCleanup = false;

        public boolean checkWait() throws InterruptedException {
            return checkLatch.await(MAX_WAIT, TimeUnit.SECONDS);
        }

        public boolean cleanWait() throws InterruptedException {
            return cleanLatch.await(MAX_WAIT, TimeUnit.SECONDS);
        }

        public boolean check() {
            checkLatch.countDown();
            if (throwOnCheck) {
                throw new RuntimeException("They made me do it");
            }
            return checkValue;
        }

        public void cleanup() {
            cleanLatch.countDown();
            if (throwOnCleanup) {
                throw new RuntimeException("They made me do it again.");
            }
        }
    }

    @Test
    public void testSimple() throws Exception {
        e = new TestEntry();
        r.add(e);
        assertTrue(e.checkWait());
    }

    @Test
    public void testShouldBeRemoved() throws Exception {
        e = new TestEntry();
        r.add(e);
        assertEquals(1, r.size());
        assertTrue(e.checkWait());
        CountDownLatch resume = pause();
        e.checkValue = false;
        resume.countDown();
        assertTrue(e.cleanWait());
        assertEquals(0, r.size());
    }

    @Test
    public void testCheckFalseLeadsToRemove() throws Exception {
        e = new TestEntry();
        r.add(e);
        assertEquals(1, r.size());
        assertTrue(e.checkWait());
        CountDownLatch resume = pause();
        e.checkValue = false;
        resume.countDown();
        assertTrue(e.cleanWait());
        assertEquals(0, r.size());
    }

    @Test
    public void testCheckThrowsLeadsToRemove() throws Exception {
        e = new TestEntry();
        r.add(e);
        assertEquals(1, r.size());
        assertTrue(e.checkWait());
        CountDownLatch resume = pause();
        e.throwOnCheck = true;
        resume.countDown();
        assertTrue(e.cleanWait());
        assertEquals(0, r.size());
    }

    @Test
    public void testCleanupThrowsIsCaught() throws Exception {
        e = new TestEntry();
        r.add(e);
        assertEquals(1, r.size());
        assertTrue(e.checkWait());
        CountDownLatch resume = pause();
        e.checkValue = false; // Force cleanup.
        e.throwOnCleanup = true;
        resume.countDown();
        assertTrue(e.cleanWait());
        assertEquals(0, r.size());
    }

}
