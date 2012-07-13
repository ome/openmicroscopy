/*
 * ome.formats.utests.ChannelDataTest
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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

import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.ChannelData;
import ome.xml.model.enums.*;
import ome.xml.model.primitives.*;
import omero.api.ServiceFactoryPrx;
import omero.model.Filament;
import omero.model.Filter;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the creation of channel objects.
 * 
 * @author Chris Allan <callan at blackcat dot ca>
 */
public class ChannelDataTest extends TestCase
{
	
	/** Reference to the wrapper. */
	private OMEROWrapper wrapper;
	
	/** Reference to the store. */
	private OMEROMetadataStoreClient store;
	
	/** Identifies the index of the filter set. */
	private static final int FILTER_SET_INDEX = 0;
	
	/** Identifies the index of the filter. */
	private static final int FILTER_INDEX = 0;
	
	/** Identifies the index of the light source. */
	private static final int LIGHTSOURCE_INDEX = 0;
	
	/** Identifies the index of the instrument. */
	private static final int INSTRUMENT_INDEX = 0;
	
	/** Identifies the index of the image. */
	private static final int IMAGE_INDEX = 0;

	/** Identifies the index of the channel. */
	private static final int CHANNEL_INDEX = 0;

	/** Identifies the index of the emission filter. */
    private static final int EM_FILTER_INDEX = 0;

    /** Identifies the index of the excitation filter. */
    private static final int EX_FILTER_INDEX = 0;

    /**
     * Initializes the components and populates the store.
     */
	@BeforeMethod
	protected void setUp() 
		throws Exception
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
        
