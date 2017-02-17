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
import ome.xml.model.primitives.PositiveInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Pixels;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DetectorSettingsTest
{
	private OMEROWrapper wrapper;

	private OMEROMetadataStoreClient store;

	private static final int DETECTOR_INDEX = 0;

	private static final int INSTRUMENT_INDEX = 0;

	private static final int IMAGE_INDEX = 0;

	private static final int CHANNEL_INDEX = 0;

	private static final String DETECTOR_MODEL = "Model";

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

        // Need to populate at least one pixels field of each Image.
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX);
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX + 1);
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX + 2);

        // Add some metadata to the Detector to ensure that it is not lost.
        store.setDetectorModel(
			DETECTOR_MODEL, INSTRUMENT_INDEX, DETECTOR_INDEX);

        // Set the LSID on our Objective and link to all three images. Also
        // link the Instrument to all three images.
        store.setDetectorID("Detector:0", INSTRUMENT_INDEX, DETECTOR_INDEX);
        store.setInstrumentID("Instrument:0", INSTRUMENT_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 1);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 2);
        store.setDetectorSettingsID(
			"Detector:0", IMAGE_INDEX, CHANNEL_INDEX);
        store.setDetectorSettingsID(
			"Detector:0", IMAGE_INDEX + 1, CHANNEL_INDEX);
        store.setDetectorSettingsID(
			"Detector:0", IMAGE_INDEX + 2, CHANNEL_INDEX);
	}

	@Test
	public void testImageDetectorExists()
	{
	    for (int i = 0; i < 3; i++)
	    {
	        LSID lsid = new LSID(Pixels.class, i);
	        Assert.assertNotNull(store.getSourceObject(lsid));
	    }
	    Assert.assertNotNull(store.getSourceObject(new LSID(Instrument.class, 0)));
	    Assert.assertNotNull(store.getSourceObject(new LSID(Detector.class, 0, 0)));
	}

	@Test
	public void testDetectorModelPreserved()
	{
	    Detector detector = store.getDetector(INSTRUMENT_INDEX,
	                                          DETECTOR_INDEX);
	    Assert.assertEquals(DETECTOR_MODEL, detector.getModel().getValue());
	}

	@Test
	public void testContainerCount()
	{
	    Assert.assertEquals(store.countCachedContainers(Detector.class), 1);
	    Assert.assertEquals(store.countCachedContainers(Instrument.class), 1);
	    Assert.assertEquals(store.countCachedContainers(Pixels.class), 3);
	    Assert.assertEquals(store.countCachedContainers(null), 5);
	}

	@Test
	public void testReferences()
	{
	    for (int i = 0; i < 3; i++)
	    {
	        LSID imageLsid = new LSID(Image.class, i);
	        LSID dsLsid = new LSID(DetectorSettings.class, i,
	                               CHANNEL_INDEX);
	        Assert.assertTrue(store.hasReference(dsLsid, new LSID("Detector:0")));
	        Assert.assertTrue(store.hasReference(imageLsid, new LSID("Instrument:0")));
	    }
	    Assert.assertEquals(store.countCachedReferences(null, null), 6);
	}
}
