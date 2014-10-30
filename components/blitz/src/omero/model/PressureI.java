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
import ome.util.Filterable;
import ome.util.ModelMapper;
import ome.util.ReverseModelMapper;

/**
 * Blitz wrapper around the {@link ome.model.util.Pressure} class.
 * Like {@link Details} and {@link Permissions}, this object
 * is embedded into other objects and does not have a full life
 * cycle of its own.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PressureI extends Pressure implements ModelBased {

    private static final long serialVersionUID = 1L;

    public static final Ice.ObjectFactory makeFactory(final omero.client client) {

        return new Ice.ObjectFactory() {

            public Ice.Object create(String arg0) {
                return new PressureI();
            }

            public void destroy() {
                // no-op
            }

        };
    };

    public final static Ice.ObjectFactory Factory = makeFactory(null);

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double time, Ice.Current current) {
        this.value = time;
    }

    public UnitsPressure getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(UnitsPressure unit, Ice.Current current) {
        this.unit = unit;
    }

    public Pressure copy(Ice.Current ignore) {
        PressureI copy = new PressureI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

    @Override
    public void copyObject(Filterable model, ModelMapper mapper) {
        if (model instanceof ome.model.units.Pressure) {
            ome.model.units.Pressure t = (ome.model.units.Pressure) model;
            this.value = t.getValue();
            this.unit = (omero.model.UnitsPressure) mapper.findTarget(t.getUnit());
        } else {
            throw new IllegalArgumentException(
              "Pressure cannot copy from " +
              (model==null ? "null" : model.getClass().getName()));
        }
    }

    @Override
    public Filterable fillObject(ReverseModelMapper mapper) {
        // This is a workaround for errors of the form:
        //
        //   org.hibernate.PropertyAccessException:
        //   IllegalArgumentException occurred calling getter of ome.model.screen.Well.wellSamples
        //
        // when importing plates with multiple images. Likely this
        // is caused by Hibernate trying through multiple paths
        // to resolve the UnitsPressure object and continually
        // failing to do so.
        ome.model.enums.UnitsPressure ut = new ome.model.enums.UnitsPressure();
        if (getUnit().getId() != null) {
            ut.setId(getUnit().getId().getValue());
            ut.unload();
        } else {
            if (getUnit().getValue() != null) {
                ut.setValue(getUnit().getValue().getValue());
            }
        }

        ome.model.units.Pressure t = new ome.model.units.Pressure();
        t.setValue(getValue());
        t.setUnit(ut);
        return t;
    }

}
