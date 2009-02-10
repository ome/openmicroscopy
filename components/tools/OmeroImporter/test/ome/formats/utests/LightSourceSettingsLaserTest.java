package ome.formats.utests;

import ome.formats.LSID;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.MetaLightSource;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import omero.model.Laser;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.Pixels;
import omero.api.ServiceFactoryPrx;
import junit.framework.TestCase;

public class LightSourceSettingsLaserTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final int LOGICAL_CHANNEL_INDEX = 0;
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory();
        wrapper = new OMEROWrapper();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper.setMetadataStore(store);
        
        // Need to populate at least one pixels field.
        store.setPixelsSizeX(1, IMAGE_INDEX, PIXELS_INDEX);
        
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
	}
	
	public void testLightSourceSettingsLightSourceNotMLS()
	{
        for (int i = 0; i < 3; i++)
        {
            LSID lsid = new LSID(LightSource.class, INSTRUMENT_INDEX, i);
            LightSource ls = (LightSource) store.getSourceObject(lsid);
            if (ls instanceof MetaLightSource)
            {
                fail("Light source " + ls + " is meta.");
            }
        }
	}
	
	public void testLightSourceCount()
	{
        LSID lsid = new LSID(Pixels.class, IMAGE_INDEX, PIXELS_INDEX);
        assertNotNull(store.getSourceObject(lsid));
        assertEquals(3, store.countCachedContainers(Laser.class));
        assertEquals(7, store.countCachedContainers(null));
	}
	
	public void testLightSourceSettingsCount()
	{
        LSID lsid = new LSID(Pixels.class, IMAGE_INDEX, PIXELS_INDEX);
        assertNotNull(store.getSourceObject(lsid));
        assertEquals(3, store.countCachedContainers(Laser.class));
        assertEquals(7, store.countCachedContainers(null));
	}

	public void testReferences()
	{
        for (int i = 0; i < 3; i++)
        {
            LSID imageLsid = new LSID(LightSettings.class, IMAGE_INDEX, i);
            assertTrue(store.hasReference(imageLsid, new LSID("Laser:" + i)));
        }
        assertEquals(3, store.countCachedReferences(null, null));
	}
}
