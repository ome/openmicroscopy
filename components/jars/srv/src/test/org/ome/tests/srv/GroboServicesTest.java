/*
 * Created on Feb 26, 2005
 */
package org.ome.tests.srv;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestMonitorRunnable;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import junit.framework.TestCase;

/**
 * @author josh
 */
public class GroboServicesTest extends TestCase {

    static class Worker extends TestRunnable {
        int count;
        int sleepTime;

        public Worker() {
            count = 3;
            sleepTime = 1000;
        }

        public void runTest() throws Throwable {
            for (int i = 0; i < this.count; ++i) {
                Thread.sleep(this.sleepTime);
                System.err.println("Running...");
            }
        }
    }

    public static class Monitor extends TestMonitorRunnable {
        public Monitor() {
        }

        public void runMonitor() throws Throwable {
            assertTrue("This should be true", true);
        }
    }

    public void testServices() throws Throwable {
        TestRunnable tcs[] = { new Worker(), new Worker() };
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tcs);
        mttr.runTestRunnables(2 * 60 * 1000);
    }

    public void testWithMonitor() throws Throwable {
        int runnerCount = 30;
        TestRunnable tcs[] = new TestRunnable[runnerCount];
        for (int i = 0; i < runnerCount; ++i) {
            tcs[i] = new Worker();
        }
        TestRunnable monitors[] = { new Monitor() };
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tcs,
                monitors);
        mttr.runTestRunnables(10 * 60 * 1000);

        // verify
        for (int i = 0; i < runnerCount; ++i) {
            assertEquals(1, 1);
        }
    }

}

