package ome.formats.utests;

import static ome.xml.model.LightSourceSettings.getWavelengthUnitXsdDefault;
import static ome.formats.model.UnitsFactory.convertFrequency;
import static ome.formats.model.UnitsFactory.convertLength;
import static ome.formats.model.UnitsFactory.makeFrequency;
import static ome.formats.model.UnitsFactory.makeLength;

import java.util.LinkedHashMap;

import ome.formats.Index;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.units.UNITS;
import ome.units.quantity.Frequency;
import ome.units.quantity.Length;
import ome.xml.model.enums.*;
import omero.api.ServiceFactoryPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.Laser;
import junit.framework.TestCase;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LaserTest extends TestCase
{

    private static final String WAVE_UNIT = getWavelengthUnitXsdDefault();

	private OMEROMetadataStoreClient store;

	private static final int LIGHTSOURCE_INDEX = 0;

	private static final int INSTRUMENT_INDEX = 0;
	
	private static Length makeWave(double d) {
	    return convertLength(makeLength(d, WAVE_UNIT));
	}

	private static Frequency hz(double d) {
	    return convertFrequency(makeFrequency(d, UNITS.HZ));
	}

	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
			new BlitzInstanceProvider(store.getEnumerationProvider()));
	}

	@Test
	public void testNewLaserAllAttributes()
	{
		int i = LIGHTSOURCE_INDEX + 10;
		store.setLaserID("Laser:100", INSTRUMENT_INDEX, i);
		store.setLaserWavelength(makeWave(100.1), INSTRUMENT_INDEX, i);
		store.setLaserType(LaserType.METALVAPOR, INSTRUMENT_INDEX, i);
		store.setLaserLaserMedium(LaserMedium.EMINUS, INSTRUMENT_INDEX, i);
		store.setLaserPockelCell(true, INSTRUMENT_INDEX, i);
		store.setLaserPulse(Pulse.REPETITIVE, INSTRUMENT_INDEX, i);
		store.setLaserRepetitionRate(hz(2.0), INSTRUMENT_INDEX, i);
		store.setLaserTuneable(true, INSTRUMENT_INDEX, i);
	}

	@Test
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

	@Test
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

	@Test
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
