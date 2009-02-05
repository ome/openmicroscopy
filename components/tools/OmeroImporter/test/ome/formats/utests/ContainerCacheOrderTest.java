package ome.formats.utests;

import java.util.Map;

import ome.formats.LSID;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import omero.metadatastore.IObjectContainer;
import omero.api.ServiceFactoryPrx;
import junit.framework.TestCase;

public class ContainerCacheOrderTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final int LOGICAL_CHANNEL_INDEX = 0;
	
	private static final int OBJECTIVE_INDEX = 0;
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory();
        wrapper = new OMEROWrapper();
        store = new OMEROMetadataStoreClient(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper.setMetadataStore(store);

        // Populate at least one image field.
        store.setImageName("Foo", IMAGE_INDEX);
        
        // Populate at least one pixels field.
        store.setPixelsSizeX(1, IMAGE_INDEX, PIXELS_INDEX);
        store.setPixelsSizeX(1, IMAGE_INDEX, PIXELS_INDEX + 1);
        
        // Populate at least one logical channel field.
        store.setLogicalChannelEmWave(100, IMAGE_INDEX, PIXELS_INDEX);
        
        // Populate at least one instrument field.
        store.setInstrumentID("Instrument:0", INSTRUMENT_INDEX);
        
        // First Laser, First LightSourceSettings
		store.setLightSourceModel(
				"Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceID(
				"Laser:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourcePower(1.0f, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLaserFrequencyMultiplication(
				1, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceSettingsLightSource(
				"Laser:0", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		store.setLightSourceSettingsAttenuation(
				1.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		
		// Second Laser, Second LightSourceSettings
		store.setLightSourceModel(
				"Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLightSourceID(
				"Laser:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLightSourcePower(1.0f, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLaserFrequencyMultiplication(
				1, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLightSourceSettingsLightSource(
				"Laser:1", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
		store.setLightSourceSettingsAttenuation(
				1.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
				
		// Third Laser, Third LightSourceSettings (different orientation)
		store.setLaserLaserMedium(
				"Ar", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 2);
		store.setLaserType(
				"Gas", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 2);
		store.setLightSourceID(
				"Laser:2", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 2);
		store.setLightSourceSettingsLightSource(
				"Laser:2", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 2);
		store.setLightSourceSettingsAttenuation(
				1.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 2);
		
		// First Objective, First ObjectiveSettings
		store.setObjectiveLensNA(1.0f, INSTRUMENT_INDEX, OBJECTIVE_INDEX);
		store.setObjectiveID("Objective:0", INSTRUMENT_INDEX, OBJECTIVE_INDEX);
		store.setObjectiveSettingsObjective("Objective:0", IMAGE_INDEX);
		
		// Second Objective, Second ObjectiveSettings
		store.setObjectiveLensNA(1.0f, INSTRUMENT_INDEX, OBJECTIVE_INDEX + 1);
		store.setObjectiveID("Objective:1", INSTRUMENT_INDEX, OBJECTIVE_INDEX + 1);
		store.setObjectiveSettingsObjective("Objective:1", IMAGE_INDEX + 1);
	}
	
	public void testOrder()
	{
		Map<LSID, IObjectContainer> containerCache = 
			store.getContainerCache();
		for (LSID key : containerCache.keySet())
		{
			System.err.println(key + " == " + containerCache.get(key).sourceObject);
		}
	}
}
