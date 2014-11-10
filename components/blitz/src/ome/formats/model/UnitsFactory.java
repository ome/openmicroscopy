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
import omero.model.ElectricPotential;
import omero.model.ElectricPotentialI;
import omero.model.Frequency;
import omero.model.FrequencyI;
import omero.model.Length;
import omero.model.LengthI;
import omero.model.Power;
import omero.model.PowerI;
import omero.model.Pressure;
import omero.model.PressureI;
import omero.model.Temperature;
import omero.model.TemperatureI;
import omero.model.Time;
import omero.model.TimeI;
import omero.model.enums.UnitsElectricPotential;
import omero.model.enums.UnitsFrequency;
import omero.model.enums.UnitsLength;
import omero.model.enums.UnitsPower;
import omero.model.enums.UnitsPressure;
import omero.model.enums.UnitsTemperature;
import omero.model.enums.UnitsTime;


/**
 * Utility class to generate and convert unit objects.
 */
public class UnitsFactory {


    //
    // ElectricPotential
    //

    public static ElectricPotential makeElectricPotential(double d, String unit) {
        UnitsElectricPotential ul = UnitsElectricPotential.valueOf(unit);
        ElectricPotential copy = new ElectricPotentialI();
        copy.setUnit(ul);
        copy.setValue(d);
        return copy;
    }

    public static ome.xml.model.enums.UnitsElectricPotential makeElectricPotentialUnitXML(String unit) {
        try {
            return ome.xml.model.enums.UnitsElectricPotential
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad ElectricPotential unit: " + unit, e);
        }
    }

    public static ome.units.quantity.ElectricPotential makeElectricPotentialXML(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.ElectricPotential> units =
                ome.xml.model.enums.handlers.UnitsElectricPotentialEnumHandler
                        .getBaseUnit(makeElectricPotentialUnitXML(unit));
        return new ome.units.quantity.ElectricPotential(d, units);
    }

    public static ElectricPotential makeElectricPotential(double d,
            Unit<ome.units.quantity.ElectricPotential> unit) {
        return makeElectricPotential(d, unit.getSymbol());
    }

    public static ElectricPotential makeElectricPotential(double d, UnitsElectricPotential unit) {
        ElectricPotential copy = new ElectricPotentialI();
        copy.setUnit(unit);
        copy.setValue(d);
        return copy;
    }

    /**
     * Convert a Bio-Formats {@link ElectricPotential} to an OMERO ElectricPotential. A null will be
     * returned if the input is null.
     */
    public static ElectricPotential convertElectricPotential(ome.units.quantity.ElectricPotential value) {
        if (value == null)
            return null;
        ome.model.enums.UnitsElectricPotential internal =
            ome.model.enums.UnitsElectricPotential.bySymbol(value.unit().getSymbol());
        UnitsElectricPotential ul = UnitsElectricPotential.valueOf(internal.toString());
        omero.model.ElectricPotential l = new omero.model.ElectricPotentialI();
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
   public static ome.units.quantity.ElectricPotential convertElectricPotential(ElectricPotential t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       String u = t.getUnit().toString();
       ome.xml.model.enums.UnitsElectricPotential units = makeElectricPotentialUnitXML(u);
       ome.units.unit.Unit<ome.units.quantity.ElectricPotential> units2 =
               ome.xml.model.enums.handlers.UnitsElectricPotentialEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.ElectricPotential(v, units2);
   }

   public static ElectricPotential convertElectricPotential(ElectricPotential value, Unit<ome.units.quantity.ElectricPotential> ul) {
       return convertElectricPotential(value, ul.getSymbol());
   }

   public static ElectricPotential convertElectricPotential(ElectricPotential value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           return value;
       }
       throw new RuntimeException(String.format(
               "%d %s cannot be converted to %s", value.getValue(), source));
   }


    //
    // Frequency
    //

