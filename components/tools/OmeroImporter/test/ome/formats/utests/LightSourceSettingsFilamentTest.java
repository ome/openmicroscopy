package ome.formats.utests;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.MetaLightSource;
import ome.util.LSID;
import ome.xml.r201004.enums.*;
import ome.xml.r201004.primitives.*;
import omero.api.ServiceFactoryPrx;
import omero.model.Filament;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.Pixels;

public class LightSourceSettingsFilamentTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
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
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX);
        
        // First Filament, First LightSourceSettings
		store.setFilamentModel("Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setFilamentID("Filament:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setFilamentType(
        FilamentType.OTHER, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setChannelLightSourceSettingsID(
				"Filament:0", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		
		// Second Filament, Second LightSourceSettings
		store.setFilamentModel("Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setFilamentID("Filament:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setFilamentType(
        FilamentType.OTHER, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setChannelLightSourceSettingsID(
				"Filament:1", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
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
        LSID lsid = new LSID(Pixels.class, IMAGE_INDEX);
        assertNotNull(store.getSourceObject(lsid));
        assertEquals(2, store.countCachedContainers(Filament.class));
        assertEquals(5, store.countCachedContainers(null));
	}
	
	public void testLightSourceSettingsCount()
	{
        LSID lsid = new LSID(Pixels.class, IMAGE_INDEX);
        assertNotNull(store.getSourceObject(lsid));
        assertEquals(2, store.countCachedContainers(Filament.class));
        assertEquals(5, store.countCachedContainers(null));
	}
	
	public void testReferences()
	{
        for (int i = 0; i < 2; i++)
        {
            LSID imageLsid = new LSID(LightSettings.class, IMAGE_INDEX, i);
            assertTrue(store.hasReference(imageLsid, new LSID("Filament:" + i)));
        }
        assertEquals(2, store.countCachedReferences(null, null));
	}
}
