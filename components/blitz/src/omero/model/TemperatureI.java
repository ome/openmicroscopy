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

import static ome.model.units.Conversion.Mul;
import static ome.model.units.Conversion.Add;
import static ome.model.units.Conversion.Int;
import static ome.model.units.Conversion.Pow;
import static ome.model.units.Conversion.Rat;
import static ome.model.units.Conversion.Sym;

import java.math.BigDecimal;

import java.util.Collections;
import java.util.Map;
import java.util.EnumMap;
import java.util.HashMap;

import ome.model.ModelBased;
import ome.model.units.BigResult;
import ome.model.units.Conversion;
import ome.units.unit.Unit;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.xml.model.enums.EnumerationException;

import omero.model.enums.UnitsTemperature;

/**
 * Blitz wrapper around the {@link ome.model.units.Temperature} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class TemperatureI extends Temperature implements ModelBased {

    private static final long serialVersionUID = 1L;

    private static Map<UnitsTemperature, Conversion> createMapCELSIUS() {
        EnumMap<UnitsTemperature, Conversion> c =
            new EnumMap<UnitsTemperature, Conversion>(UnitsTemperature.class);
        c.put(UnitsTemperature.FAHRENHEIT, Add(Mul(Rat(Int(9), Int(5)), Sym("c")), Int(32)));
        c.put(UnitsTemperature.KELVIN, Add(Sym("c"), Rat(Int(5463), Int(20))));
        c.put(UnitsTemperature.RANKINE, Add(Mul(Rat(Int(9), Int(5)), Sym("c")), Rat(Int(49167), Int(100))));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTemperature, Conversion> createMapFAHRENHEIT() {
        EnumMap<UnitsTemperature, Conversion> c =
            new EnumMap<UnitsTemperature, Conversion>(UnitsTemperature.class);
        c.put(UnitsTemperature.CELSIUS, Add(Mul(Rat(Int(5), Int(9)), Sym("f")), Rat(Int(-160), Int(9))));
        c.put(UnitsTemperature.KELVIN, Add(Mul(Rat(Int(5), Int(9)), Sym("f")), Rat(Int(45967), Int(180))));
        c.put(UnitsTemperature.RANKINE, Add(Sym("f"), Rat(Int(45967), Int(100))));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTemperature, Conversion> createMapKELVIN() {
        EnumMap<UnitsTemperature, Conversion> c =
            new EnumMap<UnitsTemperature, Conversion>(UnitsTemperature.class);
        c.put(UnitsTemperature.CELSIUS, Add(Sym("k"), Rat(Int(-5463), Int(20))));
        c.put(UnitsTemperature.FAHRENHEIT, Add(Mul(Rat(Int(9), Int(5)), Sym("k")), Rat(Int(-45967), Int(100))));
        c.put(UnitsTemperature.RANKINE, Mul(Rat(Int(9), Int(5)), Sym("k")));
        return Collections.unmodifiableMap(c);
    }

    private static Map<UnitsTemperature, Conversion> createMapRANKINE() {
        EnumMap<UnitsTemperature, Conversion> c =
            new EnumMap<UnitsTemperature, Conversion>(UnitsTemperature.class);
        c.put(UnitsTemperature.CELSIUS, Add(Mul(Rat(Int(5), Int(9)), Sym("r")), Rat(Int(-5463), Int(20))));
        c.put(UnitsTemperature.FAHRENHEIT, Add(Sym("r"), Rat(Int(-45967), Int(100))));
        c.put(UnitsTemperature.KELVIN, Mul(Rat(Int(5), Int(9)), Sym("r")));
        return Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsTemperature, Map<UnitsTemperature, Conversion>> conversions;
    static {

        Map<UnitsTemperature, Map<UnitsTemperature, Conversion>> c
            = new EnumMap<UnitsTemperature, Map<UnitsTemperature, Conversion>>(UnitsTemperature.class);

        c.put(UnitsTemperature.CELSIUS, createMapCELSIUS());
        c.put(UnitsTemperature.FAHRENHEIT, createMapFAHRENHEIT());
        c.put(UnitsTemperature.KELVIN, createMapKELVIN());
        c.put(UnitsTemperature.RANKINE, createMapRANKINE());
        conversions = Collections.unmodifiableMap(c);
    }

    private static final Map<UnitsTemperature, String> SYMBOLS;
    static {
        Map<UnitsTemperature, String> s = new HashMap<UnitsTemperature, String>();
        s.put(UnitsTemperature.CELSIUS, "°C");
        s.put(UnitsTemperature.FAHRENHEIT, "°F");
        s.put(UnitsTemperature.KELVIN, "K");
        s.put(UnitsTemperature.RANKINE, "°R");
        SYMBOLS = s;
    }

    public static String lookupSymbol(UnitsTemperature unit) {
        return SYMBOLS.get(unit);
    }

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new TemperatureI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsTemperature makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsTemperature
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Temperature unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Temperature makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Temperature> units =
                ome.xml.model.enums.handlers.UnitsTemperatureEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Temperature(d, units);
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
   public static ome.units.quantity.Temperature convert(Temperature t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsTemperature.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsTemperature units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Temperature> units2 =
               ome.xml.model.enums.handlers.UnitsTemperatureEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Temperature(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public TemperatureI() {
        super();
    }

    public TemperatureI(double d, UnitsTemperature unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public TemperatureI(double d,
            Unit<ome.units.quantity.Temperature> unit) {
        this(d, ome.model.enums.UnitsTemperature.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Temperature}
    * based on the given ome-xml enum
    */
   public TemperatureI(Temperature value, Unit<ome.units.quantity.Temperature> ul) throws BigResult {
       this(value,
            ome.model.enums.UnitsTemperature.bySymbol(ul.getSymbol()).toString());
   }

   /**
    * Copy constructor that converts the given {@link omero.model.Temperature}
    * based on the given ome.model enum
    */
   public TemperatureI(double d, ome.model.enums.UnitsTemperature ul) {
        this(d, UnitsTemperature.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Temperature}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public TemperatureI(Temperature value, String target) throws BigResult {
       String source = value.getUnit().toString();
       if (target.equals(source)) {
           setValue(value.getValue());
           setUnit(value.getUnit());
        } else {
            UnitsTemperature targetUnit = UnitsTemperature.valueOf(target);
            Conversion conversion = conversions.get(targetUnit).get(value.getUnit());
            if (conversion == null) {
                throw new RuntimeException(String.format(
                    "%f %s cannot be converted to %s",
                        value.getValue(), value.getUnit(), target));
            }
            double orig = value.getValue();
            BigDecimal big = conversion.convert(orig);
            double converted = big.doubleValue();
            if (Double.isInfinite(converted)) {
                throw new BigResult(big,
                        "Failed to convert " + source + ":" + target);
            }

            setValue(converted);
            setUnit(targetUnit);
       }
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public TemperatureI(Temperature value, UnitsTemperature target) throws BigResult {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public TemperatureI(ome.units.quantity.Temperature value) {
        ome.model.enums.UnitsTemperature internal =
            ome.model.enums.UnitsTemperature.bySymbol(value.unit().getSymbol());
        UnitsTemperature ul = UnitsTemperature.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsTemperature getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsTemperature unit, Ice.Current current) {
        this.unit = unit;
    }

    public String getSymbol(Ice.Current current) {
        return SYMBOLS.get(this.unit);
    }

    public Temperature copy(Ice.Current ignore) {
        TemperatureI copy = new TemperatureI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Temperature) {
            ome.model.units.Temperature t = (ome.model.units.Temperature) model;
            this.value = t.getValue();
            this.unit = UnitsTemperature.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Temperature cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsTemperature ut = ome.model.enums.UnitsTemperature.valueOf(getUnit().toString());
        ome.model.units.Temperature t = new ome.model.units.Temperature(getValue(), ut);
        return t;
    }

    // ~ Java overrides
    // =========================================================================

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((unit == null) ? 0 : unit.hashCode());
        long temp;
        temp = Double.doubleToLongBits(value);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Temperature(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Temperature other = (Temperature) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

