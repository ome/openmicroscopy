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

import ome.model.enums.UnitsTime;
import ome.util.Filter;
import ome.util.Filterable;

/**
 * class storing both a time duration (double) and a unit for that duration
 * (e.g. ns, ms, s, etc.) encapsulated in a {@link UnitsTime} instance. As
 * also described in the remoting definition (.ice) for Time, this is an
 * embedded class meaning that the columns here do not appear in their own
 * table but exist directly on the containing object. Like Details and
 * Permissions, instances do not contain long identifiers and cannot be
 * persisted on their own.
 */
@Embeddable
public class Time implements Serializable, Filterable {

    private static final long serialVersionUID = 1L;

    public final static String VALUE = "ome.model.units.Time_value";

    public final static String UNIT = "ome.model.units.Time_unit";

    // ~ Constructors
    // =========================================================================

    public Time() {
    }

    // ~ Fields
    // =========================================================================

    /**
     * float representation of the time, i.e. duration, represented by this
     * field.
     */
    private double value;

    /**
     * representation of the units which should be considering when
     * producing a representation of the {@link #time} field.
     */
    private UnitsTime unit;

    // ~ Property accessors : used primarily by Hibernate
    // =========================================================================

    /**
     * value of this unit-field. It will be persisted to a column with the same
     * name as the containing field. For example, planeInfo.getExposureTime()
     * which is of type {@link Time} will be stored in a column "planeinfo.exposuretime".
     **/
    @Column(name = "value", nullable = false)
    public double getValue() {
        return this.value;
    }

    /**
     * Many-to-one field ome.model.units.Time.unit (ome.model.enums.UnitsTime).
     * These values are stored in a column suffixed by "Unit". Whereas {@link #value}
     * for exposureTime will be stored as "planeinfo.exposuretime", the unit enum
     * will be stored as "planeinfo.exposuretimeunit".
     */
     @javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY,cascade = {javax.persistence.CascadeType.MERGE, javax.persistence.CascadeType.PERSIST, javax.persistence.CascadeType.REFRESH},
             targetEntity = ome.model.enums.UnitsTime.class)
     @org.hibernate.annotations.Cascade({org.hibernate.annotations.CascadeType.LOCK, org.hibernate.annotations.CascadeType.MERGE, org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.REPLICATE, org.hibernate.annotations.CascadeType.REFRESH, org.hibernate.annotations.CascadeType.SAVE_UPDATE, org.hibernate.annotations.CascadeType.EVICT})
     @javax.persistence.JoinColumn(name="unit", nullable=false,
         unique=false, insertable=true, updatable=true)
     @org.hibernate.annotations.ForeignKey(name="FKtime_unit_unitstime")
    public UnitsTime getUnit() {
        return this.unit;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public void setUnit(UnitsTime unit) {
        this.unit = unit;
    }

    @Override
    public boolean acceptFilter(Filter filter) {
        this.unit = (UnitsTime) filter.filter(UNIT, unit);
        this.value = (Double) filter.filter(VALUE,  value);
        return true;
    }

}
