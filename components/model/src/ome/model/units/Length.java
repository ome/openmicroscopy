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

package ome.model.units;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import ome.units.unit.Unit;
import ome.xml.model.enums.EnumerationException;

import ome.model.enums.UnitsLength;
import ome.util.Filter;
import ome.util.Filterable;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * class storing both a Length and a unit for that Length
 * (e.g. m, in, ly, etc.) encapsulated in a {@link UnitsLength} instance. As
 * also described in the remoting definition (.ice) for Length, this is an
 * embedded class meaning that the columns here do not appear in their own
 * table but exist directly on the containing object. Like Details and
 * Permissions, instances do not contain long identifiers and cannot be
 * persisted on their own.
 */
@Embeddable
public class Length implements Serializable, Filterable, ome.model.units.Unit {

    private static final long serialVersionUID = 1L;

    public final static String VALUE = "ome.model.units.Length_value";

    public final static String UNIT = "ome.model.units.Length_unit";

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
        String u = t.getUnit().getSymbol();
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
        String source = value.getUnit().getSymbol();
        if (target.equals(source)) {
            return value;
        }
        throw new RuntimeException(String.format(
                "%f %s cannot be converted to %s",
                value.getValue(), value.getUnit().getSymbol(), source));
    }

    // ~ Constructors
    // =========================================================================

    /**
     * no-arg constructor to keep Hibernate happy.
     */
    @Deprecated
    public Length() {
        // no-op
    }

    public Length(double d, String u) {
        this.value = d;
        this.unit = UnitsLength.valueOf(u);
    }

    public Length(double d, UnitsLength u) {
        this.value = d;
        this.unit = u;
    }

    public Length(double d,
            Unit<ome.units.quantity.Length> unit) {
        this(d, UnitsLength.bySymbol(unit.getSymbol()));
    }

    public Length(ome.units.quantity.Length value) {
        this(value.value().doubleValue(),
            UnitsLength.bySymbol(value.unit().getSymbol()));
    }

    // ~ Fields
    // =========================================================================

    /**
     * positive float representation of the Length represented by this
     * field.
     */
    private double value;

    /**
     * representation of the units which should be considering when
     * producing a representation of the {@link #value} field.
     */
    private UnitsLength unit = null;

    // ~ Property accessors : used primarily by Hibernate
    // =========================================================================

    /**
     * value of this unit-field. It will be persisted to a column with the same
     * name as the containing field. For example, pixels.getPhysicalsizex()
     * which is of type {@link Length} will be stored in a column "pixelsphysicalSizeX".
     **/
    @Column(name = "value", nullable = false)
    public double getValue() {
        return this.value;
    }

    /**
     * Many-to-one field ome.model.units.Length.unit (ome.model.enums.UnitsLength).
     * These values are stored in a column suffixed by "Unit". Whereas {@link #value}
     * for physicalSizeX will be stored as "pixels.physicalSizeX", the unit enum
     * will be stored as "pixels.physicalSizeXUnit".
     */
    @javax.persistence.Column(name="unit", nullable=false,
        unique=false, insertable=true, updatable=true)
    @Type(type="ome.model.units.GenericEnumType",
          parameters=@Parameter(name="unit", value="LENGTH"))
    public UnitsLength getUnit() {
        return this.unit;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setUnit(UnitsLength unit) {
        this.unit = unit;
    }

    @Override
    public boolean acceptFilter(Filter filter) {
        this.unit = (UnitsLength) filter.filter(UNIT, unit);
        this.value = (Double) filter.filter(VALUE,  value);
        return true;
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
        return "Length(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Length other = (Length) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

