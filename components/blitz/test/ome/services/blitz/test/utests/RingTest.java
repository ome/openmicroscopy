/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import junit.framework.TestCase;
import ome.services.blitz.fire.Ring;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class RingTest extends TestCase {

    Ring ring1, ring2;
    
    @BeforeClass
    public void setup() throws Exception {
        ring1 = new Ring();
        ring2 = new Ring();
    }
    
    @Test
    public void testSimpleAdd() throws Exception {
        ring1.put("a","b");
        Thread.sleep(1000L);
        assertTrue(ring1.containsKey("a")); // Even this needs the wait.
        assertTrue(ring2.containsKey("a"));
        assertEquals("b", ring2.get("a"));
    }

}