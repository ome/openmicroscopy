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

/**
 * class storing both a time duration (double) and a unit for that duration
 * (e.g. ns, ms, s, etc.)
 */
@Embeddable
public class Time implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * string representation of the units which should be considering when
     * producing a representation of the {@link #time} field.
     */
    private String unit;

    // ~ Property accessors : used primarily by Hibernate
    // =========================================================================

    @Column(name = "value", nullable = false)
    public double getValue() {
        return this.value;
    }

    @Column(name = "unit", nullable = false)
    public String getUnit() {
        return this.unit;
    }

}
