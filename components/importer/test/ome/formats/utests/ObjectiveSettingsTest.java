package ome.formats.utests;

import java.util.List;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.OMEROWrapper;
import ome.formats.testclient.TestServiceFactory;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.core.Image;
import ome.system.ServiceFactory;
import junit.framework.TestCase;

public class ObjectiveSettingsTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStore store;
	
	private static final int OBJECTIVE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final String OBJECTIVE_MODEL = "Model";
	
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
        
        // Add some metadata to the Objective to ensure that it is not lost.
        store.setObjectiveModel(
        		OBJECTIVE_MODEL, INSTRUMENT_INDEX, OBJECTIVE_INDEX);
        
        // Set the LSID on our Objective and link to all three images. Also
        // link the Instrument to all three images.
        store.setObjectiveID("Objective:0", INSTRUMENT_INDEX, OBJECTIVE_INDEX);
        store.setInstrumentID("Instrument:0", INSTRUMENT_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 1);
        store.setImageInstrumentRef("Instrument:0", IMAGE_INDEX + 2);
        store.setObjectiveSettingsObjective("Objective:0", IMAGE_INDEX);
        store.setObjectiveSettingsObjective("Objective:0", IMAGE_INDEX + 1);
        store.setObjectiveSettingsObjective("Objective:0", IMAGE_INDEX + 2);
	}

	public void testImageObjectiveSettingsExists()
	{
        List<Image> images = (List<Image>) store.getRoot();
        for (Image image : images)
        {
        	assertNotNull(image.getObjectiveSettings());
        }
	}
	
	public void testImageObjectiveSettingsObjectiveModelPreserved()
	{
        List<Image> images = (List<Image>) store.getRoot();
        ObjectiveSettings os = images.get(0).getObjectiveSettings();
        Objective objective = os.getObjective();
        assertEquals(OBJECTIVE_MODEL, objective.getModel());
	}

	public void testImageObjectiveSettingsReferences()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Objective objective =
        	images.get(0).getObjectiveSettings().getObjective();
        for (Image image : images)
        {
        	ObjectiveSettings os = image.getObjectiveSettings();
        	assertEquals(os.getObjective(), objective);
        }
	}
	
	public void testImageInstrumentObjectiveCount()
	{
        List<Image> images = (List<Image>) store.getRoot();
        for (Image image : images)
        {
        	assertEquals(1, image.getInstrument().sizeOfObjective());
        }
	}
	
	public void testImageInstrumentObjectiveModelPreserved()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Instrument instrument = images.get(0).getInstrument();
        Objective objective = instrument.iterateObjective().next();
        assertEquals(OBJECTIVE_MODEL, objective.getModel());
	}

	public void testInstrumentReferences()
	{
        List<Image> images = (List<Image>) store.getRoot();
        Objective objective =
        	images.get(0).getInstrument().iterateObjective().next();
        for (Image image : images)
        {
        	Instrument instrument = image.getInstrument();
        	assertEquals(instrument.iterateObjective().next(), objective);
        }
	}
}
