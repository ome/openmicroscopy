/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.utests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import junit.framework.TestCase;
import ome.util.tasks.Run;

import org.testng.annotations.Test;

/**
 */
public class StdinTest extends TestCase {

    @Test
    public void testNoStdinShouldReturn() throws Exception {
        final boolean[] finished = new boolean[1];
        Thread t = new Thread() {
            @Override
            public void run() {
                Run.main(new String[]{"task=DebugTask"});
                finished[0] = true;
            }
        };
        t.start();
        Thread.sleep(2500);
        if (!finished[0]) {
            fail("Didn't return.");
        }
        
    }

    @Test
    public void testStdinShouldBeRead() throws Exception {
        InputStream old_in = System.in;
        String s = "task=DebugTask"; // If not found, will throw an exception.
        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
        System.setIn(bais);
        Run.main(new String[]{});
        
    }
}
