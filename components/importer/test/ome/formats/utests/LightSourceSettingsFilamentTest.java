package ome.formats.utests;

import java.util.Iterator;
import java.util.List;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.MetaLightSource;
import ome.formats.importer.OMEROWrapper;
import ome.formats.testclient.TestServiceFactory;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.system.ServiceFactory;
import junit.framework.TestCase;

public class LightSourceSettingsFilamentTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStore store;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final int LOGICAL_CHANNEL_INDEX = 0;
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactory sf = new TestServiceFactory();
        wrapper = new OMEROWrapper();
        store = new OMEROMetadataStore(sf);
        wrapper.setMetadataStore(store);
        
        // Need to populate at least one pixels field.
        store.setPixelsSizeX(1, IMAGE_INDEX, PIXELS_INDEX);
        
        // First Filament, First LightSourceSettings
		store.setLightSourceModel(
				"Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceID(
				"Filament:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setFilamentType("Unknown", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLightSourceSettingsLightSource(
				"Filament:0", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		store.setLightSourceSettingsAttenuation(
				1.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
		
		// Second Filament, Second LightSourceSettings
		store.setLightSourceModel(
				"Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLightSourceID(
				"Filament:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setFilamentType(
				"Unknown", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLightSourceSettingsLightSource(
				"Filament:1", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
		store.setLightSourceSettingsAttenuation(
				1.0f, IMAGE_INDEX, LOGICAL_CHANNEL_INDEX + 1);
	}
	
	public void testLightSourceSettingsLightSourceNotMLS()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Image image = images.get(0);
        Instrument instrument = image.getSetup();
        Iterator<Channel> iter = image.getPrimaryPixels().iterateChannels();
        while (iter.hasNext())
        {
        	Channel c = iter.next();
        	LogicalChannel lc = c.getLogicalChannel();
        	LightSettings ls = lc.getLightSourceSettings();
        	LightSource lightSource = ls.getLightSource();
        	if (lightSource instanceof MetaLightSource)
        	{
        		fail("Light source " + lightSource + " is meta.");
        	}
        }
	}
	
	public void testLightSourceCount()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Image image = images.get(0);
        Instrument instrument = image.getSetup();
        assertEquals(2, instrument.sizeOfLightSource());
	}
	
	public void testLightSourceSettingsCount()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Image image = images.get(0);
        Iterator<Channel> iter = image.getPrimaryPixels().iterateChannels();
        int i = 0;
        while (iter.hasNext())
        {
        	Channel c = iter.next();
        	LogicalChannel lc = c.getLogicalChannel();
        	LightSettings ls = lc.getLightSourceSettings();
        	assertNotNull(ls);
        	i++;
        }
        assertEquals(2, i);
	}
	
	public void testLightSourceSettingsLightSourceExists()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Image image = images.get(0);
        Iterator<Channel> iter = image.getPrimaryPixels().iterateChannels();
        while (iter.hasNext())
        {
        	Channel c = iter.next();
        	LogicalChannel lc = c.getLogicalChannel();
        	LightSettings ls = lc.getLightSourceSettings();
        	LightSource lightSource = ls.getLightSource();
        	assertNotNull(lightSource);
        }
	}

	public void testReferences()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Image image = images.get(0);
        Instrument instrument = image.getSetup();
        Iterator<Channel> iter = image.getPrimaryPixels().iterateChannels();
        while (iter.hasNext())
        {
        	Channel c = iter.next();
        	LogicalChannel lc = c.getLogicalChannel();
        	LightSettings ls = lc.getLightSourceSettings();
        	LightSource lightSource = ls.getLightSource();
        	Iterator<LightSource> instrumentIter = 
        		instrument.iterateLightSource();
        	boolean exists = false;
        	while (instrumentIter.hasNext())
        	{
        		LightSource instrumentLightSource = instrumentIter.next();
        		if (instrumentLightSource == lightSource)
        		{
        			exists = true;
        		}
        	}
        	assertTrue(exists);
        }
	}
}
