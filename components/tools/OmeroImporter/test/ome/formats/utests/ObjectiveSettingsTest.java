package ome.formats.utests;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import ome.xml.model.enums.Correction;
import ome.xml.model.primitives.PositiveInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.Image;
import omero.model.Instrument;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.Pixels;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ObjectiveSettingsTest extends TestCase
{
	private OMEROWrapper wrapper;
	
	private OMEROMetadataStoreClient store;
	
	private static final int OBJECTIVE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final String OBJECTIVE_MODEL = "Model";
	
	private static final Correction FL = ome.xml.model.enums.Correction.FL;
	
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
        store.setImageObjectiveSettingsID("Objective:0", IMAGE_INDEX);
        store.setImageObjectiveSettingsID("Objective:0", IMAGE_INDEX + 1);
        store.setImageObjectiveSettingsID("Objective:0", IMAGE_INDEX + 2);
	}

	@Test
	public void testObjectiveCorrectionExists()
	{
		Objective o =
			(Objective) store.getSourceObject(new LSID(Objective.class, 0, 0));
		assertNotNull(o);
		assertNotNull(o.getCorrection());
	}
	
	@Test
	public void testObjectiveCorrectionZeroLength()
	{
		store.setObjectiveCorrection(FL, INSTRUMENT_INDEX, OBJECTIVE_INDEX);
		Objective o =
			(Objective) store.getSourceObject(new LSID(Objective.class, 0, 0));
		assertNotNull(o);
		// Test enumeration provider always returns "Unknown", in reality this
		// should be "Other".
		assertEquals("Unknown", o.getCorrection().getValue().getValue());
	}
	
	@Test
	public void testObjectiveCorrectionNull()
	{
		store.setObjectiveCorrection(FL, INSTRUMENT_INDEX, OBJECTIVE_INDEX);
		Objective o =
			(Objective) store.getSourceObject(new LSID(Objective.class, 0, 0));
		assertNotNull(o);
		// Test enumeration provider always returns "Unknown", in reality this
		// should be "Other".
		assertEquals("Unknown", o.getCorrection().getValue().getValue());
	}
	
	@Test
	public void testImageObjectiveExists()
	{
	    for (int i = 0; i < 3; i++)
	    {
	        LSID lsid = new LSID(Pixels.class, i);
	        assertNotNull(store.getSourceObject(lsid));
	    }
	    assertNotNull(store.getSourceObject(new LSID(Instrument.class, 0)));
	    assertNotNull(store.getSourceObject(new LSID(Objective.class, 0, 0)));
	}
	
	@Test
	public void testObjectiveModelPreserved()
	{
	    Objective objective = store.getObjective(INSTRUMENT_INDEX,
	    		                                 OBJECTIVE_INDEX);
	    assertEquals(OBJECTIVE_MODEL, objective.getModel().getValue());
	}
	
	@Test
	public void testContainerCount()
	{
	    assertEquals(1, store.countCachedContainers(Objective.class));
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
	        LSID osLsid = new LSID(ObjectiveSettings.class,
	                               IMAGE_INDEX + i);
	        assertTrue(store.hasReference(osLsid, new LSID("Objective:0")));
	        assertTrue(store.hasReference(imageLsid, new LSID("Instrument:0")));
	    }
	    assertEquals(6, store.countCachedReferences(null, null));
	}
}
