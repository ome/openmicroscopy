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
import omero.api.ServiceFactoryPrx;
import omero.model.Rectangle;
import omero.model.Roi;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ShapeProcessorTest
{
	private OMEROMetadataStoreClient store;

	private static final int ROI_INDEX = 1;

	private static final int SHAPE_INDEX = 1;

	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
			new BlitzInstanceProvider(store.getEnumerationProvider()));
        store.setROIDescription("Foobar", ROI_INDEX);
        store.setRectangleX(25.0, ROI_INDEX + 1, SHAPE_INDEX);
	}

	@Test
	public void testShapeExists()
	{
		Assert.assertEquals(store.countCachedContainers(Roi.class, null), 1);
		Assert.assertEquals(store.countCachedContainers(Rectangle.class, null), 1);
		LSID roiLSID1 = new LSID(Roi.class, ROI_INDEX);
		LSID shapeLSID1 = new LSID(Rectangle.class, ROI_INDEX + 1, SHAPE_INDEX);
		Roi roi = (Roi) store.getSourceObject(roiLSID1);
		Rectangle shape = (Rectangle) store.getSourceObject(shapeLSID1);
		Assert.assertNotNull(roi);
		Assert.assertNotNull(shape);
		Assert.assertEquals(roi.getDescription().getValue(), "Foobar");
		Assert.assertEquals(shape.getX().getValue(), 25.0);
	}

	@Test
	public void testShapePostProcess()
	{
		store.postProcess();
		Assert.assertEquals(2, store.countCachedContainers(Roi.class, null));
		Assert.assertEquals(1, store.countCachedContainers(Rectangle.class, null));
		LSID roiLSID1 = new LSID(Roi.class, ROI_INDEX);
		LSID roiLSID2 = new LSID(Roi.class, ROI_INDEX + 1);
		Roi roi1 = (Roi) store.getSourceObject(roiLSID1);
		Roi roi2 = (Roi) store.getSourceObject(roiLSID2);
		Assert.assertNotNull(roi1);
		Assert.assertNotNull(roi2);
		Assert.assertEquals(roi1.getDescription().getValue(), "Foobar");
		Assert.assertNull(roi2.getDescription());
	}
}
