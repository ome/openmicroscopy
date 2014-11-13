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

import ome.model.ModelBased;
import ome.units.unit.Unit;
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;
import ome.xml.model.enums.EnumerationException;

import omero.model.enums.UnitsTime;

/**
 * Blitz wrapper around the {@link ome.model.units.Time} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class TimeI extends Time implements ModelBased {

    private static final long serialVersionUID = 1L;

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new TimeI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    //
    // CONVERSIONS
    //

    public static ome.xml.model.enums.UnitsTime makeXMLUnit(String unit) {
        try {
            return ome.xml.model.enums.UnitsTime
                    .fromString((String) unit);
        } catch (EnumerationException e) {
            throw new RuntimeException("Bad Time unit: " + unit, e);
        }
    }

    public static ome.units.quantity.Time makeXMLQuantity(double d, String unit) {
        ome.units.unit.Unit<ome.units.quantity.Time> units =
                ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                        .getBaseUnit(makeXMLUnit(unit));
        return new ome.units.quantity.Time(d, units);
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
   public static ome.units.quantity.Time convert(Time t) {
       if (t == null) {
           return null;
       }

       Double v = t.getValue();
       // Use the code/symbol-mapping in the ome.model.enums files
       // to convert to the specification value.
       String u = ome.model.enums.UnitsTime.valueOf(
               t.getUnit().toString()).getSymbol();
       ome.xml.model.enums.UnitsTime units = makeXMLUnit(u);
       ome.units.unit.Unit<ome.units.quantity.Time> units2 =
               ome.xml.model.enums.handlers.UnitsTimeEnumHandler
                       .getBaseUnit(units);

       return new ome.units.quantity.Time(v, units2);
   }


    //
    // REGULAR ICE CLASS
    //

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public TimeI() {
        super();
    }

    public TimeI(double d, UnitsTime unit) {
        super();
        this.setUnit(unit);
        this.setValue(d);
    }

    public TimeI(double d,
            Unit<ome.units.quantity.Time> unit) {
        this(d, ome.model.enums.UnitsTime.bySymbol(unit.getSymbol()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Time}
    * based on the given ome-xml enum
    */
   public TimeI(Time value, Unit<ome.units.quantity.Time> ul) {
       this(value,
            ome.model.enums.UnitsTime.bySymbol(ul.getSymbol()).toString());
   }

   public TimeI(double d, ome.model.enums.UnitsTime ul) {
        this(d, UnitsTime.valueOf(ul.toString()));
    }

   /**
    * Copy constructor that converts the given {@link omero.model.Time}
    * based on the given enum string.
    *
    * @param target String representation of the CODE enum
    */
    public TimeI(Time value, String target) {
       String source = value.getUnit().toString();
       if (!target.equals(source)) {
            throw new RuntimeException(String.format(
               "%f %s cannot be converted to %s",
               value.getValue(), value.getUnit(), target));
       }
       setValue(value.getValue());
       setUnit(value.getUnit());
    }

   /**
    * Copy constructor that converts between units if possible.
    *
    * @param target unit that is desired. non-null.
    */
    public TimeI(Time value, UnitsTime target) {
        this(value, target.toString());
    }

    /**
     * Convert a Bio-Formats {@link Length} to an OMERO Length.
     */
    public TimeI(ome.units.quantity.Time value) {
        ome.model.enums.UnitsTime internal =
            ome.model.enums.UnitsTime.bySymbol(value.unit().getSymbol());
        UnitsTime ul = UnitsTime.valueOf(internal.toString());
        setValue(value.value().doubleValue());
        setUnit(ul);
    }

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double value , Ice.Current current) {
        this.value = value;
    }

    public UnitsTime getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsTime unit, Ice.Current current) {
        this.unit = unit;
    }

    public Time copy(Ice.Current ignore) {
        TimeI copy = new TimeI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Time) {
            ome.model.units.Time t = (ome.model.units.Time) model;
            this.value = t.getValue();
            this.unit = UnitsTime.valueOf(t.getUnit().toString());
        } else {
            throw new IllegalArgumentException(
              "Time cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        ome.model.enums.UnitsTime ut = ome.model.enums.UnitsTime.valueOf(getUnit().toString());
        ome.model.units.Time t = new ome.model.units.Time(getValue(), ut);
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
        return "Time(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Time other = (Time) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

