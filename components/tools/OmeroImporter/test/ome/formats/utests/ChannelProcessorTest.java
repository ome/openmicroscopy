package ome.formats.utests;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.ChannelData;
import ome.formats.model.ChannelProcessor;
import omero.api.ServiceFactoryPrx;

public class ChannelProcessorTest extends TestCase
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
        store.setReader(new TestReader());
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper.setMetadataStore(store);
        
        // Need to populate at least one pixels and image field.
        store.setImageName("Image", IMAGE_INDEX);
        store.setPixelsSizeX(1, IMAGE_INDEX, PIXELS_INDEX);
        store.setPixelsSizeC(2, IMAGE_INDEX, PIXELS_INDEX);
        
        // First Filament, First LightSourceSettings
		store.setLightSourceID(
				"Laser:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceManufacturer("0", INSTRUMENT_INDEX,
				LIGHTSOURCE_INDEX);
		store.setLaserType("Unknown", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceSettingsLightSource(
				"Laser:0", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
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

	public void testBaseDataChannelOne()
	{
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(255, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(0, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNull(data.getLogicalChannel().getName());
	}
	
	
	
	public void testBaseDataChannelTwo()
	{
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(255, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNull(data.getLogicalChannel().getName());
	}
	
	public void testGraphicsDomain()
	{
		ChannelProcessor processor = new ChannelProcessor();
		store.setReader(new TestReader(true));
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(255, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(0, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals(ChannelProcessor.RED_TEXT, 
				data.getLogicalChannel().getName().getValue());
	}
	
	public void testLogicalChannelGreenEmissionWavelength()
	{
		store.setLogicalChannelEmWave(525, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(255, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(0, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("525", data.getLogicalChannel().getName().getValue());
	}
	
	public void testLogicalChannelBlueEmissionWavelength()
	{
		store.setLogicalChannelEmWave(450, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(255, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("450", data.getLogicalChannel().getName().getValue());
	}
	
	public void testLogicalChannelRedEmissionWavelength()
	{
		store.setLogicalChannelEmWave(625, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(255, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(0, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("625", data.getLogicalChannel().getName().getValue());
	}
	
	public void testFilterSetEmFilterBlueWavelength()
	{
		store.setTransmittanceRangeCutIn(425, INSTRUMENT_INDEX, 0);
		store.setTransmittanceRangeCutOut(430, INSTRUMENT_INDEX, 0);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(255, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("425", data.getLogicalChannel().getName().getValue());
	}
	
	public void testLogicalChannelSecondaryEmFilterBlueWavelength()
	{
		store.setTransmittanceRangeCutIn(430, INSTRUMENT_INDEX, 2);
		store.setTransmittanceRangeCutOut(435, INSTRUMENT_INDEX, 2);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(255, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("430", data.getLogicalChannel().getName().getValue());
	}
	
	public void testLaserBlueWavelength()
	{
		store.setLaserWavelength(435, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(255, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("435", data.getLogicalChannel().getName().getValue());
	}
	
	public void testLogicalChannelGreenExcitationWavelength()
	{
		store.setLogicalChannelExWave(525, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(255, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(0, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("525", data.getLogicalChannel().getName().getValue());
	}
	
	public void testLogicalChannelBlueExcitationWavelength()
	{
		store.setLogicalChannelExWave(450, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(255, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("450", data.getLogicalChannel().getName().getValue());
	}
	
	public void testLogicalChannelRedExcitationWavelength()
	{
		store.setLogicalChannelExWave(625, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(255, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(0, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("625", data.getLogicalChannel().getName().getValue());
	}
	
	public void testFilterSetExFilterBlueWavelength()
	{
		store.setTransmittanceRangeCutIn(425, INSTRUMENT_INDEX, 1);
		store.setTransmittanceRangeCutOut(430, INSTRUMENT_INDEX, 1);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(255, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("425", data.getLogicalChannel().getName().getValue());
	}
	
	public void testLogicalChannelSecondaryExFilterBlueWavelength()
	{
		store.setTransmittanceRangeCutIn(430, INSTRUMENT_INDEX, 3);
		store.setTransmittanceRangeCutOut(435, INSTRUMENT_INDEX, 3);
		ChannelProcessor processor = new ChannelProcessor();
		processor.process(store);
		ChannelData data = ChannelData.fromObjectContainerStore(
				store, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		assertNotNull(data.getChannel());
		assertNotNull(data.getChannel().getRed());
		assertEquals(0, data.getChannel().getRed().getValue());
		assertNotNull(data.getChannel().getGreen());
		assertEquals(0, data.getChannel().getGreen().getValue());
		assertNotNull(data.getChannel().getBlue());
		assertEquals(255, data.getChannel().getBlue().getValue());
		assertNotNull(data.getChannel().getAlpha());
		assertEquals(255, data.getChannel().getAlpha().getValue());
		assertNotNull(data.getLogicalChannel());
		assertNotNull(data.getLogicalChannel().getName());
		assertEquals("430", data.getLogicalChannel().getName().getValue());
	}
}
