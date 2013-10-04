/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import static omero.rtypes.rbool;
import static omero.rtypes.rint;
import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.util.Arrays;

import junit.framework.TestCase;
import omero.RList;
import omero.sys.ParametersI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ParametersTest extends TestCase {

    ParametersI p;

    @BeforeMethod
    public void setup() {
        p = new ParametersI();
    }

    //
    // Copied from PojoOptionsTest
    //

    @Test
    public void testBasics() throws Exception {
        p.exp(rlong(1));
        p.grp(rlong(1));
        p.endTime(rtime(1));
    }

    @Test
    public void testDefaults() throws Exception {
        // Removed to prevent confusion. assertFalse(p.isLeaves());
        assertFalse(p.isGroup());
        assertFalse(p.isExperimenter());
        assertFalse(p.isEndTime());
        assertFalse(p.isStartTime());
        assertFalse(p.isPagination());
    }

    @Test
    public void testExperimenter() throws Exception {
        p.exp(rlong(1));
        assertTrue(p.isExperimenter());
        assertEquals(p.getExperimenter().getValue(), 1L);
        p.allExps();
        assertFalse(p.isExperimenter());
    }

    @Test
    public void testGroup() throws Exception {
        p.grp(rlong(1));
        assertTrue(p.isGroup());
        assertEquals(p.getGroup().getValue(), 1L);
        p.allGrps();
        assertFalse(p.isGroup());
    }

    //
    // Parameters.theFilter.limit, offset
    //

    @Test
    public void testFilter() throws Exception {
        p.noPage();
        assertNull(p.theFilter);
        p.page(2, 3);
        assertEquals(rint(2), p.theFilter.offset);
        assertEquals(rint(3), p.theFilter.limit);
        p.noPage();
        assertNull(p.theFilter.offset);
        assertNull(p.getOffset());
        assertNull(p.theFilter.limit);
        assertNull(p.getLimit());
    }

    //
    // Parameters.theFilter.ownerId, groupId
    //

    @Test
    public void testOwnerId() throws Exception {
        assertNull(p.theFilter);
        p.exp(rlong(1));
        assertNotNull(p.theFilter);
        assertNotNull(p.theFilter.ownerId);
        assertEquals(rlong(1), p.getExperimenter());
        assertNull(p.allExps().getExperimenter());
        assertNotNull(p.theFilter);
    }

    @Test
    public void testGroupId() throws Exception {
        assertNull(p.theFilter);
        p.grp(rlong(1));
        assertNotNull(p.theFilter);
        assertNotNull(p.theFilter.groupId);
        assertEquals(rlong(1), p.getGroup());
        assertNull(p.allGrps().getGroup());
        assertNotNull(p.theFilter);
    }

    //
    // Parameters.theFilter.startTime, endTime
    //

    @Test
    public void testTimes() throws Exception {
        assertNull(p.theFilter);
        p.startTime(rtime(0));
        assertNotNull(p.theFilter);
        assertNotNull(p.theFilter.startTime);
        p.endTime(rtime(1));
        assertNotNull(p.theFilter.endTime);
        p.allTimes();
        assertNotNull(p.theFilter);
        assertNull(p.theFilter.startTime);
        assertNull(p.theFilter.endTime);
    }

    //
    // Parameters.theOptions
    //

    @Test
    public void testOptionsAcquisitionData() throws Exception {
        assertNull(p.getAcquisitionData());
        assertEquals(rbool(true), p.acquisitionData().getAcquisitionData());
        assertEquals(rbool(false), p.noAcquisitionData().getAcquisitionData());
        assertNotNull(p.getAcquisitionData());
    }

    @Test
    public void testOptionsOrphan() throws Exception {
        assertNull(p.getOrphan());
        assertEquals(rbool(true), p.orphan().getOrphan());
        assertEquals(rbool(false), p.noOrphan().getOrphan());
        assertNotNull(p.getOrphan());
    }

    @Test
    public void testOptionsUnique() throws Exception {
        assertNull(p.getLeaves());
        assertEquals(rbool(true), p.leaves().getLeaves());
        assertEquals(rbool(false), p.noLeaves().getLeaves());
        assertNotNull(p.getLeaves());
    }

    //
    // Parameters.map
    //

    @Test
    public void testAddBasicString() throws Exception {
        p.add("string", rstring("a"));
        assertEquals(rstring("a"), p.map.get("string"));
    }

    @Test
    public void testAddBasicInt() throws Exception {
        p.add("int", rint(1));
        assertEquals(rint(1), p.map.get("int"));
    }

    @Test
    public void testAddIdRaw() throws Exception {
        p.addId(1);
        assertEquals(rlong(1), p.map.get("id"));
    }

    @Test
    public void testAddIdRType() throws Exception {
        p.addId(rlong(1));
        assertEquals(rlong(1), p.map.get("id"));
    }

    @Test
    public void testAddLongRaw() throws Exception {
        p.addLong("long", 1L);
        assertEquals(rlong(1), p.map.get("long"));
    }

    @Test
    public void testAddLongRType() throws Exception {
        p.addLong("long", rlong(1L));
        assertEquals(rlong(1), p.map.get("long"));
    }

    @Test
    public void testAddIds() throws Exception {
        p.addIds(Arrays.asList(1L, 2L));
        RList list = (RList) p.map.get("ids");
        assertTrue(list.getValue().contains(rlong(1)));
        assertTrue(list.getValue().contains(rlong(2)));
    }

    @Test
    public void testAddLongs() throws Exception {
        p.addLongs("longs", Arrays.asList(1L, 2L));
        RList list = (RList) p.map.get("longs");
        assertTrue(list.getValue().contains(rlong(1)));
        assertTrue(list.getValue().contains(rlong(2)));
    }

}
