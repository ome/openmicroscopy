package ome.formats.utests;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ome.formats.LSID;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import omero.metadatastore.IObjectContainer;
import omero.model.Image;
import omero.model.ObjectiveSettings;
import omero.api.ServiceFactoryPrx;
import junit.framework.TestCase;

public class IObjectContainerStoreTest extends TestCase
{
	private OMEROMetadataStoreClient store;
	
	private static final int IMAGE_INDEX = 0;
	
	private static final int PIXELS_INDEX = 0;
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory();
        store = new OMEROMetadataStoreClient(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        
        // Two objects of the same type
        store.setImageName("Foo1", IMAGE_INDEX);
        store.setImageName("Foo2", IMAGE_INDEX + 1);
        
        // Object of a different type
        store.setPixelsSizeX(1, IMAGE_INDEX, PIXELS_INDEX);
        
        // Add a reference
        store.setObjectiveSettingsObjective("Objective:0", IMAGE_INDEX);
	}
	
	public void testGetCaches()
	{
		assertNotNull(store.getContainerCache());
		assertNotNull(store.getReferenceCache());
		assertNull(store.getReferenceStringCache());
	}
	
	public void testSetReferenceStringCache()
	{
		Map<String, String> a = new HashMap<String, String>();
		store.setReferenceStringCache(a);
		assertEquals(a, store.getReferenceStringCache());
	}
	
	public void testGetSourceObject()
	{
		assertNotNull(store.getSourceObject(new LSID(Image.class, 0)));
	}
	
	public void testGetSourceObjects()
	{
		assertEquals(2, store.getSourceObjects(Image.class).size());
	}
	
	public void testGetIObjectContainer()
	{
		LinkedHashMap<String, Integer> indexes = 
			new LinkedHashMap<String, Integer>();
		indexes.put("imageIndex", IMAGE_INDEX + 2);
		store.getIObjectContainer(Image.class, indexes);
		assertEquals(3, store.countCachedContainers(Image.class));
	}
	
	public void testCachedContainers()
	{
		assertEquals(2, store.countCachedContainers(Image.class));
	}
	
	public void testHasReference()
	{
		assertTrue(store.hasReference(new LSID(ObjectiveSettings.class, IMAGE_INDEX),
				                      new LSID("Objective:0")));
	}
}
