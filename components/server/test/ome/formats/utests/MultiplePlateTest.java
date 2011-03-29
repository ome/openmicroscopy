package ome.formats.utests;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import ome.util.LSID;
import ome.formats.OMEROMetadataStore;
import ome.model.core.Image;
import ome.model.screen.Plate;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import junit.framework.TestCase;

public class MultiplePlateTest extends TestCase
{
    private OMEROMetadataStore store;

    @Override
    protected void setUp() throws Exception
    {
        store = new OMEROMetadataStore();
        Map<String, Integer> indexes;

        // We are using separate loops for the below metadata population to
        // better mimic exactly the order that OMERO.importer sends us data.
        for (int i = 0; i < 9; i++)
        {
            Image image = new Image();
            indexes = new LinkedHashMap<String, Integer>();
            indexes.put("imageIndex", i);
            store.updateObject("Image:" + i, image, indexes);
        }

        for (int i = 0; i < 3; i++)
        {
            Plate plate = new Plate();
            plate.setName("Plate:" + i);
            indexes = new LinkedHashMap<String, Integer>();
            indexes.put("plateIndex", i);
            store.updateObject("Plate:" + i, plate, indexes);
        }
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                Well well = new Well();
                indexes = new LinkedHashMap<String, Integer>();
                indexes.put("plateIndex", i);
                indexes.put("wellIndex", j);
                store.updateObject(String.format(
                        "Well:%d:%d", i, j), well, indexes);
            }
        }
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                WellSample wellSample = new WellSample();
                indexes = new LinkedHashMap<String, Integer>();
                indexes.put("plateIndex", i);
                indexes.put("wellIndex", j);
                indexes.put("wellSampleIndex", 0);
                store.updateObject(String.format(
                        "WellSample:%d:%d:%d", i, j, 0), wellSample, indexes);
            }
        }

        Map<String, String[]> referenceCache = new HashMap<String, String[]>();
        for (int i = 0; i < 3; i++)
        {
            for (int j = 0; j < 3; j++)
            {
                referenceCache.put(
                        String.format("WellSample:%d:%d:%d", i, j, 0),
                        new String[] { "Image:" + ((i * 3) + j) });
            }
        }
        store.updateReferences(referenceCache);
    }

    public void testMetadata()
    {
        for (int i = 0; i < 3; i++)
        {
            Plate plate = (Plate) store.getObjectByLSID(new LSID("Plate:" + i));
            assertNotNull(plate);
            assertEquals("Plate:" + i, plate.getName());
            assertEquals(3, plate.sizeOfWells());
            Iterator<Well> wellIterator = plate.iterateWells();
            while (wellIterator.hasNext())
            {
                Well well = wellIterator.next();
                assertNotNull(well);
                assertEquals(1, well.sizeOfWellSamples());
                WellSample wellSample = well.iterateWellSamples().next();
                assertNotNull(wellSample);
                assertNotNull(wellSample.getImage());
            }
        }
    }

    /*
	public void testAddDetectorSettingsDetectorReference()
	{
	    Map<String, String[]> referenceCache = new HashMap<String, String[]>();
	    referenceCache.put("DetectorSettings:0", new String[] { "Detector:0" });
	    store.updateReferences(referenceCache);
	    DetectorSettings detectorSettings = (DetectorSettings)
	    	store.getObjectByLSID(new LSID("DetectorSettings:0"));
	    assertNotNull(detectorSettings.getDetector());
	}
	
	public void testAddObjectiveSettingsObjectiveReference()
	{
	    Map<String, String[]> referenceCache = new HashMap<String, String[]>();
	    referenceCache.put("ObjectiveSettings:0",
	    		           new String[] { "Objective:0" });
	    store.updateReferences(referenceCache);
	    ObjectiveSettings objectiveSettings = (ObjectiveSettings)
	    	store.getObjectByLSID(new LSID("ObjectiveSettings:0"));
	    assertNotNull(objectiveSettings.getObjective());
	}
	
	public void testAddDetectorAndObjectiveSettingsReferences()
	{
	    Map<String, String[]> referenceCache = new HashMap<String, String[]>();
	    
	    referenceCache.put("DetectorSettings:0", new String[] { "Detector:0"});
	    store.updateReferences(referenceCache);
	    DetectorSettings detectorSettings = (DetectorSettings)
	    	store.getObjectByLSID(new LSID("DetectorSettings:0"));
	    
	    referenceCache.put("ObjectiveSettings:0",
	    		           new String[] { "Objective:0"});
	    store.updateReferences(referenceCache);
	    ObjectiveSettings objectiveSettings = (ObjectiveSettings)
	    	store.getObjectByLSID(new LSID("ObjectiveSettings:0"));
	    
	    assertNotNull(objectiveSettings.getObjective());
	    assertNotNull(detectorSettings.getDetector());
	}
	*/
}
