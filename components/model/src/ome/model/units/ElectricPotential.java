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

import ome.model.enums.UnitsElectricPotential;
import ome.util.Filter;
import ome.util.Filterable;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

/**
 * class storing both a electricpotential and a unit for that potential
 * (e.g. C, K, etc.) encapsulated in a {@link UnitsElectricPotential} instance. As
 * also described in the remoting definition (.ice) for ElectricPotential, this is an
 * embedded class meaning that the columns here do not appear in their own
 * table but exist directly on the containing object. Like Details and
 * Permissions, instances do not contain long identifiers and cannot be
 * persisted on their own.
 */
@Embeddable
public class ElectricPotential implements Serializable, Filterable {

    private static final long serialVersionUID = 1L;

    public final static String VALUE = "ome.model.units.ElectricPotential_value";

    public final static String UNIT = "ome.model.units.ElectricPotential_unit";

    // ~ Constructors
    // =========================================================================

    public ElectricPotential() {
    }

    // ~ Fields
    // =========================================================================

    /**
     * positive float representation of the potential represented by this
     * field.
     */
    private double value;

    /**
     * representation of the units which should be considering when
     * producing a representation of the {@link #electricPotential} field.
     */
    private UnitsElectricPotential unit;

    // ~ Property accessors : used primarily by Hibernate
    // =========================================================================

    /**
     * value of this unit-field. It will be persisted to a column with the same
     * name as the containing field. For example, detectorSettings.getVoltage()
     * which is of type {@link ElectricPotential} will be stored in a column "detectorsettings.voltage".
     **/
    @Column(name = "value", nullable = false)
    public double getValue() {
        return this.value;
    }

    /**
     * Many-to-one field ome.model.units.ElectricPotential.unit (ome.model.enums.UnitsElectricPotential).
     * These values are stored in a column suffixed by "Unit". Whereas {@link #value}
     * for physicalSizeX will be stored as "detectorsettings.voltage", the unit enum
     * will be stored as "detectorsettings.voltageunit".
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

}
