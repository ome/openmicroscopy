package ome.formats.utests;

import java.util.LinkedHashMap;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import omero.api.ServiceFactoryPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.Laser;
import omero.model.LightSource;
import junit.framework.TestCase;

public class LaserTest extends TestCase
{
	private OMEROMetadataStoreClient store;
	
	private static final int LIGHTSOURCE_INDEX = 0;
	
	private static final int INSTRUMENT_INDEX = 0;
	
	@Override
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
        		new BlitzInstanceProvider(store.getEnumerationProvider()));
	}
	
	public void testNewLaserAllAttributes()
	{
		int i = LIGHTSOURCE_INDEX + 10;
		store.setLightSourceID("Laser:100", INSTRUMENT_INDEX, i);
		store.setLaserWavelength(100, INSTRUMENT_INDEX, i);
		store.setLaserType("Foo", INSTRUMENT_INDEX, i);
		store.setLaserLaserMedium("Bar", INSTRUMENT_INDEX, i);
		store.setLaserPockelCell(true, INSTRUMENT_INDEX, i);
		store.setLaserPulse("Pulse", INSTRUMENT_INDEX, i);
		store.setLaserRepetitionRate(true, INSTRUMENT_INDEX, i);
		store.setLaserTuneable(true, INSTRUMENT_INDEX, i);
	}

	public void testNewLaserIdFirst()
	{
	    int i = LIGHTSOURCE_INDEX + 10;
	    store.setLightSourceID("LightSource:100", INSTRUMENT_INDEX, i);
        store.setLaserType("Foo", INSTRUMENT_INDEX, i);
        LinkedHashMap<String, Integer> indexes =
            new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", INSTRUMENT_INDEX);
        indexes.put("lightSourceIndex", i);
        IObjectContainer laserContainer = 
            store.getIObjectContainer(Laser.class, indexes);
        IObjectContainer lightSourceContainer =
            store.getIObjectContainer(LightSource.class, indexes);
        assertEquals("LightSource:100", laserContainer.LSID);
        assertEquals("LightSource:100", lightSourceContainer.LSID);
        assertEquals(laserContainer.sourceObject, laserContainer.sourceObject);
	}

    public void testNewLaserConcreteAttributeFirst()
    {
        int i = LIGHTSOURCE_INDEX + 10;
        store.setLaserType("Foo", INSTRUMENT_INDEX, i);
        store.setLightSourceID("LightSource:100", INSTRUMENT_INDEX, i);
        LinkedHashMap<String, Integer> indexes =
            new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", INSTRUMENT_INDEX);
        indexes.put("lightSourceIndex", i);
        IObjectContainer laserContainer = 
            store.getIObjectContainer(Laser.class, indexes);
        IObjectContainer lightSourceContainer =
            store.getIObjectContainer(LightSource.class, indexes);
        assertEquals("LightSource:100", laserContainer.LSID);
        assertEquals("LightSource:100", lightSourceContainer.LSID);
        assertEquals(laserContainer.sourceObject, laserContainer.sourceObject);
    }
    
    public void testNewLaserSuperclassAttributeLast()
    {
        int i = LIGHTSOURCE_INDEX + 10;
        store.setLightSourceID("LightSource:100", INSTRUMENT_INDEX, i);
        store.setLaserType("Foo", INSTRUMENT_INDEX, i);
        store.setLightSourceModel("Bar", INSTRUMENT_INDEX, i);
        LinkedHashMap<String, Integer> indexes =
            new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", INSTRUMENT_INDEX);
        indexes.put("lightSourceIndex", i);
        IObjectContainer laserContainer = 
            store.getIObjectContainer(Laser.class, indexes);
        IObjectContainer lightSourceContainer =
            store.getIObjectContainer(LightSource.class, indexes);
        assertEquals("LightSource:100", laserContainer.LSID);
        assertEquals("LightSource:100", lightSourceContainer.LSID);
        assertEquals(laserContainer.sourceObject, laserContainer.sourceObject);
    }
}
