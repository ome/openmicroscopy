/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

import java.math.BigDecimal;


/**
 * Checked exception which is thrown from unit methods which can possibly
 * overflow. Use of {@link BigDecimal} in the {@link ome.model.units.Conversion}
 * prevents the overflow from happening prematurely, but once the value is to
 * be returned to the client, the ome.model (or dependent objects) will be
 * forced to transform the {@link BigDecimal} to a {@link Double}. If that
 * {@link Double} is either {@link Double#POSITIVE_INFINITY} or
 * {@link Double#NEGATIVE_INFINITY}, then this exception will be thrown. The
 * internal {@link BigDecimal} will be returned in the {@link #result} field
 * for consumption by the client.
 */
public class BigResult extends Exception {

    private static final long serialVersionUID = 626976940908390756L;

    public final BigDecimal result;

    public BigResult(BigDecimal result, String message) {
        super(message);
        this.result = result;
    }

}
