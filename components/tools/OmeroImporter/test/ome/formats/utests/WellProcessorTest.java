package ome.formats.utests;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.util.LSID;
import ome.xml.model.primitives.NonNegativeInteger;
import omero.api.ServiceFactoryPrx;
import omero.model.Plate;
import omero.model.Well;
import junit.framework.TestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class WellProcessorTest extends TestCase
{
	private OMEROMetadataStoreClient store;
	
	private static final int PLATE_INDEX = 1;
	
	private static final int WELL_INDEX = 1;
	
	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        store.setWellColumn(new NonNegativeInteger(0), PLATE_INDEX, WELL_INDEX);
        store.setWellColumn(new NonNegativeInteger(1), PLATE_INDEX, WELL_INDEX + 1);
        store.setWellColumn(new NonNegativeInteger(0), PLATE_INDEX + 1, WELL_INDEX);
        store.setPlateName("setUp Plate", PLATE_INDEX + 1);
	}
	
	@Test
	public void testWellExists()
	{
		assertEquals(3, store.countCachedContainers(Well.class, null));
		assertEquals(1, store.countCachedContainers(Plate.class, null));
		LSID wellLSID1 = new LSID(Well.class, PLATE_INDEX, WELL_INDEX);
		LSID wellLSID2 = new LSID(Well.class, PLATE_INDEX, WELL_INDEX + 1);
		LSID wellLSID3 = new LSID(Well.class, PLATE_INDEX + 1, WELL_INDEX);
		LSID plateLSID1 = new LSID(Plate.class, PLATE_INDEX + 1);
		Well well1 = (Well) store.getSourceObject(wellLSID1);
		Well well2 = (Well) store.getSourceObject(wellLSID2);
		Well well3 = (Well) store.getSourceObject(wellLSID3);
		Plate plate1 = (Plate) store.getSourceObject(plateLSID1);
		assertNotNull(well1);
		assertNotNull(well2);
		assertNotNull(well3);
		assertNotNull(plate1);
		assertEquals(0, well1.getColumn().getValue());
		assertEquals(1, well2.getColumn().getValue());
		assertEquals(0, well3.getColumn().getValue());
		assertEquals("setUp Plate", plate1.getName().getValue());
	}
	
	@Test
	public void testWellPostProcess()
	{
		store.postProcess();
		assertEquals(3, store.countCachedContainers(Well.class, null));
		assertEquals(2, store.countCachedContainers(Plate.class, null));
		LSID plateLSID1 = new LSID(Plate.class, PLATE_INDEX);
		LSID plateLSID2 = new LSID(Plate.class, PLATE_INDEX + 1);
		Plate plate1 = (Plate) store.getSourceObject(plateLSID1);
		Plate plate2 = (Plate) store.getSourceObject(plateLSID2);
		assertNotNull(plate1);
		assertNotNull(plate2);
		assertEquals("Plate", plate1.getName().getValue());
		assertEquals("setUp Plate", plate2.getName().getValue());
	}
}
