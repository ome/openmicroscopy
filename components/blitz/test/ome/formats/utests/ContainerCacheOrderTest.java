package ome.formats.utests;

import static ome.formats.model.UnitsFactory.convertLength;
import static ome.formats.model.UnitsFactory.convertPower;

import java.util.Map;

import junit.framework.TestCase;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.UnitsFactory;
import ome.units.quantity.Power;
import ome.util.LSID;
import ome.xml.model.enums.LaserMedium;
import ome.xml.model.enums.LaserType;
import ome.xml.model.primitives.PercentFraction;
import ome.xml.model.primitives.PositiveInteger;
import omero.api.ServiceFactoryPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.LengthI;
import omero.model.Plate;
import omero.model.PowerI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ContainerCacheOrderTest extends TestCase
{
	private OMEROWrapper wrapper;

	private OMEROMetadataStoreClient store;

	private static final int LIGHTSOURCE_INDEX = 0;

	private static final int INSTRUMENT_INDEX = 0;

	private static final int IMAGE_INDEX = 0;

	private static final int CHANNEL_INDEX = 0;

	private static final int OBJECTIVE_INDEX = 0;

	private static ome.units.quantity.Length makeWave(double d) {
	    return convertLength(new LengthI(d, UnitsFactory.Channel_EmissionWavelength));
	}

	Power watt(double d) {
	    return convertPower(new PowerI(d, UnitsFactory.LightSource_Power));
	}

	@BeforeMethod
	protected void setUp() throws Exception
	{
		ServiceFactoryPrx sf = new TestServiceFactory().proxy();
        wrapper = new OMEROWrapper(new ImportConfig());
        store = new OMEROMetadataStoreClient();
        store.initialize(sf);
        store.setReader(new TestReader());
        store.setEnumerationProvider(new TestEnumerationProvider());
        store.setInstanceProvider(
			new BlitzInstanceProvider(store.getEnumerationProvider()));
        wrapper.setMetadataStore(store);

        // Populate at least one image field.
        store.setImageName("Foo", IMAGE_INDEX);

        // Populate at least one pixels field.
        store.setPixelsSizeX(new PositiveInteger(1), IMAGE_INDEX);

        // Populate at least one logical channel field.
        store.setChannelEmissionWavelength(
            makeWave(100.1), IMAGE_INDEX, CHANNEL_INDEX);

        // Populate at least one instrument field.
        store.setInstrumentID("Instrument:0", INSTRUMENT_INDEX);

        // First Laser, First LightSourceSettings
		store.setLaserModel(
				"Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLaserID(
				"Laser:0", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLaserPower(
        watt(1.0), INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setLaserFrequencyMultiplication(
				new PositiveInteger(1), INSTRUMENT_INDEX, LIGHTSOURCE_INDEX);
		store.setChannelLightSourceSettingsID(
				"Laser:0", IMAGE_INDEX, CHANNEL_INDEX);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, CHANNEL_INDEX);

		// Second Laser, Second LightSourceSettings
		store.setLaserModel(
				"Model", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLaserID(
				"Laser:1", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLaserPower(
        watt(1.0), INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setLaserFrequencyMultiplication(
				new PositiveInteger(1), INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 1);
		store.setChannelLightSourceSettingsID(
				"Laser:1", IMAGE_INDEX, CHANNEL_INDEX + 1);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, CHANNEL_INDEX + 1);

		// Third Laser, Third LightSourceSettings (different orientation)
		store.setLaserLaserMedium(
				LaserMedium.AR, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 2);
		store.setLaserType(
				LaserType.GAS, INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 2);
		store.setLaserID(
				"Laser:2", INSTRUMENT_INDEX, LIGHTSOURCE_INDEX + 2);
		store.setChannelLightSourceSettingsID(
				"Laser:2", IMAGE_INDEX, CHANNEL_INDEX + 2);
		store.setChannelLightSourceSettingsAttenuation(
				new PercentFraction(1f), IMAGE_INDEX, CHANNEL_INDEX + 2);

		// First Objective, First ObjectiveSettings
		store.setObjectiveLensNA(1.0, INSTRUMENT_INDEX, OBJECTIVE_INDEX);
		store.setObjectiveID("Objective:0", INSTRUMENT_INDEX, OBJECTIVE_INDEX);
		store.setObjectiveSettingsID("Objective:0", IMAGE_INDEX);

		// Second Objective, Second ObjectiveSettings
		store.setObjectiveLensNA(1.0, INSTRUMENT_INDEX, OBJECTIVE_INDEX + 1);
		store.setObjectiveID("Objective:1", INSTRUMENT_INDEX, OBJECTIVE_INDEX + 1);
		store.setObjectiveSettingsID("Objective:1", IMAGE_INDEX + 1);

		// A Plate
		store.setPlateName("Plate", 0);
	}

	@Test
	public void testOrder()
	{
		Map<LSID, IObjectContainer> containerCache =
			store.getContainerCache();
		for (LSID key : containerCache.keySet())
		{
			System.err.println(key + " == " + containerCache.get(key).sourceObject);
		}
	}

	@Test
	public void testPlateLSIDEquivilence()
	{
		LSID a = new LSID(Plate.class, 0);
		LSID b = new LSID("omero.model.Plate:0");
		assertEquals(a, b);
		assertEquals(b, a);
	}

	@Test
	public void testGetPlateByString()
	{
		Map<LSID, IObjectContainer> containerCache =
			store.getContainerCache();
		assertNotNull(containerCache.get(new LSID("omero.model.Plate:0", true)));
	}

	@Test
	public void testGetPlateByClassAndIndex()
	{
		Map<LSID, IObjectContainer> containerCache =
			store.getContainerCache();
		assertNotNull(containerCache.get(new LSID(Plate.class, 0)));
	}
}
