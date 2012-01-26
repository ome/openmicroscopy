package ome.formats.utests;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import ome.util.LSID;
import ome.formats.Index;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.xml.model.primitives.PositiveInteger;
import omero.model.Image;
import omero.model.ObjectiveSettings;
import omero.model.Pixels;
import omero.api.ServiceFactoryPrx;
import junit.framework.TestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IObjectContainerStoreTest extends TestCase
{
	private OMEROMetadataStoreClient store;
	
	private static final int IMAGE_INDEX = 0;
	
	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setReader(new TestReader());
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
        
        // Two objects of the same type
        store.setImageName("Foo1", IMAGE_INDEX);
        store.setImageName("Foo2", IMAGE_INDEX + 1);
        
        // Objects of a different type
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX);
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX + 1);
        
        // Add a reference
        store.setImageObjectiveSettingsID("Objective:0", IMAGE_INDEX);
	}
	
	@Test
	public void testGetCaches()
	{
		assertNotNull(store.getContainerCache());
		assertNotNull(store.getReferenceCache());
		assertNull(store.getReferenceStringCache());
	}
	
	@Test
	public void testSetReferenceStringCache()
	{
		Map<String, String[]> a = new HashMap<String, String[]>();
		store.setReferenceStringCache(a);
		assertEquals(a, store.getReferenceStringCache());
	}
	
	@Test
	public void testGetSourceObject()
	{
		assertNotNull(store.getSourceObject(new LSID(Image.class, 0)));
	}
	
	@Test
	public void testGetSourceObjects()
	{
		assertEquals(2, store.getSourceObjects(Image.class).size());
	}
	
	@Test
	public void testGetIObjectContainer()
	{
		LinkedHashMap<Index, Integer> indexes =
			new LinkedHashMap<Index, Integer>();
		indexes.put(Index.IMAGE_INDEX, IMAGE_INDEX + 2);
		store.getIObjectContainer(Image.class, indexes);
		assertEquals(3, store.countCachedContainers(Image.class));
	}
	
	@Test
	public void testCachedContainers()
	{
		assertEquals(2, store.countCachedContainers(Image.class));
		assertEquals(2, store.countCachedContainers(Pixels.class));
		assertEquals(1, store.countCachedContainers(
				Pixels.class, IMAGE_INDEX));
		assertEquals(1, store.countCachedContainers(
				Pixels.class, IMAGE_INDEX + 1));
	}
	
	@Test
	public void testHasReference()
	{
		assertTrue(store.hasReference(new LSID(ObjectiveSettings.class,
				                      IMAGE_INDEX), new LSID("Objective:0")));
	}
	
	@Test
	public void testCount10000CachedContainers()
	{
		for (int i = 0; i < 10000; i++)
		{
			store.setImageName(String.valueOf(i), i);
		}
		long t0 = System.currentTimeMillis();
		store.countCachedContainers(Image.class, null);
		assertTrue((System.currentTimeMillis() - t0) < 100);
	}
	
	@Test
	public void testGet10000ContainersByClass()
	{
		for (int i = 0; i < 10000; i++)
		{
			store.setImageName(String.valueOf(i), i);
		}
		long t0 = System.currentTimeMillis();
		store.getIObjectContainers(Image.class);
		assertTrue((System.currentTimeMillis() - t0) < 100);
	}
}
