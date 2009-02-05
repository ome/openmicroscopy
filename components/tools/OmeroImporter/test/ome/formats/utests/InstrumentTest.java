package ome.formats.utests;

import ome.formats.LSID;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import omero.model.Instrument;
import omero.model.LightSource;
import omero.model.Image;
import omero.model.Pixels;
import omero.api.ServiceFactoryPrx;
import junit.framework.TestCase;

public class InstrumentTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final String LIGHTSOURCE_MODEL = "Model";
	
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
	    for (int i = 0; i < 3; i++)
	    {
	        LSID lsid = new LSID(Pixels.class, i, PIXELS_INDEX);
	        assertNotNull(store.getSourceObject(lsid));
	    }
	    LSID lsid = new LSID(LightSource.class, 
	                           INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
	    assertNotNull(store.getSourceObject(lsid));
	    assertNotNull(store.getSourceObject(new LSID("Instrument:0")));
	}
	
	public void testImageInstrumentLightSourceModelPreserved()
	{
        LightSource ls = store.getLightSource(INSTRUMENT_INDEX,
                                              LIGHTSOURCE_INDEX);
        assertEquals(LIGHTSOURCE_MODEL, ls.getModel().getValue());
	}
	
	public void testContainerCount()
	{
	    assertEquals(1, store.countCachedContainers(LightSource.class));
	    assertEquals(1, store.countCachedContainers(Instrument.class));
	    assertEquals(3, store.countCachedContainers(Pixels.class));
	    assertEquals(5, store.countCachedContainers(null));
	}

    public void testReferences()
    {
        for (int i = 0; i < 3; i++)
        {
            LSID imageLsid = new LSID(Image.class, i);
            assertTrue(store.hasReference(imageLsid, new LSID("Instrument:0")));
        }
        assertEquals(3, store.countCachedReferences(null, null));
    }
}