    public static Frequency makeFrequency(double d, String unit) {
        UnitsFrequency ul = UnitsFrequency.valueOf(unit);
        Frequency copy = new FrequencyI();
        copy.setUnit(ul);
        copy.setValue(d);
        return copy;
    }

    public static ome.xml.model.enums.UnitsFrequency makeFrequencyUnitXML(String unit) {
        try {
            return ome.xml.model.enums.UnitsFrequency
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Frequency unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Frequency makeFrequencyXML(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Frequency> units =
                ome.xml.model.enums.handlers.UnitsFrequencyEnumHandler
                        .getBaseUnit(makeFrequencyUnitXML(unit));
        return new ome.units.quantity.Frequency(d, units);
    }

    public static Frequency makeFrequency(double d,
            Unit<ome.units.quantity.Frequency> unit) {
        return makeFrequency(d, unit.getSymbol());
    }

    public static Frequency makeFrequency(double d, UnitsFrequency unit) {
        Frequency copy = new FrequencyI();
        copy.setUnit(unit);
        copy.setValue(d);
        return copy;
    }

    /**
     * Convert a Bio-Formats {@link Frequency} to an OMERO Frequency. A null will be
     * returned if the input is null.
     */
    public static Frequency convertFrequency(ome.units.quantity.Frequency value) {
        if (value == null)
            return null;
        ome.model.enums.UnitsFrequency internal =
            ome.model.enums.UnitsFrequency.bySymbol(value.unit().getSymbol());
        UnitsFrequency ul = UnitsFrequency.valueOf(internal.toString());
        omero.model.Frequency l = new omero.model.FrequencyI();
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
   public static ome.units.quantity.Frequency convertFrequency(Frequency t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       String u = t.getUnit().toString();
       ome.xml.model.enums.UnitsFrequency units = makeFrequencyUnitXML(u);
       ome.units.unit.Unit<ome.units.quantity.Frequency> units2 =
               ome.xml.model.enums.handlers.UnitsFrequencyEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Frequency(v, units2);
   }

   public static Frequency convertFrequency(Frequency value, Unit<ome.units.quantity.Frequency> ul) {
       return convertFrequency(value, ul.getSymbol());
   }

   public static Frequency convertFrequency(Frequency value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           return value;
       }
       throw new RuntimeException(String.format(
               "%d %s cannot be converted to %s", value.getValue(), source));
   }


    //
    // Length
    //

    public static Length makeLength(double d, String unit) {
        UnitsLength ul = UnitsLength.valueOf(unit);
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
            throw new RuntimeException("Bad Length unit: " + unit, e);
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
        ome.model.enums.UnitsLength internal =
            ome.model.enums.UnitsLength.bySymbol(value.unit().getSymbol());
        UnitsLength ul = UnitsLength.valueOf(internal.toString());
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

       Double v = t.getValue();
       String u = t.getUnit().toString();
       ome.xml.model.enums.UnitsLength units = makeLengthUnitXML(u);
       ome.units.unit.Unit<ome.units.quantity.Length> units2 =
               ome.xml.model.enums.handlers.UnitsLengthEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Length(v, units2);
   }

   public static Length convertLength(Length value, Unit<ome.units.quantity.Length> ul) {
       return convertLength(value, ul.getSymbol());
   }

   public static Length convertLength(Length value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           return value;
       }
       throw new RuntimeException(String.format(
               "%d %s cannot be converted to %s", value.getValue(), source));
   }


    //
    // Power
    //

    public static Power makePower(double d, String unit) {
        UnitsPower ul = UnitsPower.valueOf(unit);
        Power copy = new PowerI();
        copy.setUnit(ul);
        copy.setValue(d);
        return copy;
    }

    public static ome.xml.model.enums.UnitsPower makePowerUnitXML(String unit) {
        try {
            return ome.xml.model.enums.UnitsPower
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Power unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Power makePowerXML(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Power> units =
                ome.xml.model.enums.handlers.UnitsPowerEnumHandler
                        .getBaseUnit(makePowerUnitXML(unit));
        return new ome.units.quantity.Power(d, units);
    }

    public static Power makePower(double d,
            Unit<ome.units.quantity.Power> unit) {
        return makePower(d, unit.getSymbol());
    }

    public static Power makePower(double d, UnitsPower unit) {
        Power copy = new PowerI();
        copy.setUnit(unit);
        copy.setValue(d);
        return copy;
    }

    /**
     * Convert a Bio-Formats {@link Power} to an OMERO Power. A null will be
     * returned if the input is null.
     */
    public static Power convertPower(ome.units.quantity.Power value) {
        if (value == null)
            return null;
        ome.model.enums.UnitsPower internal =
            ome.model.enums.UnitsPower.bySymbol(value.unit().getSymbol());
        UnitsPower ul = UnitsPower.valueOf(internal.toString());
        omero.model.Power l = new omero.model.PowerI();
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
   public static ome.units.quantity.Power convertPower(Power t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       String u = t.getUnit().toString();
       ome.xml.model.enums.UnitsPower units = makePowerUnitXML(u);
       ome.units.unit.Unit<ome.units.quantity.Power> units2 =
               ome.xml.model.enums.handlers.UnitsPowerEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Power(v, units2);
   }

   public static Power convertPower(Power value, Unit<ome.units.quantity.Power> ul) {
       return convertPower(value, ul.getSymbol());
   }

   public static Power convertPower(Power value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           return value;
       }
       throw new RuntimeException(String.format(
               "%d %s cannot be converted to %s", value.getValue(), source));
   }


    //
    // Pressure
    //

    public static Pressure makePressure(double d, String unit) {
        UnitsPressure ul = UnitsPressure.valueOf(unit);
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
            throw new RuntimeException("Bad Pressure unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Pressure makePressureXML(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Pressure> units =
                ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                        .getBaseUnit(makePressureUnitXML(unit));
        return new ome.units.quantity.Pressure(d, units);
    }

    public static Pressure makePressure(double d,
            Unit<ome.units.quantity.Pressure> unit) {
        return makePressure(d, unit.getSymbol());
    }

    public static Pressure makePressure(double d, UnitsPressure unit) {
        Pressure copy = new PressureI();
        copy.setUnit(unit);
        copy.setValue(d);
        return copy;
    }

    /**
     * Convert a Bio-Formats {@link Pressure} to an OMERO Pressure. A null will be
     * returned if the input is null.
     */
    public static Pressure convertPressure(ome.units.quantity.Pressure value) {
        if (value == null)
            return null;
        ome.model.enums.UnitsPressure internal =
            ome.model.enums.UnitsPressure.bySymbol(value.unit().getSymbol());
        UnitsPressure ul = UnitsPressure.valueOf(internal.toString());
        omero.model.Pressure l = new omero.model.PressureI();
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
   public static ome.units.quantity.Pressure convertPressure(Pressure t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       String u = t.getUnit().toString();
       ome.xml.model.enums.UnitsPressure units = makePressureUnitXML(u);
       ome.units.unit.Unit<ome.units.quantity.Pressure> units2 =
               ome.xml.model.enums.handlers.UnitsPressureEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Pressure(v, units2);
   }

   public static Pressure convertPressure(Pressure value, Unit<ome.units.quantity.Pressure> ul) {
       return convertPressure(value, ul.getSymbol());
   }

   public static Pressure convertPressure(Pressure value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           return value;
       }
       throw new RuntimeException(String.format(
               "%d %s cannot be converted to %s", value.getValue(), source));
   }


    //
    // Temperature
    //

    public static Temperature makeTemperature(double d, String unit) {
        UnitsTemperature ul = UnitsTemperature.valueOf(unit);
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
            throw new RuntimeException("Bad Temperature unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Temperature makeTemperatureXML(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Temperature> units =
                ome.xml.model.enums.handlers.UnitsTemperatureEnumHandler
                        .getBaseUnit(makeTemperatureUnitXML(unit));
        return new ome.units.quantity.Temperature(d, units);
    }

    public static Temperature makeTemperature(double d,
            Unit<ome.units.quantity.Temperature> unit) {
        return makeTemperature(d, unit.getSymbol());
    }

    public static Temperature makeTemperature(double d, UnitsTemperature unit) {
        Temperature copy = new TemperatureI();
        copy.setUnit(unit);
        copy.setValue(d);
        return copy;
    }

    /**
     * Convert a Bio-Formats {@link Temperature} to an OMERO Temperature. A null will be
     * returned if the input is null.
     */
    public static Temperature convertTemperature(ome.units.quantity.Temperature value) {
        if (value == null)
            return null;
        ome.model.enums.UnitsTemperature internal =
            ome.model.enums.UnitsTemperature.bySymbol(value.unit().getSymbol());
        UnitsTemperature ul = UnitsTemperature.valueOf(internal.toString());
        omero.model.Temperature l = new omero.model.TemperatureI();
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
   public static ome.units.quantity.Temperature convertTemperature(Temperature t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       String u = t.getUnit().toString();
       ome.xml.model.enums.UnitsTemperature units = makeTemperatureUnitXML(u);
       ome.units.unit.Unit<ome.units.quantity.Temperature> units2 =
               ome.xml.model.enums.handlers.UnitsTemperatureEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Temperature(v, units2);
   }

   public static Temperature convertTemperature(Temperature value, Unit<ome.units.quantity.Temperature> ul) {
       return convertTemperature(value, ul.getSymbol());
   }

   public static Temperature convertTemperature(Temperature value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           return value;
       }
       throw new RuntimeException(String.format(
               "%d %s cannot be converted to %s", value.getValue(), source));
   }


    //
    // Time
    //

    public static Time makeTime(double d, String unit) {
        UnitsTime ul = UnitsTime.valueOf(unit);
        Time copy = new TimeI();
        copy.setUnit(ul);
        copy.setValue(d);
        return copy;
    }

    public static ome.xml.model.enums.UnitsTime makeTimeUnitXML(String unit) {
        try {
            return ome.xml.model.enums.UnitsTime
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Time unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Time makeTimeXML(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Time> units =
                ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                        .getBaseUnit(makeTimeUnitXML(unit));
        return new ome.units.quantity.Time(d, units);
    }

    public static Time makeTime(double d,
            Unit<ome.units.quantity.Time> unit) {
        return makeTime(d, unit.getSymbol());
    }

    public static Time makeTime(double d, UnitsTime unit) {
        Time copy = new TimeI();
        copy.setUnit(unit);
        copy.setValue(d);
        return copy;
    }

    /**
     * Convert a Bio-Formats {@link Time} to an OMERO Time. A null will be
     * returned if the input is null.
     */
    public static Time convertTime(ome.units.quantity.Time value) {
        if (value == null)
            return null;
        ome.model.enums.UnitsTime internal =
            ome.model.enums.UnitsTime.bySymbol(value.unit().getSymbol());
        UnitsTime ul = UnitsTime.valueOf(internal.toString());
        omero.model.Time l = new omero.model.TimeI();
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
   public static ome.units.quantity.Time convertTime(Time t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       String u = t.getUnit().toString();
       ome.xml.model.enums.UnitsTime units = makeTimeUnitXML(u);
       ome.units.unit.Unit<ome.units.quantity.Time> units2 =
               ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Time(v, units2);
   }

   public static Time convertTime(Time value, Unit<ome.units.quantity.Time> ul) {
       return convertTime(value, ul.getSymbol());
   }

   public static Time convertTime(Time value, String target) {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           return value;
       }
       throw new RuntimeException(String.format(
               "%d %s cannot be converted to %s", value.getValue(), source));
   }



}

