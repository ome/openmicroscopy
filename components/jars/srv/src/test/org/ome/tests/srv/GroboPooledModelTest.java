/*
 * Created on Feb 26, 2005
 */
package org.ome.tests.srv;

import org.apache.commons.pool.KeyedObjectPool;
import org.ome.cache.Cache;
import org.ome.interfaces.ImageService;
import org.ome.model.IImage;
import org.ome.model.LSID;
import org.ome.model.Vocabulary;

import net.sourceforge.groboutils.junit.v1.MultiThreadedTestRunner;
import net.sourceforge.groboutils.junit.v1.TestMonitorRunnable;
import net.sourceforge.groboutils.junit.v1.TestRunnable;
import junit.framework.TestCase;

/**
 * @author josh
 */
public class GroboPooledModelTest extends TestCase {

    static Cache cache = (Cache) SpringTestHarness.ctx.getBean("cache");
    static KeyedObjectPool pool = (KeyedObjectPool) SpringTestHarness.ctx.getBean("modelPool");
    static ImageService is=(ImageService)SpringTestHarness.ctx.getBean("imageService");
    static String lsidBase = Vocabulary.NS+"img";
    
    static class Worker extends TestRunnable {
        int count;
        int sleepTime;

        public Worker() {
            count = 3;
            sleepTime = 200;
        }

        public void runTest() throws Throwable {
            for (int i = 0; i < this.count; ++i) {
                Thread.sleep(this.sleepTime);
                LSID lsid = new LSID(lsidBase+i);
                System.out.println("Getting: "+lsid);
                IImage img = is.retrieveImage(lsid);
                System.out.println("Image: " +img);
            }
        }
    }

    public static class Monitor extends TestMonitorRunnable {
        
        public Monitor() {
        }

        public void runMonitor() throws Throwable {
            Thread.sleep(2000);
            String output = 
            "\n*****************************"+
            "\nActive: "+pool.getNumActive()+
            "\nIdle: "+pool.getNumIdle()+
            "\n*****************************";        
        	System.out.println(output);
        }
    }

    public void testWithMonitor() throws Throwable {
        int runnerCount = 2;
        TestRunnable tcs[] = new TestRunnable[runnerCount];
        for (int i = 0; i < runnerCount; ++i) {
            tcs[i] = new Worker();
        }
        TestRunnable monitors[] = { new Monitor() };
        MultiThreadedTestRunner mttr = new MultiThreadedTestRunner(tcs,
                monitors);
        mttr.runTestRunnables(1 * 60 * 1000);
    }

}

