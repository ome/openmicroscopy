/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import junit.framework.TestCase;
import ome.services.blitz.fire.Ring;

import org.jgroups.blocks.ReplicatedTree;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class RingTest extends TestCase {

    // Using tree directly since the ring hides it
    ReplicatedTree tree1, tree2;
    
    @BeforeClass
    public void setup() throws Exception {
        tree1 = new ReplicatedTree("test", "session_ring.xml", 1000);
        tree2= new ReplicatedTree("test", "session_ring.xml", 1000);
        tree1.start();
        tree2.start();
    }
    
    @AfterClass
    public void tearDown() {
        tree1.stop();
        tree2.stop();
    }
    
    //@Test
    public void testMain() throws Exception {
        Ring.main(new String[]{});
    }
    
    @Test
    public void testLotsOfCalls() throws Exception {
        int i = 0;
        int j = 0;
        long start = System.currentTimeMillis();
        for (int k = 0; k < 100; k++) {
            i++; j++;
            tree1.put("/1", i+"", i+"");
            tree2.put("/2", j+"", j+"");
        }


        assertEquals(100, tree2.getKeys("/1").size());
        assertEquals(100, tree1.getKeys("/2").size());
    
    }
    
    @Test
    public void testPrintSessions() throws Exception {
        Ring ring = new Ring("omero", "session_ring.xml");
        ring.printSessions();
    }
    

}