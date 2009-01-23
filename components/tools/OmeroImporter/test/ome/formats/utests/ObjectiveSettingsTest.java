package ome.formats.utests;

import ome.formats.LSID;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.OMEROWrapper;
import omero.model.Instrument;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.Image;
import omero.model.Pixels;
import omero.api.ServiceFactoryPrx;
import junit.framework.TestCase;

public class ObjectiveSettingsTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int OBJECTIVE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	private static final String OBJECTIVE_MODEL = "Model";
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory();
        wrapper = new OMEROWrapper();
        store = new OMEROMetadataStoreClient(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
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

	public void testImageObjectiveExists()
	{
	    for (int i = 0; i < 3; i++)
	    {
	        LSID lsid = new LSID(Pixels.class, i, PIXELS_INDEX);
	        assertNotNull(store.getSourceObject(lsid));
	    }
	    assertNotNull(store.getSourceObject(new LSID("Instrument:0")));
	    assertNotNull(store.getSourceObject(new LSID("Objective:0")));
	}
	
	public void testObjectiveModelPreserved()
	{
	    Objective objective = store.getObjective(INSTRUMENT_INDEX,
	    		                                 OBJECTIVE_INDEX);
	    assertEquals(OBJECTIVE_MODEL, objective.getModel().getValue());
	}
	
	public void testContainerCount()
	{
	    assertEquals(1, store.countCachedContainers(Objective.class));
	    assertEquals(1, store.countCachedContainers(Instrument.class));
	    assertEquals(3, store.countCachedContainers(Pixels.class));
	    assertEquals(5, store.countCachedContainers(null));
	}
	
	public void testReferences()
	{
	    for (int i = 0; i < 3; i++)
	    {
	        LSID imageLsid = new LSID(Image.class, i);
	        LSID osLsid = new LSID(ObjectiveSettings.class,
	                               IMAGE_INDEX + i);
	        assertTrue(store.hasReference(osLsid, new LSID("Objective:0")));
	        assertTrue(store.hasReference(imageLsid, new LSID("Instrument:0")));
	    }
	    assertEquals(6, store.countCachedReferences(null, null));
	}
}
