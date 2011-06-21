/*
 *   Copyright 2006-2011 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.params.utests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import ome.parameters.Filter;
import ome.parameters.Options;
import ome.parameters.Parameters;

import org.testng.annotations.Test;

public class ParamsTest extends TestCase {

    @Test
    public void test_ParamsWithFilter() throws Exception {
        Parameters p = new Parameters(new Filter().unique());
        assertTrue(p.isUnique());
    }

    @Test
    public void test_ParamsWithCopy() throws Exception {
        Parameters p = new Parameters();
        p.addBoolean("TEST", Boolean.TRUE);
        Parameters p2 = new Parameters(p);
        assertTrue(p2.keySet().contains("TEST"));
    }

    @Test
    public void test_toString() throws Exception {
        String display = "";
        Parameters p = new Parameters();
        display = p.toString();
        p.addBoolean("T", Boolean.TRUE);
        display = p.toString();
        p.setFilter(new Filter());
        display = p.toString();
        p.setFilter(new Filter().group(1).page(0, 1));
        display = p.toString();
        Options o = new Options();
        o.acquisitionData = false;
        p.setOptions(o);
        display = p.toString();
        o.orphan = true;
        o.leaves = true;
        display = p.toString();
        p.addLong("id", 1l);
        display = p.toString();
        Map<String, Object> mym = new HashMap<String, Object>();
        mym.put("a", 1);
        p.addMap("mym", mym);
        display = p.toString();
        p.addList("list", Arrays.asList(1L, 2L));
        display = p.toString();
    }
}
