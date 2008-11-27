package ome.formats.utests;

import java.util.Iterator;
import java.util.List;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.MetaLightSource;
import ome.formats.importer.OMEROWrapper;
import ome.formats.testclient.TestServiceFactory;
import ome.model.acquisition.Detector;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.system.ServiceFactory;
import junit.framework.TestCase;

public class DetectorSettingsTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStore store;
	
	private static final int DETECTOR_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final int LOGICAL_CHANNEL_INDEX = 0;
	
	private static final String DETECTOR_MODEL = "Model";
	
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

	public void testImageDetectorSettingsExists()
	{
        List<Image> images = (List<Image>) store.getRoot();
        for (Image image : images)
        {
        	Iterator<Channel> iter = image.getPrimaryPixels().iterateChannels();
        	while (iter.hasNext())
        	{
        		Channel channel = iter.next();
        		LogicalChannel lc = channel.getLogicalChannel();
        		assertNotNull(lc.getDetectorSettings());
        	}
        }
	}
	
	public void testImageDetectorSettingsDetectorModelPreserved()
	{
        List<Image> images = (List<Image>) store.getRoot();
        for (Image image : images)
        {
        	Iterator<Channel> iter = image.getPrimaryPixels().iterateChannels();
        	while (iter.hasNext())
        	{
        		Channel channel = iter.next();
        		LogicalChannel lc = channel.getLogicalChannel();
        		Detector detector = lc.getDetectorSettings().getDetector();
        		assertEquals(DETECTOR_MODEL, detector.getModel());
        	}
        }
	}

	public void testImageDetectorSettingsReferences()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Detector detector = 
        	images.get(0).getPrimaryPixels().iterateChannels().next()
        		.getLogicalChannel().getDetectorSettings().getDetector();
        for (Image image : images)
        {
        	Iterator<Channel> iter = image.getPrimaryPixels().iterateChannels();
        	while (iter.hasNext())
        	{
        		Channel channel = iter.next();
        		LogicalChannel lc = channel.getLogicalChannel();
        		Detector channelDetector = 
        			lc.getDetectorSettings().getDetector();
        		assertEquals(detector, channelDetector);
        	}
        }
	}
	
	public void testImageInstrumentDetectorCount()
	{
        List<Image> images = (List<Image>) store.getRoot();
        for (Image image : images)
        {
        	assertEquals(1, image.getSetup().sizeOfDetector());
        }
	}
	
	public void testImageInstrumentDetectorModelPreserved()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Instrument instrument = images.get(0).getSetup();
        Detector detector = instrument.iterateDetector().next();
        assertEquals(DETECTOR_MODEL, detector.getModel());
	}

	public void testInstrumentReferences()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Detector detector =
        	images.get(0).getSetup().iterateDetector().next();
        for (Image image : images)
        {
        	Instrument instrument = image.getSetup();
        	assertEquals(instrument.iterateDetector().next(), detector);
        }
	}
}
