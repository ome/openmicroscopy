package ome.formats.utests;

import java.util.LinkedHashMap;

import ome.formats.Index;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.xml.r201004.enums.*;
import ome.xml.r201004.primitives.*;
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
		store.setLaserID("Laser:100", INSTRUMENT_INDEX, i);
		store.setLaserWavelength(new PositiveInteger(100), INSTRUMENT_INDEX, i);
		store.setLaserType(LaserType.METALVAPOR, INSTRUMENT_INDEX, i);
		store.setLaserLaserMedium(LaserMedium.EMINUS, INSTRUMENT_INDEX, i);
		store.setLaserPockelCell(true, INSTRUMENT_INDEX, i);
		store.setLaserPulse(Pulse.REPETITIVE, INSTRUMENT_INDEX, i);
		store.setLaserRepetitionRate(2.0, INSTRUMENT_INDEX, i);
		store.setLaserTuneable(true, INSTRUMENT_INDEX, i);
	}

	public void testNewLaserIdFirst()
	{
	    int i = LIGHTSOURCE_INDEX + 10;
	    store.setLaserID("LightSource:100", INSTRUMENT_INDEX, i);
      store.setLaserType(LaserType.METALVAPOR, INSTRUMENT_INDEX, i);
      LinkedHashMap<Index, Integer> indexes =
          new LinkedHashMap<Index, Integer>();
      indexes.put(Index.INSTRUMENT_INDEX, INSTRUMENT_INDEX);
      indexes.put(Index.LIGHT_SOURCE_INDEX, i);
      IObjectContainer laserContainer = 
          store.getIObjectContainer(Laser.class, indexes);
      assertEquals("LightSource:100", laserContainer.LSID);
      assertEquals(laserContainer.sourceObject, laserContainer.sourceObject);
	}

    public void testNewLaserConcreteAttributeFirst()
    {
        int i = LIGHTSOURCE_INDEX + 10;
        store.setLaserType(LaserType.METALVAPOR, INSTRUMENT_INDEX, i);
        store.setLaserID("LightSource:100", INSTRUMENT_INDEX, i);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, INSTRUMENT_INDEX);
        indexes.put(Index.LIGHT_SOURCE_INDEX, i);
        IObjectContainer laserContainer = 
            store.getIObjectContainer(Laser.class, indexes);
        assertEquals("LightSource:100", laserContainer.LSID);
        assertEquals(laserContainer.sourceObject, laserContainer.sourceObject);
    }
    
    public void testNewLaserSuperclassAttributeLast()
    {
        int i = LIGHTSOURCE_INDEX + 10;
        store.setLaserID("LightSource:100", INSTRUMENT_INDEX, i);
        store.setLaserType(LaserType.METALVAPOR, INSTRUMENT_INDEX, i);
        store.setLaserModel("Bar", INSTRUMENT_INDEX, i);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, INSTRUMENT_INDEX);
        indexes.put(Index.LIGHT_SOURCE_INDEX, i);
        IObjectContainer laserContainer = 
            store.getIObjectContainer(Laser.class, indexes);
        assertEquals("LightSource:100", laserContainer.LSID);
        assertEquals(laserContainer.sourceObject, laserContainer.sourceObject);
    }
}
