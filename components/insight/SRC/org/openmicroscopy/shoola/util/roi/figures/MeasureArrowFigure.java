/*
 * org.openmicroscopy.shoola.util.roi.figures.MeasureLineFigure 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.roi.figures;

import org.jhotdraw.draw.ArrowTip;
import org.openmicroscopy.shoola.util.roi.model.annotation.MeasurementAttributes;

/**
 * {@link MeasureLineFigure} with an {@link ArrowTip} as default line end
 * decoration
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class MeasureArrowFigure extends MeasureLineFigure {

    /**
     * See {@link MeasureLineFigure}
     */
    public MeasureArrowFigure() {
        setAttribute(MeasurementAttributes.END_DECORATION, new ArrowTip());
    }

    /**
     * See
     * {@link MeasureLineFigure#MeasureLineFigure(boolean, boolean, boolean, boolean, boolean)}
     */
    public MeasureArrowFigure(boolean readOnly, boolean clientObject,
            boolean editable, boolean deletable, boolean annotatable) {
        super(readOnly, clientObject, editable, deletable, annotatable);
        setAttribute(MeasurementAttributes.END_DECORATION, new ArrowTip());
    }

    /**
     * See
     * {@link MeasureLineFigure#MeasureLineFigure(String, boolean, boolean, boolean, boolean, boolean)}
     */
    public MeasureArrowFigure(String text, boolean readOnly,
            boolean clientObject, boolean editable, boolean deletable,
            boolean annotatable) {
        super(text, readOnly, clientObject, editable, deletable, annotatable);
        setAttribute(MeasurementAttributes.END_DECORATION, new ArrowTip());
    }

}
