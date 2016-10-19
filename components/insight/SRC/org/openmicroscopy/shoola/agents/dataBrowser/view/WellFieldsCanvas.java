/*
 * org.openmicroscopy.shoola.agents.dataBrowser.view.WellFieldsCanvas 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.awt.Dimension;
import java.awt.Point;
import java.util.List;

import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.WellSampleNode;

/**
 * Display all the fields for a given well.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public abstract class WellFieldsCanvas extends JPanel {

    /** Reference to the parent. */
    WellFieldsView parent;
    
    /**
     * Creates a new instance
     * 
     * @param parent
     *            Reference to the parent {@link WellFieldsView}
     */
    public WellFieldsCanvas(WellFieldsView parent) {
        this.parent = parent;
    }

    /**
     * Refresh the UI
     */
    public abstract void refreshUI();

    /**
     * Clear/Reset the canvas
     * 
     * @param titles
     *            The titles of the selected wells
     * @param nFields
     *            The maximum number of fields per well
     * @param thumbDim
     *            The expected thumbnail dimensions
     */
    public abstract void clear(List<String> titles, int nFields,
            Dimension thumbDim);

    /**
     * Updates/Adds a particular field thumbnail
     * 
     * @param node
     *            The field
     */
    public abstract void updateFieldThumb(WellSampleNode node);
    
    /**
     * Get the {@link WellSampleNode} at a certain position
     * 
     * @param p
     *            The Position
     * @return The {@link WellSampleNode} at the given position
     */
    public abstract WellSampleNode getNode(Point p);

}
