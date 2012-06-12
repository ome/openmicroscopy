package ome.formats.utests;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import ome.xml.model.enums.*;
import ome.xml.model.primitives.*;
import omero.api.ServiceFactoryPrx;
import omero.model.Arc;
import omero.model.LightSettings;
import omero.model.Pixels;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LightSourceSettingsArcTest extends TestCase
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
        assertNotNull(store.getSourceObject(lsid));
        assertEquals(2, store.countCachedContainers(Arc.class));
        assertEquals(5, store.countCachedContainers(null));
	}
	
	@Test
	public void testLightSourceSettingsCount()
	{
        assertEquals(2, store.countCachedContainers(LightSettings.class));
        assertEquals(5, store.countCachedContainers(null));
	}
	
	@Test
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
