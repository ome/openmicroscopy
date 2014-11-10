/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment
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

package omeis.providers.re;

import ome.model.enums.UnitsLength;
import ome.model.units.Length;
import ome.units.unit.Unit;
import ome.xml.model.enums.EnumerationException;

/**
 * Utility class to generate and convert unit objects.
 */
public class UnitsFactory {

    //
    // LENGTH
    //

    public static Length makeLength(double d, String unit) {
        UnitsLength ul = UnitsLength.valueOf(unit);
        Length copy = new Length();
        copy.setUnit(ul);
        copy.setValue(d);
        return copy;
    }

    public static ome.xml.model.enums.UnitsLength makeLengthUnitXML(String unit) {
        try {
            return ome.xml.model.enums.UnitsLength
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad length unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Length makeLengthXML(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Length> units =
                ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                        .getBaseUnit(makeLengthUnitXML(unit));
        return new ome.units.quantity.Length(d, units);
    }

    public static Length makeLength(double d,
            Unit<ome.units.quantity.Length> unit) {
        return makeLength(d, unit.getSymbol());
    }

    public static Length makeLength(double d, UnitsLength unit) {
        Length copy = new Length();
        copy.setUnit(unit);
        copy.setValue(d);
        return copy;
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length. A null will be
     * returned if the input is null.
     */
    public static Length convertLength(ome.units.quantity.Length value) {
        if (value == null)
            return null;
        UnitsLength ul = UnitsLength.bySymbol(value.unit().getSymbol());
        Length l = new Length();
        l.setValue(value.value().doubleValue());
        l.setUnit(ul);
        return l;
    }

    /**
     * FIXME: this should likely take a default so that locations which don't
     * want an exception can have
     *
     * log.warn("Using new PositiveFloat(1.0)!", e); return new
     * PositiveFloat(1.0);
     *
     * or similar.
     */
    public static ome.units.quantity.Length convertLength(Length t) {
        if (t == null) {
            return null;
        }

        Double length = t.getValue();
        String u = t.getUnit().getValue();
        ome.xml.model.enums.UnitsLength units = makeLengthUnitXML(u);
        ome.units.unit.Unit<ome.units.quantity.Length> units2 =
                ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                        .getBaseUnit(units);

        return new ome.units.quantity.Length(length, units2);
    }

    public static Length convertLength(Length value, Unit<ome.units.quantity.Length> ul) {
        return convertLength(value, ul.getSymbol());
    }

    public static Length convertLength(Length value, String target) {
        String source = value.getUnit().getValue();
        if (target.equals(source)) {
            return value;
        }
        throw new RuntimeException(String.format(
                "%d %s cannot be converted to %s", value.getValue(), source));
    }

}
