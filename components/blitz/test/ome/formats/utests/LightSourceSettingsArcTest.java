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
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import ome.xml.model.enums.ArcType;
import ome.xml.model.primitives.PercentFraction;
import ome.xml.model.primitives.PositiveInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.Arc;
import omero.model.LightSettings;
import omero.model.Pixels;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LightSourceSettingsArcTest
{
	private OMEROWrapper wrapper;

	private OMEROMetadataStoreClient store;

	private static final int LIGHTSOURCE_INDEX = 0;

	private static final int INSTRUMENT_INDEX = 0;

	private static final int IMAGE_INDEX = 0;

	private static final int CHANNEL_INDEX = 0;

	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        wrapper = new OMEROWrapper(new ImportConfig());
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
			new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper.setMetadataStore(store);

        // Need to populate at least one pixels field.
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX);

        // First Arc, First LightSourceSettings
		store.setArcModel("Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setArcID("Arc:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setArcType(ArcType.OTHER, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setChannelLightSourceSettingsID(
				"Arc:0", IMAGE_INDEX, CHANNEL_INDEX);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, CHANNEL_INDEX);

		// Second Arc, Second LightSourceSettings
		store.setArcModel("Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setArcID("Arc:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setArcType(ArcType.OTHER, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setChannelLightSourceSettingsID(
				"Arc:1", IMAGE_INDEX, CHANNEL_INDEX + 1);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, CHANNEL_INDEX + 1);
	}

	@Test
	public void testLightSourceCount()
	{
        LSID lsid = new LSID(Pixels.class, IMAGE_INDEX);
        Assert.assertNotNull(store.getSourceObject(lsid));
        Assert.assertEquals(store.countCachedContainers(Arc.class), 2);
        Assert.assertEquals(store.countCachedContainers(null), 5);
	}

	@Test
	public void testLightSourceSettingsCount()
	{
	    Assert.assertEquals(store.countCachedContainers(LightSettings.class), 2);
	    Assert.assertEquals(store.countCachedContainers(null), 5);
	}

	@Test
	public void testReferences()
	{
        for (int i = 0; i < 2; i++)
        {
            LSID imageLsid = new LSID(LightSettings.class, IMAGE_INDEX, i);
            Assert.assertTrue(store.hasReference(imageLsid, new LSID("Arc:" + i)));
        }
        Assert.assertEquals(store.countCachedReferences(null, null), 2);
	}
}
