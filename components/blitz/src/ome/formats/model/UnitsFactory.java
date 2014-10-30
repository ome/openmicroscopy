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

package ome.formats.model;

import static omero.rtypes.rstring;
import static omero.rtypes.unwrap;
import ome.units.unit.Unit;
import ome.xml.model.enums.EnumerationException;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Pressure;
import omero.model.PressureI;
import omero.model.Temperature;
import omero.model.TemperatureI;
import omero.model.Time;
import omero.model.UnitsLength;
import omero.model.UnitsLengthI;
import omero.model.UnitsPressure;
import omero.model.UnitsPressureI;
import omero.model.UnitsTemperature;
import omero.model.UnitsTemperatureI;

/**
 * Utility class to generate and convert unit objects.
 */
public class UnitsFactory {

    //
    // LENGTH
    //

    public static Length makeLength(double d, String unit) {
        UnitsLength ul = new UnitsLengthI();
        ul.setValue(rstring(unit));
        Length copy = new LengthI();
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
        Length copy = new LengthI();
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
        omero.model.UnitsLength ul = new omero.model.UnitsLengthI();
        ul.setValue(rstring(value.unit().getSymbol()));

        omero.model.Length l = new omero.model.LengthI();
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
        String u = (String) unwrap(t.getUnit().getValue());
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
        String source = value.getUnit().getValue().getValue();
        if (target.equals(source)) {
            return value;
        }
        throw new RuntimeException(String.format(
                "%d %s cannot be converted to %s", value.getValue(), source));
    }

    //
    // PRESSURE
    //

    public static Pressure makePressure(double d, String unit) {
        UnitsPressure ul = new UnitsPressureI();
        ul.setValue(rstring(unit));
        Pressure copy = new PressureI();
        copy.setUnit(ul);
        copy.setValue(d);
        return copy;
    }

    public static ome.xml.model.enums.UnitsPressure makePressureUnitXML(String unit) {
        try {
            return ome.xml.model.enums.UnitsPressure
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad pressure unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Pressure convertPressure(Pressure t) {

        if (t == null) {
            return null;
        }

        Double pressure = t.getValue();
        String u = (String) unwrap(t.getUnit().getValue());
        ome.xml.model.enums.UnitsPressure units = makePressureUnitXML(u);
        ome.units.unit.Unit<ome.units.quantity.Pressure> units2 =
                ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                        .getBaseUnit(units);

        return new ome.units.quantity.Pressure(pressure, units2);
    }

    /**
     * Convert a Bio-Formats {@link Pressure} to an OMERO Pressure. A null will be
     * returned if the input is null.
     */
    public static Pressure convertPressure(ome.units.quantity.Pressure value) {
        if (value == null)
            return null;
        omero.model.UnitsPressure ul = new omero.model.UnitsPressureI();
        ul.setValue(rstring(value.unit().getSymbol()));

        omero.model.Pressure l = new omero.model.PressureI();
        l.setValue(value.value().doubleValue());
        l.setUnit(ul);
        return l;
    }

    //
    // TEMPERATURE
    //

    public static Temperature makeTemperature(double d, String unit) {
        UnitsTemperature ul = new UnitsTemperatureI();
        ul.setValue(rstring(unit));
        Temperature copy = new TemperatureI();
        copy.setUnit(ul);
        copy.setValue(d);
        return copy;
    }

    public static ome.xml.model.enums.UnitsTemperature makeTemperatureUnitXML(String unit) {
        try {
            return ome.xml.model.enums.UnitsTemperature
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad temperature unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Temperature convertTemperature(Temperature t) {

        if (t == null) {
            return null;
        }

        Double temperature = t.getValue();
        String u = (String) unwrap(t.getUnit().getValue());
        ome.xml.model.enums.UnitsTemperature units = makeTemperatureUnitXML(u);
        ome.units.unit.Unit<ome.units.quantity.Temperature> units2 =
                ome.xml.model.enums.handlers.UnitsTemperatureEnumHandler
                        .getBaseUnit(units);

        return new ome.units.quantity.Temperature(temperature, units2);
    }

    /**
     * Convert a Bio-Formats {@link Temperature} to an OMERO Temperature. A null will be
     * returned if the input is null.
     */
    public static Temperature convertTime(ome.units.quantity.Temperature value) {
        if (value == null)
            return null;
        omero.model.UnitsTemperature ut = new omero.model.UnitsTemperatureI();
        ut.setValue(rstring(value.unit().getSymbol()));

        omero.model.Temperature t = new omero.model.TemperatureI();
        t.setValue(value.value().doubleValue());
        t.setUnit(ut);
        return t;
    }

    /**
     * Convert a Bio-Formats {@link Temperature} to an OMERO Temperature. A null will be
     * returned if the input is null.
     */
    public static Temperature convertTemperature(ome.units.quantity.Temperature value) {
        if (value == null)
            return null;
        omero.model.UnitsTemperature ul = new omero.model.UnitsTemperatureI();
        ul.setValue(rstring(value.unit().getSymbol()));

        omero.model.Temperature l = new omero.model.TemperatureI();
        l.setValue(value.value().doubleValue());
        l.setUnit(ul);
        return l;
    }


    //
    // TIME
    //

    public static ome.units.quantity.Time convertTime(Time t) {

        if (t == null) {
            return null;
        }

        Double time = t.getValue();
        ome.xml.model.enums.UnitsTime units;
        try {
            units = ome.xml.model.enums.UnitsTime.fromString((String) unwrap(t
                    .getUnit().getValue()));
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad time: " + t, e);
        }
        ome.units.unit.Unit<ome.units.quantity.Time> units2 = ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                .getBaseUnit(units);
        ome.units.quantity.Time t2 = new ome.units.quantity.Time(time, units2);
        return t2;
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length. A null will be
     * returned if the input is null.
     */
    public static Time convertTime(ome.units.quantity.Time value) {
        if (value == null)
            return null;
        omero.model.UnitsTime ut = new omero.model.UnitsTimeI();
        ut.setValue(rstring(value.unit().getSymbol()));

        omero.model.Time t = new omero.model.TimeI();
        t.setValue(value.value().doubleValue());
        t.setUnit(ut);
        return t;
    }

}
