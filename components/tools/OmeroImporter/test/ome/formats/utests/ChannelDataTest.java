package ome.formats.utests;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.ChannelData;
import omero.api.ServiceFactoryPrx;
import omero.model.Filament;

public class ChannelDataTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int FILTER_SET_INDEX = 0;
	
	private static final int FILTER_INDEX = 0;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final int LOGICAL_CHANNEL_INDEX = 0;
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory();
        wrapper = new OMEROWrapper(new ImportConfig());
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper.setMetadataStore(store);
        
        // Need to populate at least one pixels field.
        store.setPixelsSizeX(1, IMAGE_INDEX, PIXELS_INDEX);
        
        // First Filament, First LightSourceSettings
		store.setLightSourceID(
				"Filament:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceManufacturer("0", INSTRUMENT_INDEX,
				LIGHTSOURCE_INDEX);
		store.setFilamentType("Unknown", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceSettingsLightSource(
				"Filament:0", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		store.setLightSourceSettingsAttenuation(
				1.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		
		// Second Filament, Second LightSourceSettings
		store.setLightSourceID(
				"Filament:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLightSourceManufacturer("1", INSTRUMENT_INDEX,
				LIGHTSOURCE_INDEX + 1);
		store.setFilamentType("Unknown", INSTRUMENT_INDEX,
				LIGHTSOURCE_INDEX + 1);
		store.setLightSourceSettingsLightSource(
				"Filament:1", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
		store.setLightSourceSettingsAttenuation(
				2.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
		
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
		store.setLogicalChannelFilterSet("FilterSet:0", IMAGE_INDEX,
				LOGICAL_CHANNEL_INDEX);
		store.setLogicalChannelFilterSet("FilterSet:1", IMAGE_INDEX,
				LOGICAL_CHANNEL_INDEX + 1);
		
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
		store.setFilterSetEmFilter("Filter:0", INSTRUMENT_INDEX,
				FILTER_SET_INDEX);
		store.setFilterSetExFilter("Filter:1", INSTRUMENT_INDEX,
				FILTER_SET_INDEX);
		store.setFilterSetEmFilter("Filter:6", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1);
		store.setFilterSetExFilter("Filter:7", INSTRUMENT_INDEX,
				FILTER_SET_INDEX + 1);
		store.setLogicalChannelSecondaryEmissionFilter(
				"Filter:2", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		store.setLogicalChannelSecondaryExcitationFilter(
				"Filter:3", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		store.setLogicalChannelSecondaryEmissionFilter(
				"Filter:4", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
		store.setLogicalChannelSecondaryExcitationFilter(
				"Filter:5", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
	}
	
	public void testChannelDataChannelOne()
	{
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
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
		assertNotNull(data.getSecondaryEmissionFilter());
		assertEquals("2", 
				data.getSecondaryEmissionFilter().getLotNumber().getValue());
		assertNotNull(data.getSecondaryExcitationFilter());
		assertEquals("3", 
				data.getSecondaryExcitationFilter().getLotNumber().getValue());
		assertNotNull(data.getLightSource());
		assertTrue(data.getLightSource() instanceof Filament);
		assertEquals("0", 
				data.getLightSource().getManufacturer().getValue());
		assertNotNull(data.getLightSourceSettings());
		assertEquals(1.0, 
				data.getLightSourceSettings().getAttenuation().getValue());
	}
	
	public void testChannelDataChannelTwo()
	{
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
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
		assertNotNull(data.getSecondaryEmissionFilter());
		assertEquals("4", 
				data.getSecondaryEmissionFilter().getLotNumber().getValue());
		assertNotNull(data.getSecondaryExcitationFilter());
		assertEquals("5", 
				data.getSecondaryExcitationFilter().getLotNumber().getValue());
		assertNotNull(data.getLightSource());
		assertTrue(data.getLightSource() instanceof Filament);
		assertEquals("1", 
				data.getLightSource().getManufacturer().getValue());
		assertNotNull(data.getLightSourceSettings());
		assertEquals(2.0, 
				data.getLightSourceSettings().getAttenuation().getValue());
	}
	
}
