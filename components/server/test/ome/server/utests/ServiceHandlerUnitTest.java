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

import ome.security.basic.CurrentDetails;
import ome.services.util.ServiceHandler;

import org.jmock.MockObjectTestCase;
import org.testng.annotations.Test;

public class ServiceHandlerUnitTest extends MockObjectTestCase {

    @Test
    public void testStringOutput() throws Exception {
        
        String str;
        
        ServiceHandler sh = new ServiceHandler(new CurrentDetails());

        // Plain
        
        str = sh.getResultsString(null, null);
        assertEquals("null", str);
        
        str = sh.getResultsString(1L, null);
        assertEquals("1", str);

        // Arrays
        
        str = sh.getResultsString(new Object[]{null}, null);
        assertEquals("[null]", str);
        
        str = sh.getResultsString(new Long[]{1L}, null);
        assertEquals("[1]", str);

        str = sh.getResultsString(new Long[]{1L,1L,1L,1L}, null);
        assertEquals("[1, 1, 1, ... 1 more]", str);
        
        // Lists

        str = sh.getResultsString(Arrays.asList(1L), null);
        assertEquals("(1)", str);
 
        str = sh.getResultsString(Arrays.asList(new Object[]{null}), null);
        assertEquals("(null)", str);
 
        // Sets
        
        str = sh.getResultsString(new HashSet(Arrays.asList(1L)), null);
        assertEquals("(1)", str);

        // Maps
        
        HashMap map = new HashMap();
        map.put("a","b");
        str = sh.getResultsString(map, null);
        assertEquals("{a=b}", str);

        // Arrays of arrays
        Object[][] objs = new Object[3][];
        objs[0] = new Object[]{1,2,3};
        objs[1] = new Object[]{4,5,6};
        objs[2] = new Object[]{7,8,9};
        str = sh.getResultsString(objs, null);
        assertEquals("[[1, 2, 3], [4, 5, 6], [7, 8, 9]]", str);
    }
}