        // First Filament, First LightSourceSettings
		store.setFilamentID(
				"Filament:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setFilamentManufacturer("0", INSTRUMENT_INDEX,
				LIGHTSOURCE_INDEX);
		store.setFilamentType(FilamentType.OTHER, INSTRUMENT_INDEX, 
				LIGHTSOURCE_INDEX);
		store.setChannelLightSourceSettingsID(
				"Filament:0", IMAGE_INDEX, CHANNEL_INDEX);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1.0f), IMAGE_INDEX, CHANNEL_INDEX);
		
		// Second Filament, Second LightSourceSettings
		store.setFilamentID(
				"Filament:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setFilamentManufacturer("1", INSTRUMENT_INDEX,
				LIGHTSOURCE_INDEX + 1);
		store.setFilamentType(FilamentType.OTHER, INSTRUMENT_INDEX,
				LIGHTSOURCE_INDEX + 1);
		store.setChannelLightSourceSettingsID(
				"Filament:1", IMAGE_INDEX, CHANNEL_INDEX + 1);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1.0f), IMAGE_INDEX, CHANNEL_INDEX + 1);
		
		// FilterSet
		store.setFilterSetID("FilterSet:0", INSTRUMENT_INDEX,
				FILTER_SET_INDEX);
		store.setFilterSetLotNumber("0", INSTRUMENT_INDEX,
				FILTER_SET_INDEX);
		store.setFilterSetID("FilterSet:1", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1);
		store.setFilterSetLotNumber("1", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1);
		
		// FilterSet linkages
		store.setChannelFilterSetRef("FilterSet:0", IMAGE_INDEX,
				CHANNEL_INDEX);
		store.setChannelFilterSetRef("FilterSet:1", IMAGE_INDEX,
				CHANNEL_INDEX + 1);
		
		// Filters
		store.setFilterID("Filter:0", INSTRUMENT_INDEX, FILTER_INDEX);
		store.setFilterLotNumber("0", INSTRUMENT_INDEX, FILTER_INDEX);
		store.setFilterID("Filter:1", INSTRUMENT_INDEX, FILTER_INDEX + 1);
		store.setFilterLotNumber("1", INSTRUMENT_INDEX, FILTER_INDEX + 1);
		store.setFilterID("Filter:2", INSTRUMENT_INDEX, FILTER_INDEX + 2);
		store.setFilterLotNumber("2", INSTRUMENT_INDEX, FILTER_INDEX + 2);
		store.setFilterID("Filter:3", INSTRUMENT_INDEX, FILTER_INDEX + 3);
		store.setFilterLotNumber("3", INSTRUMENT_INDEX, FILTER_INDEX + 3);
		store.setFilterID("Filter:4", INSTRUMENT_INDEX, FILTER_INDEX + 4);
		store.setFilterLotNumber("4", INSTRUMENT_INDEX, FILTER_INDEX + 4);
		store.setFilterID("Filter:5", INSTRUMENT_INDEX, FILTER_INDEX + 5);
		store.setFilterLotNumber("5", INSTRUMENT_INDEX, FILTER_INDEX + 5);
		store.setFilterID("Filter:6", INSTRUMENT_INDEX, FILTER_INDEX + 6);
		store.setFilterLotNumber("6", INSTRUMENT_INDEX, FILTER_INDEX + 6);
		store.setFilterID("Filter:7", INSTRUMENT_INDEX, FILTER_INDEX + 7);
		store.setFilterLotNumber("7", INSTRUMENT_INDEX, FILTER_INDEX + 7);
		
		// Filter linkages
		store.setFilterSetEmissionFilterRef("Filter:0", INSTRUMENT_INDEX,
				FILTER_SET_INDEX, FILTER_INDEX);
		store.setFilterSetExcitationFilterRef("Filter:1", INSTRUMENT_INDEX,
				FILTER_SET_INDEX, FILTER_INDEX);
		store.setFilterSetEmissionFilterRef("Filter:6", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1, FILTER_INDEX);
		store.setFilterSetExcitationFilterRef("Filter:7", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1, FILTER_INDEX);
		store.setLightPathEmissionFilterRef(
				"Filter:2", IMAGE_INDEX, CHANNEL_INDEX, EM_FILTER_INDEX);
		store.setLightPathExcitationFilterRef(
				"Filter:3", IMAGE_INDEX, CHANNEL_INDEX, EX_FILTER_INDEX);
		store.setLightPathEmissionFilterRef("Filter:4",
        IMAGE_INDEX, CHANNEL_INDEX + 1, EM_FILTER_INDEX + 1);
		store.setLightPathExcitationFilterRef("Filter:5",
        IMAGE_INDEX, CHANNEL_INDEX + 1, EX_FILTER_INDEX + 1);
	}

	/** Tests the creation of the first channel. */
	@Test
	public void testChannelDataChannelOne()
	{
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX);
		assertNotNull(data);
		assertNotNull(data.getChannel());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getFilterSet());
		assertEquals("0", data.getFilterSet().getLotNumber().getValue());
		assertNotNull(data.getFilterSetEmissionFilter());
		assertEquals("0", 
				data.getFilterSetEmissionFilter().getLotNumber().getValue());
		assertNotNull(data.getFilterSetExcitationFilter());
		assertEquals("1", 
				data.getFilterSetExcitationFilter().getLotNumber().getValue());
		List<Filter> filters = data.getLightPathEmissionFilters();
		assertNotNull(filters);
		Iterator<Filter> i = filters.iterator();
		assertTrue(filters.size() == 1);
		Filter f;
		while (i.hasNext()) {
			f = i.next();
			assertEquals("2", f.getLotNumber().getValue());
		}
		filters = data.getLightPathExcitationFilters();
		assertNotNull(filters);
		i = filters.iterator();
		
		assertTrue(filters.size() == 1);

		while (i.hasNext()) {
			f = i.next();
			assertEquals("3", f.getLotNumber().getValue());
		}
		assertNotNull(data.getLightSource());
		assertTrue(data.getLightSource() instanceof Filament);
		assertEquals("0", 
				data.getLightSource().getManufacturer().getValue());
		assertNotNull(data.getLightSourceSettings());
		assertEquals(1.0, 
				data.getLightSourceSettings().getAttenuation().getValue());
	}
	
	/** Tests the creation of the second channel. */
	@Test
	public void testChannelDataChannelTwo()
	{
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, CHANNEL_INDEX + 1);
		assertNotNull(data);
		assertNotNull(data.getChannel());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getFilterSet());
		assertEquals("1", data.getFilterSet().getLotNumber().getValue());
		assertNotNull(data.getFilterSetEmissionFilter());
		assertEquals("6", 
				data.getFilterSetEmissionFilter().getLotNumber().getValue());
		assertNotNull(data.getFilterSetExcitationFilter());
		assertEquals("7", 
				data.getFilterSetExcitationFilter().getLotNumber().getValue());
		List<Filter> filters = data.getLightPathEmissionFilters();
		assertNotNull(filters);
		Iterator<Filter> i = filters.iterator();
		
		assertTrue(filters.size() == 1);
		
		Filter f;
		while (i.hasNext()) {
			f = i.next();
			assertEquals("4", f.getLotNumber().getValue());
		}
		filters = data.getLightPathExcitationFilters();
		assertNotNull(filters);
		i = filters.iterator();
		
		assertTrue(filters.size() == 1);

		while (i.hasNext()) {
			f = i.next();
			assertEquals("5", f.getLotNumber().getValue());
		}
		
		assertNotNull(data.getLightSource());
		assertTrue(data.getLightSource() instanceof Filament);
		assertEquals("1", 
				data.getLightSource().getManufacturer().getValue());
		assertNotNull(data.getLightSourceSettings());
		assertEquals(1.0, 
				data.getLightSourceSettings().getAttenuation().getValue());
	}
	
}
