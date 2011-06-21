/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.util.utests;

import ome.model.core.Image;
import ome.util.LSID;
import junit.framework.TestCase;

public class LSIDParseTest extends TestCase
{
    public void testParseJavaClass()
    {
    	LSID a = new LSID("ome.model.core.Image:0");
    	assertEquals(a.parseJavaClass(), Image.class);
    }

    public void testParseNoIndex()
    {
    	LSID a = new LSID("ome.model.core.Image");
    	assertEquals(0, a.parseIndexes().length);
    }
    
    public void testParseSingleIndex()
    {
    	LSID a = new LSID("ome.model.core.Image:0");
    	int[] indexes = a.parseIndexes();
    	assertEquals(1, indexes.length);
    	assertEquals(0, indexes[0]);
    }

    public void testParseDoubleIndex()
    {
    	LSID a = new LSID("ome.model.core.Image:0:0");
    	int[] indexes = a.parseIndexes();
    	assertEquals(2, indexes.length);
    	assertEquals(0, indexes[0]);
    	assertEquals(0, indexes[1]);
    }
    
    public void testParseBigSingleIndex()
    {
    	LSID a = new LSID("ome.model.core.Image:667667");
    	int[] indexes = a.parseIndexes();
    	assertEquals(1, indexes.length);
    	assertEquals(667667, indexes[0]);
    }
    
    public void testParseBigDoubleIndex()
    {
    	LSID a = new LSID("ome.model.core.Image:667667:766766");
    	int[] indexes = a.parseIndexes();
    	assertEquals(2, indexes.length);
    	assertEquals(667667, indexes[0]);
    	assertEquals(766766, indexes[1]);
    }
}
