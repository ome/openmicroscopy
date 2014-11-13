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

import ome.model.enums.UnitsElectricPotential;
import ome.util.Filter;
import ome.util.Filterable;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * class storing both a ElectricPotential and a unit for that ElectricPotential
 * (e.g. m, in, ly, etc.) encapsulated in a {@link UnitsElectricPotential} instance. As
 * also described in the remoting definition (.ice) for ElectricPotential, this is an
 * embedded class meaning that the columns here do not appear in their own
 * table but exist directly on the containing object. Like Details and
 * Permissions, instances do not contain long identifiers and cannot be
 * persisted on their own.
 */
@Embeddable
public class ElectricPotential implements Serializable, Filterable, ome.model.units.Unit {

    private static final long serialVersionUID = 1L;

    public final static String VALUE = "ome.model.units.ElectricPotential_value";

    public final static String UNIT = "ome.model.units.ElectricPotential_unit";

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
        String u = t.getUnit().getSymbol();
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
    public ElectricPotential() {
        // no-op
    }

    public ElectricPotential(double d, String u) {
        this.value = d;
        this.unit = UnitsElectricPotential.valueOf(u);
    }

    public ElectricPotential(double d, UnitsElectricPotential u) {
        this.value = d;
        this.unit = u;
    }

    public ElectricPotential(double d,
            Unit<ome.units.quantity.ElectricPotential> unit) {
        this(d, UnitsElectricPotential.bySymbol(unit.getSymbol()));
    }

    public ElectricPotential(ome.units.quantity.ElectricPotential value) {
        this(value.value().doubleValue(),
            UnitsElectricPotential.bySymbol(value.unit().getSymbol()));
    }

    // ~ Fields
    // =========================================================================

    /**
     * positive float representation of the ElectricPotential represented by this
     * field.
     */
    private double value;

    /**
     * representation of the units which should be considering when
     * producing a representation of the {@link #value} field.
     */
    private UnitsElectricPotential unit = null;

    // ~ Property accessors : used primarily by Hibernate
    // =========================================================================

    /**
     * value of this unit-field. It will be persisted to a column with the same
     * name as the containing field. For example, detectorSettings.getVolatage()
     * which is of type {@link ElectricPotential} will be stored in a column "detectorSettingsvolatage".
     **/
    @Column(name = "value", nullable = false)
    public double getValue() {
        return this.value;
    }

    /**
     * Many-to-one field ome.model.units.ElectricPotential.unit (ome.model.enums.UnitsElectricPotential).
     * These values are stored in a column suffixed by "Unit". Whereas {@link #value}
     * for physicalSizeX will be stored as "detectorSettings.volatage", the unit enum
     * will be stored as "detectorSettings.volatageUnit".
     */
    @javax.persistence.Column(name="unit", nullable=false,
        unique=false, insertable=true, updatable=true)
    @Type(type="ome.model.units.GenericEnumType",
          parameters=@Parameter(name="unit", value="ELECTRICPOTENTIAL"))
    public UnitsElectricPotential getUnit() {
        return this.unit;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setUnit(UnitsElectricPotential unit) {
        this.unit = unit;
    }

    @Override
    public boolean acceptFilter(Filter filter) {
        this.unit = (UnitsElectricPotential) filter.filter(UNIT, unit);
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
        return "ElectricPotential(" + value + " " + unit + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElectricPotential other = (ElectricPotential) obj;
        if (unit != other.unit)
            return false;
        if (Double.doubleToLongBits(value) != Double
                .doubleToLongBits(other.value))
            return false;
        return true;
    }

}

