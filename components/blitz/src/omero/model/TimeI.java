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

/**
 * Blitz wrapper around the {@link ome.model.util.Time} class.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class TimeI extends Time {

    private static final long serialVersionUID = 1L;

    public final static Ice.ObjectFactory Factory = new Ice.ObjectFactory() {

        public Ice.Object create(String arg0) {
            return new TimeI();
        }

        public void destroy() {
            // no-op
        }

    };

    public double getValue(Ice.Current current) {
        return this.value;
    }

    public void setValue(double time, Ice.Current current) {
        this.value = time;
    }

    public String getUnit(Ice.Current current) {
        return this.unit;
    }

    public void setUnit(String unit, Ice.Current current) {
        this.unit = unit;
    }

    public Time copy(Ice.Current ignore) {
        TimeI copy = new TimeI();
        copy.setValue(getValue());
        copy.setUnit(getUnit());
        return copy;
    }

}
