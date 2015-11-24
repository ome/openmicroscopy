/*
 *   Copyright (C) 2009-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.util.utests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import ome.model.acquisition.DetectorSettings;
import ome.model.acquisition.FilterSet;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.screen.Plate;
import ome.util.LSID;

import org.testng.annotations.Test;

@Test
public class LSIDEquivilenceTest extends TestCase
{
	public void testStringHashMapContainsKey()
	{
		Map<LSID, List<LSID>> map = new HashMap<LSID, List<LSID>>();
		LSID lsid1 = new LSID("Image:0"); 
		map.put(lsid1, new ArrayList<LSID>());
		LSID lsid2 = new LSID("Image:1");
		map.put(lsid2, new ArrayList<LSID>());
		assertEquals(2, map.size());
		assertTrue(map.containsKey(lsid1));
		assertTrue(map.containsKey(lsid2));
	}
	
	public void testLSIDHashMapContainsKey()
	{
		Map<LSID, String> map = new HashMap<LSID, String>();
		LSID lsid1 = new LSID(Image.class, 0); 
		map.put(lsid1, "Test");
		LSID lsid2 = new LSID(Image.class, 1);
		map.put(lsid2, "Test");
		assertEquals(2, map.size());
		assertTrue(map.containsKey(lsid1));
		assertTrue(map.containsKey(lsid2));
	}

	public void testHashMapContainsMultipleKeys()
	{
		Map<LSID, List<LSID>> map = new HashMap<LSID, List<LSID>>();
		LSID lsid1 = new LSID(FilterSet.class, 0, 0);
		map.put(lsid1, new ArrayList<LSID>());
		LSID lsid2 = new LSID(FilterSet.class, 0, 1);
		map.put(lsid2, new ArrayList<LSID>());
		LSID lsid3 = new LSID(FilterSet.class, 0, 2);
		map.put(lsid3, new ArrayList<LSID>());
		LSID lsid4 = new LSID(FilterSet.class, 0, 3);
		map.put(lsid4, new ArrayList<LSID>());
		LSID lsid5 = new LSID(DetectorSettings.class, 0, 0);
		map.put(lsid5, new ArrayList<LSID>());
		LSID lsid6 = new LSID(LogicalChannel.class, 0, 0);
		map.put(lsid6, new ArrayList<LSID>());
		LSID lsid7 = new LSID(Image.class, 0);
		map.put(lsid7, new ArrayList<LSID>());
		LSID lsid8 = new LSID(ObjectiveSettings.class, 0);
		map.put(lsid8, new ArrayList<LSID>());
		
		assertEquals(8, map.size());
		assertEquals(8, map.entrySet().size());
		assertTrue(map.containsKey(lsid1));
		assertTrue(map.containsKey(lsid2));
		assertTrue(map.containsKey(lsid3));
		assertTrue(map.containsKey(lsid4));
		assertTrue(map.containsKey(lsid5));
		assertTrue(map.containsKey(lsid6));
		assertTrue(map.containsKey(lsid7));
		assertTrue(map.containsKey(lsid8));
	}
	
    public void testStringVsStringNumeric()
    {
        LSID a = new LSID("Image:0");
        LSID b = new LSID("Image:0");
        assertEquals(a, b);
    }
    
    public void testStringVsStringAlpha()
    {
        LSID a = new LSID("Image:ABC");
        LSID b = new LSID("Image:ABC");
        assertEquals(a, b);
    }
    
    public void testStringVsStringMixed()
    {
        LSID a = new LSID("Image:ABC-1");
        LSID b = new LSID("Image:ABC-1");
        assertEquals(a, b);
    }
    
    public void testLSIDVsString()
    {
        LSID a = new LSID("ome.model.core.Image:0");
        LSID b = new LSID(Image.class, 0);
        assertEquals(a, b);
    }
    
    public void testStringVsLSID()
    {
        LSID a = new LSID(Image.class, 0);
        LSID b = new LSID("ome.model.core.Image:0");
        assertEquals(a, b);
    }
    
    public void testPlateStringVsLSID()
    {
        LSID a = new LSID(Plate.class, 0);
        LSID b = new LSID("ome.model.screen.Plate:0");
        assertEquals(a, b);
    }
    
    public void testLSIDVsStringWithConstructor()
    {
        LSID a = new LSID("ome.model.core.Image:0", true);
        LSID b = new LSID(Image.class, 0);
        assertEquals(a, b);
        assertEquals(a.getJavaClass(), b.getJavaClass());
        assertEquals(a.getIndexes()[0], b.getIndexes()[0]);
    }
    
    public void testStringVsLSIDWithConstructor()
    {
        LSID a = new LSID(Image.class, 0);
        LSID b = new LSID("ome.model.core.Image:0", true);
        assertEquals(a, b);
        assertEquals(a.getJavaClass(), b.getJavaClass());
        assertEquals(a.getIndexes()[0], b.getIndexes()[0]);
    }
    
    public void testPlateStringVsLSIDWithConstructor()
    {
        LSID a = new LSID(Plate.class, 0);
        LSID b = new LSID("ome.model.screen.Plate:0", true);
        assertEquals(a, b);
        assertEquals(a.getJavaClass(), b.getJavaClass());
        assertEquals(a.getIndexes()[0], b.getIndexes()[0]);
    }

    public void testBigListsWithSet()
    {
        Set<LSID> set = new HashSet<LSID>();
        for (int i = 0; i < 100000; i++)
        {
            set.add(new LSID(Plate.class, i));
        }
    }

    public void testBigListsWithSetAndList()
    {
        Set<LSID> found = new HashSet<LSID>();
        List<LSID> list = new ArrayList<LSID>();
        for (int i = 0; i < 100000; i++)
        {
            LSID lsid = new LSID(Plate.class, i);
            if (!found.contains(lsid)) {
                found.add(lsid);
                list.add(lsid);
            }
        }
    }

    public void testBigListsWithLinkedHashSet()
    {
        LinkedHashSet<LSID> set = new LinkedHashSet<LSID>();
        for (int i = 0; i < 100000; i++)
        {
            LSID lsid = new LSID(Plate.class, i);
            if (!set.contains(lsid)) {
                set.add(lsid);
            }
        }
    }
}
