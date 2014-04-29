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

package org.openmicroscopy.shoola.agents.util.ui;

import java.awt.Dimension;

import javax.swing.JSeparator;

/**
 * A JSeparator which does not expand in height (if it is used as horizontal
 * separator) or width (if it is used as vertical separator)
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; 
 *      <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class FixedSizeSeparator extends JSeparator {

    /**
     * See {@link JSeparator#JSeparator()}
     */
    public FixedSizeSeparator() {
        super();
    }

    /**
     * See  {@link JSeparator#JSeparator(int)}
     * @param arg0
     */
    public FixedSizeSeparator(int arg0) {
        super(arg0);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (getOrientation() == JSeparator.HORIZONTAL) {
            d.height = 12;
        } else if (getOrientation() == JSeparator.VERTICAL) {
            d.width = 12;
        }
        return d;
    }

}
