/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test;

import junit.framework.TestCase;
import omero.RLong;
import omero.RTime;
import omero.sys.PojoOptions;

import org.testng.annotations.Test;

public class PojoOptionsTest extends TestCase {

    @Test
    public void testBasics() throws Exception {
        PojoOptions po = new PojoOptions();
        po.exp(new RLong(1));
        po.grp(new RLong(1));
        po.endTime(new RTime(1));
    }
    
    @Test
    public void testDefaults() throws Exception {
        PojoOptions po = new PojoOptions();
        assertFalse(po.isLeaves());
        assertFalse(po.isGroup());
        assertFalse(po.isExperimenter());
        assertFalse(po.isEndTime());
        assertFalse(po.isStartTime());
        assertFalse(po.isPagination());
    }
    
    
    @Test
    public void testExperimenter() throws Exception {
        PojoOptions po = new PojoOptions();
        po.exp(new RLong(1));
        assertTrue(po.isExperimenter());
        assertEquals(po.getExperimenter().val, 1L);
        po.allExps();
        assertFalse(po.isExperimenter());
    }

    @Test
    public void testGroup() throws Exception {
        PojoOptions po = new PojoOptions();
        po.grp(new RLong(1));
        assertTrue(po.isGroup());
        assertEquals(po.getGroup().val, 1L);
        po.allGrps();
        assertFalse(po.isGroup());
    }

}
