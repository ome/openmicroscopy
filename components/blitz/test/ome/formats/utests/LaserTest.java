/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package ome.formats.utests;

import java.util.LinkedHashMap;

import ome.formats.Index;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.UnitsFactory;
import ome.units.UNITS;
import ome.units.quantity.Frequency;
import ome.units.quantity.Length;
import ome.xml.model.enums.LaserMedium;
import ome.xml.model.enums.LaserType;
import ome.xml.model.enums.Pulse;
import omero.api.ServiceFactoryPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.Laser;
import omero.model.LengthI;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class LaserTest
{

	private OMEROMetadataStoreClient store;

	private static final int LIGHTSOURCE_INDEX = 0;

	private static final int INSTRUMENT_INDEX = 0;
	
	private static Length makeWave(double d) {
	    return UnitsFactory.convertLength(new LengthI(d, UnitsFactory.Channel_EmissionWavelength));
	}

	private static Frequency hz(double d) {
	    return UnitsFactory.convertFrequency(
	            UnitsFactory.makeFrequency(d, UNITS.HERTZ));
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
      Assert.assertEquals(laserContainer.LSID, "LightSource:100");
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
        Assert.assertEquals(laserContainer.LSID, "LightSource:100");
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
        Assert.assertEquals(laserContainer.LSID, "LightSource:100");
    }
}
