/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.util.utests;

import java.util.HashMap;

import junit.framework.TestCase;
import ome.util.builders.PojoOptions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class PojoOptionsTest extends TestCase {

    private static Log log = LogFactory.getLog(PojoOptionsTest.class);

    PojoOptions ob;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        ob = new PojoOptions();
    }

    @Test
    public void testDefaults() {
        log.info(ob.map());
    }

    @Test
    public void testAllMethods() {
        log.info(ob.exp(new Long(3)).allExps().noLeaves().map());
    }

    @Test
    public void testNullMap() {
        ob = new PojoOptions(null);
        PojoOptions test = new PojoOptions();
        assertTrue("Should have default keys", ob.map().keySet().containsAll(
                test.map().keySet()));

        ob = new PojoOptions(new HashMap());
        assertTrue("Should be empty; this is the only way.", ob.map().keySet()
                .size() == 0);
    }
}
