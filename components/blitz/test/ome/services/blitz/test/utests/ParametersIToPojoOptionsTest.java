/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import static omero.rtypes.rtime;
import static omero.rtypes.rlong;
import static omero.rtypes.rint;

import java.sql.Timestamp;

import ome.api.IContainer;
import ome.api.IMetadata;
import ome.parameters.Parameters;
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

    public void testOptions() throws Exception {
        assertFalse(p().isLeaves());
        assertFalse(p().isOrphan());
        assertFalse(p().isAcquisitionData());
        p.leaves();
        assertTrue(p().isLeaves());
        assertFalse(p().isOrphan());
        assertFalse(p().isAcquisitionData());
        p.orphan();
        assertTrue(p().isLeaves());
        assertTrue(p().isOrphan());
        assertFalse(p().isAcquisitionData());
        p.noOrphan();
        assertTrue(p().isLeaves());
        assertFalse(p().isOrphan());
        assertFalse(p().isAcquisitionData());
        p.acquisitionData();
        assertTrue(p().isAcquisitionData());
        p.noAcquisitionData();
        assertFalse(p().isOrphan());
        p.noLeaves();
        assertFalse(p().isLeaves());
        assertFalse(p().isLeaves());
        assertFalse(p().isOrphan());
        assertFalse(p().isAcquisitionData());
    }
    
    public void testTimes() throws Exception {
        p.startTime(rtime(0));
        assertEquals(new Timestamp(0L), p().getStartTime());
        assertNull(p().getEndTime());
        p.endTime(rtime(1));
        assertEquals(new Timestamp(0L), p().getStartTime());
        assertEquals(new Timestamp(1L), p().getEndTime());
        p.startTime(null);
        assertNull(p().getStartTime());
        assertEquals(new Timestamp(1L), p().getEndTime());
        p.endTime(null);
        assertNull(p().getStartTime());
        assertNull(p().getEndTime());
    }
    
    public void testUserGroup() throws Exception {
        assertFalse(p().isExperimenter());
        assertFalse(p().isGroup());
        assertEquals(-1, p().owner());
        assertEquals(-1, p().group());
        assertNull(p().getExperimenter());
        assertNull(p().getGroup());
        p.exp(rlong(1L));
        assertTrue(p().isExperimenter());
        assertFalse(p().isGroup());
        assertEquals(1L, p().owner());
        assertEquals(-1, p().group());
        assertNotNull(p().getExperimenter());
        assertNull(p().getGroup());
        p.grp(rlong(1L));
        assertTrue(p().isExperimenter());
        assertTrue(p().isGroup());
        assertEquals(1L, p().owner());
        assertEquals(1L, p().group());
        assertNotNull(p().getExperimenter());
        assertNotNull(p().getGroup());
        p.allExps();
        assertFalse(p().isExperimenter());
        assertTrue(p().isGroup());
        assertEquals(-1L, p().owner());
        assertEquals(1L, p().group());
        assertNull(p().getExperimenter());
        assertNotNull(p().getGroup());
        p.allGrps();
        assertFalse(p().isExperimenter());
        assertFalse(p().isGroup());
        assertEquals(-1L, p().owner());
        assertEquals(-1L, p().group());
        assertNull(p().getExperimenter());
        assertNull(p().getGroup());
    }
    
    public void testPaginationAndUniqueness() throws Exception {
        assertFalse(p().isPagination());
        assertFalse(p().isUnique());
        assertNull(p().getOffset());
        assertNull(p().getLimit());
        p.page(0, 1);
        assertTrue(p().isPagination());
        assertFalse(p().isUnique());
        assertEquals(new Integer(0), p().getOffset());
        assertEquals(new Integer(1), p().getLimit());
        assertFalse(p().isUnique());
        p.noPage();
        assertFalse(p().isPagination());
        assertFalse(p().isUnique());
        assertNull(p().getOffset());
        assertNull(p().getLimit());
        assertFalse(p().isUnique());
        p.page(rint(0), rint(1));
        assertTrue(p().isPagination());
        assertFalse(p().isUnique());
        assertEquals(new Integer(0), p().getOffset());
        assertEquals(new Integer(1), p().getLimit());
        assertFalse(p().isUnique());
        p.unique();
        assertTrue(p().isUnique());
        assertTrue(p().isPagination());
        assertEquals(new Integer(0), p().getOffset());
        assertEquals(new Integer(1), p().getLimit());
        p.noUnique();
        assertFalse(p().isUnique());
        assertTrue(p().isPagination());
        assertFalse(p().isUnique());
        assertEquals(new Integer(0), p().getOffset());
        assertEquals(new Integer(1), p().getLimit());
        p.noPage();
        
    }

    // ============
    
    private Parameters p() throws Exception {
        return m.convert(p);
    }
    
}
