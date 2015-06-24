/*
 * Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
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

package ome.formats.utests;

import junit.framework.TestCase;
import ome.model.units.BigResult;
import ome.units.UNITS;
import omero.model.LengthI;
import omero.model.enums.UnitsLength;

import org.testng.annotations.Test;

public class UnitsTest extends TestCase {

    /**
     * Creates a new length instance of the default units.
     *
     * @param d The value.
     * @return See above.
     */
    protected omero.model.Length mm(double d) {
        return mm(d, UnitsLength.MILLIMETER);
    }

    /**
     * Creates a new length instance of the specified units.
     *
     * @param d The value.
     * @param units The units.
     * @return See above.
     */
    protected omero.model.Length mm(double d, UnitsLength units) {
        omero.model.Length l = new omero.model.LengthI();
        l.setUnit(units);
        l.setValue(d);
        return l;
    }

    @Test
    public void testLengthConversion() {
        LengthI.convert(mm(1));
    }

    @Test
    public void testLengthMapping() throws BigResult {
        new LengthI(mm(1), UNITS.M);
    }

    @Test
    public void testLengthNoOpMapping() throws BigResult {
        new LengthI(mm(1), UNITS.MM);
    }

    @Test
    public void testLengthMappingFromCentimeterToMicrometer() throws BigResult {
        new LengthI(mm(1, UnitsLength.CENTIMETER), UnitsLength.MICROMETER);
    }

    @Test
    public void testLengthMappingFromMeterToMicrometer() throws BigResult {
        new LengthI(mm(1, UnitsLength.METER), UnitsLength.MICROMETER);
    }

    @Test
    public void testLengthMappingFromMicrometerToMicrometer() throws BigResult {
        new LengthI(mm(1, UnitsLength.MICROMETER), UnitsLength.MICROMETER);
    }

    @Test
    public void testLengthMappingFromMeterToCentimeter() throws BigResult {
        new LengthI(mm(1, UnitsLength.METER), UnitsLength.CENTIMETER);
    }

    @Test
    public void testLengthMappingFromCentimeterToMeter() throws BigResult {
        new LengthI(mm(1, UnitsLength.CENTIMETER), UnitsLength.METER);
    }
}
