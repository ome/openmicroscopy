package ome.formats.utests;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ome.util.LSID;
import ome.formats.OMEROMetadataStore;
import ome.model.acquisition.Detector;
import ome.model.acquisition.DetectorSettings;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.Pixels;
import junit.framework.TestCase;

public class DetectorSettingsDetectorTest extends TestCase
{
	private OMEROMetadataStore store;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int DETECTOR_INDEX = 0;
	
	private static final int OBJECTIVE_INDEX = 0;
	
	private static final int LOGICAL_CHANNEL_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	@Override
	protected void setUp() throws Exception
	{
        store = new OMEROMetadataStore();

        String imageLSID = "Image:0";
        Image image = new Image();
        Map<String, Integer> imageIndexes = 
            new LinkedHashMap<String, Integer>();
        imageIndexes.put("imageIndex", IMAGE_INDEX);
        
        String pixelsLSID = "Pixels:0";
        Pixels pixels = new Pixels();
        Map<String, Integer> pixelsIndexes = 
            new LinkedHashMap<String, Integer>();
        pixelsIndexes.put("imageIndex", IMAGE_INDEX);
        pixelsIndexes.put("pixelsIndex", PIXELS_INDEX);
        
        String logicalChannelLSID = "LogicalChannel:0";
        LogicalChannel logicalChannel = new LogicalChannel();
        Map<String, Integer> logicalChannelIndexes = 
            new LinkedHashMap<String, Integer>();
        logicalChannelIndexes.put("imageIndex", IMAGE_INDEX);
        logicalChannelIndexes.put("logicalChannelIndex", LOGICAL_CHANNEL_INDEX);
        
        String instrumentLSID = "Instrument:0";
        Instrument instrument = new Instrument();
        Map<String, Integer> instrumentIndexes = 
            new LinkedHashMap<String, Integer>();
        instrumentIndexes.put("instrumentIndex", INSTRUMENT_INDEX);
        
        String detectorLSID = "Detector:0";
        Detector detector = new Detector();
        Map<String, Integer> detectorIndexes = 
            new LinkedHashMap<String, Integer>();
        detectorIndexes.put("instrumentIndex", INSTRUMENT_INDEX);
        detectorIndexes.put("detectorIndex", DETECTOR_INDEX);
        
        String objectiveLSID = "Objective:0";
        Objective objective = new Objective();
        Map<String, Integer> objectiveIndexes = 
            new LinkedHashMap<String, Integer>();
        objectiveIndexes.put("instrumentIndex", INSTRUMENT_INDEX);
        objectiveIndexes.put("objectiveIndex", OBJECTIVE_INDEX);
        
        String detectorSettingsLSID = "DetectorSettings:0";
        DetectorSettings detectorSettings = new DetectorSettings();
        Map<String, Integer> detectorSettingsIndexes = 
            new LinkedHashMap<String, Integer>();
        detectorSettingsIndexes.put("imageIndex", IMAGE_INDEX);
        detectorSettingsIndexes.put("logicalChannelIndex", LOGICAL_CHANNEL_INDEX);
        
        String objectiveSettingsLSID = "ObjectiveSettings:0";
        ObjectiveSettings objectiveSettings = new ObjectiveSettings();
        Map<String, Integer> objectiveSettingsIndexes = 
            new LinkedHashMap<String, Integer>();
        objectiveSettingsIndexes.put("imageIndex", IMAGE_INDEX);

        store.updateObject(imageLSID, image, imageIndexes);
        store.updateObject(pixelsLSID, pixels, pixelsIndexes);
        store.updateObject(instrumentLSID, instrument, instrumentIndexes);
        store.updateObject(logicalChannelLSID, logicalChannel,
        		           logicalChannelIndexes);
        store.updateObject(detectorLSID, detector, detectorIndexes);
        store.updateObject(objectiveLSID, objective, objectiveIndexes);
        store.updateObject(detectorSettingsLSID, detectorSettings,
        		           detectorSettingsIndexes);
        store.updateObject(objectiveSettingsLSID, objectiveSettings,
		                   objectiveSettingsIndexes);
	}
	
	public void testAddDetectorSettingsDetectorReference()
	{
	    Map<String, String> referenceCache = new HashMap<String, String>();
	    referenceCache.put("DetectorSettings:0", "Detector:0");
	    store.updateReferences(referenceCache);
	    DetectorSettings detectorSettings = (DetectorSettings)
	    	store.getObjectByLSID(new LSID("DetectorSettings:0"));
	    assertNotNull(detectorSettings.getDetector());
	}
	
	public void testAddObjectiveSettingsObjectiveReference()
	{
	    Map<String, String> referenceCache = new HashMap<String, String>();
	    referenceCache.put("ObjectiveSettings:0", "Objective:0");
	    store.updateReferences(referenceCache);
	    ObjectiveSettings objectiveSettings = (ObjectiveSettings)
	    	store.getObjectByLSID(new LSID("ObjectiveSettings:0"));
	    assertNotNull(objectiveSettings.getObjective());
	}
	
	public void testAddDetectorAndObjectiveSettingsReferences()
	{
	    Map<String, String> referenceCache = new HashMap<String, String>();
	    
	    referenceCache.put("DetectorSettings:0", "Detector:0");
	    store.updateReferences(referenceCache);
	    DetectorSettings detectorSettings = (DetectorSettings)
	    	store.getObjectByLSID(new LSID("DetectorSettings:0"));
	    
	    referenceCache.put("ObjectiveSettings:0", "Objective:0");
	    store.updateReferences(referenceCache);
	    ObjectiveSettings objectiveSettings = (ObjectiveSettings)
	    	store.getObjectByLSID(new LSID("ObjectiveSettings:0"));
	    
	    assertNotNull(objectiveSettings.getObjective());
	    assertNotNull(detectorSettings.getDetector());
	}
}
