/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.util.utests;

import java.util.Properties;

import junit.framework.TestCase;
import ome.util.tasks.Run;

import org.testng.annotations.Test;

/**
 */
public class DebugTaskTest extends TestCase {

    @Test
    public void testOutput() throws Exception {
        Run.main(new String[]{"task=DebugTask","a=b"});
    }
    // ~ Helpers
    // =========================================================================

    protected Properties pairs(String... values) {
        Properties p = new Properties();
        for (int i = 0; i < values.length; i += 2) {
            p.setProperty(values[i], values[i + 1]);
        }
        return p;
    }
}
