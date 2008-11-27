package ome.formats.utests;

import java.util.List;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.OMEROWrapper;
import ome.formats.testclient.TestServiceFactory;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.LightSource;
import ome.model.core.Image;
import ome.system.ServiceFactory;
import junit.framework.TestCase;

public class InstrumentTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStore store;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final String LIGHTSOURCE_MODEL = "Model";
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactory sf = new TestServiceFactory();
        wrapper = new OMEROWrapper();
        store = new OMEROMetadataStore(sf);
        wrapper.setMetadataStore(store);
        
        // Need to populate at least one pixels field of each Image.
        store.setPixelsSizeX(1, IMAGE_INDEX, PIXELS_INDEX);
        store.setPixelsSizeX(1, IMAGE_INDEX + 1, PIXELS_INDEX);
        store.setPixelsSizeX(1, IMAGE_INDEX + 2, PIXELS_INDEX);
        
        // Add some metadata to the LightSource to ensure that it is not lost.
        store.setLightSourceModel(
        		LIGHTSOURCE_MODEL, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
        
        // Set the LSID on our Instrument and link to all three images.
        store.setInstrumentID("Instrument:0", INSTRUMENT_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 1);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 2);
	}

	public void testImageInstrumentExists()
	{
        List<Image> images = (List<Image>) store.getRoot();
        for (Image image : images)
        {
        	assertNotNull(image.getSetup());
        }
	}
	
	public void testImageInstrumentLightSourceExists()
	{
        List<Image> images = (List<Image>) store.getRoot();
        for (Image image : images)
        {
        	assertEquals(1, image.getSetup().sizeOfLightSource());
        }
	}
	
	public void testImageInstrumentLightSourceModelPreserved()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Instrument instrument = images.get(0).getSetup();
        LightSource lightSource = instrument.iterateLightSource().next();
        assertEquals(LIGHTSOURCE_MODEL, lightSource.getModel());
	}

	public void testReferences()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Instrument instrument = images.get(0).getSetup();
        for (Image image : images)
        {
        	assertEquals(image.getSetup(), instrument);
        }
	}
}
