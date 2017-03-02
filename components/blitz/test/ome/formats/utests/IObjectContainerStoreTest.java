/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.utests;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ome.util.LSID;
import ome.formats.Index;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.xml.model.primitives.PositiveInteger;
import omero.model.Image;
import omero.model.ObjectiveSettings;
import omero.model.Pixels;
import omero.api.ServiceFactoryPrx;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IObjectContainerStoreTest
{
	private OMEROMetadataStoreClient store;

	private static final int IMAGE_INDEX = 0;

	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setReader(new TestReader());
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
			new BlitzInstanceProvider(store.getEnumerationProvider()));

        // Two objects of the same type
        store.setImageName("Foo1", IMAGE_INDEX);
        store.setImageName("Foo2", IMAGE_INDEX + 1);

        // Objects of a different type
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX);
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX + 1);

        // Add a reference
        store.setObjectiveSettingsID("Objective:0", IMAGE_INDEX);
	}

	@Test
	public void testGetCaches()
	{
		Assert.assertNotNull(store.getContainerCache());
		Assert.assertNotNull(store.getReferenceCache());
		Assert.assertNull(store.getReferenceStringCache());
	}

	@Test
	public void testSetReferenceStringCache()
	{
		Map<String, String[]> a = new HashMap<String, String[]>();
		store.setReferenceStringCache(a);
		Assert.assertEquals(a, store.getReferenceStringCache());
	}

	@Test
	public void testGetSourceObject()
	{
	    Assert.assertNotNull(store.getSourceObject(new LSID(Image.class, 0)));
	}

	@Test
	public void testGetSourceObjects()
	{
	    Assert.assertEquals(store.getSourceObjects(Image.class).size(), 2);
	}

	@Test
	public void testGetIObjectContainer()
	{
		LinkedHashMap<Index, Integer> indexes =
			new LinkedHashMap<Index, Integer>();
		indexes.put(Index.IMAGE_INDEX, IMAGE_INDEX + 2);
		store.getIObjectContainer(Image.class, indexes);
		Assert.assertEquals(store.countCachedContainers(Image.class), 3);
	}

	@Test
	public void testCachedContainers()
	{
	    Assert.assertEquals(2, store.countCachedContainers(Image.class), 2);
	    Assert.assertEquals(store.countCachedContainers(Pixels.class), 2);
	    Assert.assertEquals(store.countCachedContainers(
				Pixels.class, IMAGE_INDEX), 1);
	    Assert.assertEquals(store.countCachedContainers(
				Pixels.class, IMAGE_INDEX + 1), 1);
	}

	@Test
	public void testHasReference()
	{
	    Assert.assertTrue(store.hasReference(new LSID(ObjectiveSettings.class,
				                      IMAGE_INDEX), new LSID("Objective:0")));
	}

	@Test
	public void testCount10000CachedContainers()
	{
		for (int i = 0; i < 10000; i++)
		{
			store.setImageName(String.valueOf(i), i);
		}
		long t0 = System.currentTimeMillis();
		store.countCachedContainers(Image.class, null);
		Assert.assertTrue((System.currentTimeMillis() - t0) < 100);
	}

	@Test
	public void testGet10000ContainersByClass()
	{
		for (int i = 0; i < 10000; i++)
		{
			store.setImageName(String.valueOf(i), i);
		}
		long t0 = System.currentTimeMillis();
		store.getIObjectContainers(Image.class);
		Assert.assertTrue((System.currentTimeMillis() - t0) < 100);
	}
}
