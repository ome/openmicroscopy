/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import static omero.rtypes.rtime;

import java.sql.Timestamp;

import ome.api.IContainer;
import ome.api.IMetadata;
import ome.util.builders.PojoOptions;
import omero.sys.ParametersI;
import omero.util.IceMapper;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests whether or not values are being properly passed in as expected
 * by {@link IContainer} and {@link IMetadata}
 */
@Test(groups = "ticket:67")
public class ParametersIToPojoOptionsTest extends MockObjectTestCase {

    ParametersI p;
    IceMapper m = new IceMapper();

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        p = new ParametersI();
        m = new IceMapper();
    }

    public void testLeaves() throws Exception {
        p.leaves();
        assertTrue(po().isLeaves());
        p.noLeaves();
        assertFalse(po().isLeaves());
    }
    
    public void testOrphan() throws Exception {
        p.orphan();
        assertTrue(po().isOrphan());
        p.noOrphan();
        assertFalse(po().isOrphan());
    }
    
    public void testAcquisitionData() throws Exception {
        p.acquisitionData();
        assertTrue(po().isOrphan());
        p.noAcquisitionData();
        assertFalse(po().isOrphan());
    }
    
    public void testTimes() throws Exception {
        p.startTime(rtime(0));
        assertTrue(po().isStartTime());
        assertEquals(new Timestamp(0L), po().getStartTime());
        assertFalse(po().isEndTime());
        assertNull(po().getEndTime());
        p.endTime(rtime(1));
        assertTrue(po().isStartTime());
        assertEquals(new Timestamp(0L), po().getStartTime());
        assertTrue(po().isEndTime());
        assertEquals(new Timestamp(1L), po().getEndTime());
        p.startTime(null);
        assertFalse(po().isStartTime());
        assertNull(po().getStartTime());
        assertTrue(po().isEndTime());
        assertEquals(new Timestamp(1L), po().getEndTime());
        p.endTime(null);
        assertFalse(po().isStartTime());
        assertNull(po().getStartTime());
        assertFalse(po().isEndTime());
        assertNull(po().getEndTime());
    }

    // ============
    
    private PojoOptions po() throws Exception {
        return new PojoOptions(m.convert(p));
    }
    
}
