/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.utests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import ome.services.util.ServiceHandler;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.Test;

public class ServiceHandlerUnitTest extends MockObjectTestCase {

    @Test
    public void testStringOutput() throws Exception {
        
        String str;
        
        // Plain
        
        str = ServiceHandler.getResultsString(null, null);
        assertEquals("null", str);
        
        str = ServiceHandler.getResultsString(1L, null);
        assertEquals("1", str);

        // Arrays
        
        str = ServiceHandler.getResultsString(new Object[]{null}, null);
        assertEquals("[null]", str);
        
        str = ServiceHandler.getResultsString(new Long[]{1L}, null);
        assertEquals("[1]", str);

        str = ServiceHandler.getResultsString(new Long[]{1L,1L,1L,1L}, null);
        assertEquals("[1, 1, 1, ... 1 more]", str);
        
        // Lists

        str = ServiceHandler.getResultsString(Arrays.asList(1L), null);
        assertEquals("(1)", str);
 
        str = ServiceHandler.getResultsString(Arrays.asList(new Object[]{null}), null);
        assertEquals("(null)", str);
 
        // Sets
        
        str = ServiceHandler.getResultsString(new HashSet<Long>(Arrays.asList(1L)), null);
        assertEquals("(1)", str);

        // Maps
        
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("a","b");
        str = ServiceHandler.getResultsString(map, null);
        assertEquals("{a=b}", str);

        // Arrays of arrays
        Object[][] objs = new Object[3][];
        objs[0] = new Object[]{1,2,3};
        objs[1] = new Object[]{4,5,6};
        objs[2] = new Object[]{7,8,9};
        str = ServiceHandler.getResultsString(objs, null);
        assertEquals("[[1, 2, 3], [4, 5, 6], [7, 8, 9]]", str);
    }

    @Test
    public void testLongStringOutput() throws Exception {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("0123456789");
        }

        String rv = ServiceHandler.getResultsString(sb.toString(), null);
        assertEquals(sb.toString().substring(0, 100), rv);
        rv = ServiceHandler.getResultsString(Arrays.asList(sb.toString()), null);
        assertEquals("(" + sb.toString().substring(0, 100) + ")", rv);
    }
}
