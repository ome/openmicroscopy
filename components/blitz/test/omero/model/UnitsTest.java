/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.model;

import java.io.IOException;

import junit.framework.TestCase;
import ome.model.units.BigResult;
import omero.model.enums.UnitsLength;
import omero.model.enums.UnitsTemperature;
import omero.model.enums.UnitsPower;

import org.testng.annotations.Test;

@Test(groups = "unit")
public class UnitsTest extends TestCase {

    @Test
    public void testPowerConversion() throws IOException, BigResult {
        Power p1 = new PowerI(100.1, UnitsPower.CENTIWATT);
        Power p2 = new PowerI(p1, UnitsPower.WATT);
        assertEquals(1.001, p2.getValue());
    }

    @Test
    public void testLengthSymbol() throws IOException {
        LengthI l = new LengthI(100.1, UnitsLength.MICROMETER);
        assertEquals("µm", l.getSymbol());
    }

    @Test
    public void testTemperatureConversion() throws IOException, BigResult {
        Temperature f = new TemperatureI(32, UnitsTemperature.FAHRENHEIT);
        Temperature c = new TemperatureI(f, UnitsTemperature.CELSIUS);
        Temperature k = new TemperatureI(c, UnitsTemperature.KELVIN);

        assertEquals(0, c.getValue(), 1e-5);
        assertEquals(273.15, k.getValue(), 1e-5);
    }
}
