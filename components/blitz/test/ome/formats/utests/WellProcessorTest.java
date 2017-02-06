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

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import ome.xml.model.primitives.NonNegativeInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.Plate;
import omero.model.Well;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WellProcessorTest
{
	private OMEROMetadataStoreClient store;

	private static final int PLATE_INDEX = 1;

	private static final int WELL_INDEX = 1;

	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
			new BlitzInstanceProvider(store.getEnumerationProvider()));
        store.setWellColumn(new NonNegativeInteger(0), PLATE_INDEX, WELL_INDEX);
        store.setWellColumn(new NonNegativeInteger(1), PLATE_INDEX, WELL_INDEX + 1);
        store.setWellColumn(new NonNegativeInteger(0), PLATE_INDEX + 1, WELL_INDEX);
        store.setPlateName("setUp Plate", PLATE_INDEX + 1);
	}

	@Test
	public void testWellExists()
	{
		Assert.assertEquals(store.countCachedContainers(Well.class, null), 3);
		Assert.assertEquals(store.countCachedContainers(Plate.class, null), 1);
		LSID wellLSID1 = new LSID(Well.class, PLATE_INDEX, WELL_INDEX);
		LSID wellLSID2 = new LSID(Well.class, PLATE_INDEX, WELL_INDEX + 1);
		LSID wellLSID3 = new LSID(Well.class, PLATE_INDEX + 1, WELL_INDEX);
		LSID plateLSID1 = new LSID(Plate.class, PLATE_INDEX + 1);
		Well well1 = (Well) store.getSourceObject(wellLSID1);
		Well well2 = (Well) store.getSourceObject(wellLSID2);
		Well well3 = (Well) store.getSourceObject(wellLSID3);
		Plate plate1 = (Plate) store.getSourceObject(plateLSID1);
		Assert.assertNotNull(well1);
		Assert.assertNotNull(well2);
		Assert.assertNotNull(well3);
		Assert.assertNotNull(plate1);
		Assert.assertEquals(well1.getColumn().getValue(), 0);
		Assert.assertEquals(well2.getColumn().getValue(), 1);
		Assert.assertEquals(well3.getColumn().getValue(), 0);
		Assert.assertEquals(plate1.getName().getValue(), "setUp Plate");
	}

	@Test
	public void testWellPostProcess()
	{
		store.postProcess();
		Assert.assertEquals(store.countCachedContainers(Well.class, null), 3);
		Assert.assertEquals(store.countCachedContainers(Plate.class, null), 2);
		LSID plateLSID1 = new LSID(Plate.class, PLATE_INDEX);
		LSID plateLSID2 = new LSID(Plate.class, PLATE_INDEX + 1);
		Plate plate1 = (Plate) store.getSourceObject(plateLSID1);
		Plate plate2 = (Plate) store.getSourceObject(plateLSID2);
		Assert.assertNotNull(plate1);
		Assert.assertNotNull(plate2);
		Assert.assertEquals(plate1.getName().getValue(), "Plate");
		Assert.assertEquals(plate2.getName().getValue(), "setUp Plate");
	}
}
