package ome.formats.utests;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import ome.xml.model.primitives.PositiveInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.Pixels;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class InstrumentTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final String LIGHTSOURCE_MODEL = "Model";
	
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
        
        // Need to populate at least one pixels field of each Image.
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX);
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX + 1);
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX + 2);
        
        // Add some metadata to the LightSource to ensure that it is not lost.
        store.setLaserModel(
        		LIGHTSOURCE_MODEL, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
        
        // Set the LSID on our Instrument and link to all three images.
        store.setInstrumentID("Instrument:0", INSTRUMENT_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 1);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 2);
	}

	@Test
	public void testImageInstrumentExists()
	{
	    for (int i = 0; i < 3; i++)
	    {
	        LSID lsid = new LSID(Pixels.class, i);
	        assertNotNull(store.getSourceObject(lsid));
	    }
	    LSID lsid = new LSID(Laser.class, 
	                           INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
	    assertNotNull(store.getSourceObject(lsid));
	    assertNotNull(store.getSourceObject(new LSID(Instrument.class, 0)));
	}
	
	@Test
	public void testImageInstrumentLightSourceModelPreserved()
	{
        Laser ls = store.getLaser(INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
        assertEquals(LIGHTSOURCE_MODEL, ls.getModel().getValue());
	}
	
	@Test
	public void testContainerCount()
	{
	    assertEquals(1, store.countCachedContainers(Laser.class));
	    assertEquals(1, store.countCachedContainers(Instrument.class));
	    assertEquals(3, store.countCachedContainers(Pixels.class));
	    assertEquals(5, store.countCachedContainers(null));
	}

	@Test
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
