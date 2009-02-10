package ome.formats.utests;

import ome.formats.LSID;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.MetaLightSource;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import omero.model.Arc;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.Pixels;
import omero.api.ServiceFactoryPrx;
import junit.framework.TestCase;

public class LightSourceSettingsArcTest extends TestCase
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
        
        // First Arc, First LightSourceSettings
		store.setLightSourceModel(
				"Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceID(
				"Arc:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setArcType("Unknown", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceSettingsLightSource(
				"Arc:0", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		store.setLightSourceSettingsAttenuation(
				1.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		
		// Second Arc, Second LightSourceSettings
		store.setLightSourceModel(
				"Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLightSourceID(
				"Arc:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setArcType("Unknown", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLightSourceSettingsLightSource(
				"Arc:1", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
		store.setLightSourceSettingsAttenuation(
				1.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
	}
	
	public void testLightSourceSettingsLightSourceNotMLS()
	{
        for (int i = 0; i < 2; i++)
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
        assertEquals(2, store.countCachedContainers(Arc.class));
        assertEquals(5, store.countCachedContainers(null));
	}
	
	public void testLightSourceSettingsCount()
	{
        assertEquals(2, store.countCachedContainers(LightSettings.class));
        assertEquals(5, store.countCachedContainers(null));
	}
	
	public void testReferences()
	{
        for (int i = 0; i < 2; i++)
        {
            LSID imageLsid = new LSID(LightSettings.class, IMAGE_INDEX, i);
            assertTrue(store.hasReference(imageLsid, new LSID("Arc:" + i)));
        }
        assertEquals(2, store.countCachedReferences(null, null));
	}
}
