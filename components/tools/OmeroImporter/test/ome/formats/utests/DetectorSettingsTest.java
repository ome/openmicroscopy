package ome.formats.utests;

import ome.formats.LSID;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.Instrument;
import omero.model.Image;
import omero.model.Pixels;
import omero.api.ServiceFactoryPrx;
import junit.framework.TestCase;

public class DetectorSettingsTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int DETECTOR_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final int LOGICAL_CHANNEL_INDEX = 0;
	
	private static final String DETECTOR_MODEL = "Model";
	
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
        
        // Add some metadata to the Detector to ensure that it is not lost.
        store.setDetectorModel(
        		DETECTOR_MODEL, INSTRUMENT_INDEX, DETECTOR_INDEX);
        
        // Set the LSID on our Objective and link to all three images. Also
        // link the Instrument to all three images.
        store.setDetectorID("Detector:0", INSTRUMENT_INDEX, DETECTOR_INDEX);
        store.setInstrumentID("Instrument:0", INSTRUMENT_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 1);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 2);
        store.setDetectorSettingsDetector(
        		"Detector:0", IMAGE_INDEX, LOGICAL_CHANNEL_INDEX);
        store.setDetectorSettingsDetector(
        		"Detector:0", IMAGE_INDEX + 1, LOGICAL_CHANNEL_INDEX);
        store.setDetectorSettingsDetector(
        		"Detector:0", IMAGE_INDEX + 2, LOGICAL_CHANNEL_INDEX);
	}

	public void testImageDetectorExists()
	{
	    for (int i = 0; i < 3; i++)
	    {
	        LSID lsid = new LSID(Pixels.class, i, PIXELS_INDEX);
	        assertNotNull(store.getSourceObject(lsid));
	    }
	    assertNotNull(store.getSourceObject(new LSID("Instrument:0")));
	    assertNotNull(store.getSourceObject(new LSID("Detector:0")));
	}
	
	public void testDetectorModelPreserved()
	{
	    Detector detector = store.getDetector(INSTRUMENT_INDEX,
	                                          DETECTOR_INDEX);
	    assertEquals(DETECTOR_MODEL, detector.getModel().getValue());
	}
	
	public void testContainerCount()
	{
	    assertEquals(1, store.countCachedContainers(Detector.class));
	    assertEquals(1, store.countCachedContainers(Instrument.class));
	    assertEquals(3, store.countCachedContainers(Pixels.class));
	    assertEquals(5, store.countCachedContainers(null));
	}

	public void testReferences()
	{
	    for (int i = 0; i < 3; i++)
	    {
	        LSID imageLsid = new LSID(Image.class, i);
	        LSID dsLsid = new LSID(DetectorSettings.class, i,
	                               LOGICAL_CHANNEL_INDEX);
	        assertTrue(store.hasReference(dsLsid, new LSID("Detector:0")));
	        assertTrue(store.hasReference(imageLsid, new LSID("Instrument:0")));
	    }
	    assertEquals(6, store.countCachedReferences(null, null));
	}
}
