/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

import static omero.rtypes.*;

import java.util.Arrays;

import junit.framework.TestCase;
import omero.RList;
import omero.sys.ParametersI;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ParametersTest extends TestCase {

    ParametersI p;
    
    @BeforeTest
    public void setup() {
        p = new ParametersI();
    }
    
    //
    // Parameters.theFilter
    //
    
    @Test
    public void testFilter() throws Exception {
        p.noPage();
        assertNull(p.theFilter);
        p.page(2,3);
        assertEquals(rint(2), p.theFilter.offset);
        assertEquals(rint(3), p.theFilter.limit);
        p.noPage();
        assertNull(p.theFilter);
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
        p.addLong("long",1L);
        assertEquals(rlong(1), p.map.get("long"));
    }
    
    @Test
    public void testAddLongRType() throws Exception {
        p.addLong("long",rlong(1L));
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
