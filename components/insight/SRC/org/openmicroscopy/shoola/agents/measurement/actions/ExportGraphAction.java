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

package org.openmicroscopy.shoola.agents.measurement.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.openmicroscopy.shoola.agents.measurement.view.MeasurementViewer;

/**
 * Action to export a graph as JPEG or PNG image file.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 */
public class ExportGraphAction extends MeasurementViewerAction {
    
    /**
     * Creates a new instance
     * 
     * @param model Reference to the MeasurementViewer
     */
    public ExportGraphAction(MeasurementViewer model) {
        this(model, "Export...", "Export the graph as JPEG or PNG.");
    }

    /**
     * Creates a new instance with a custom name/description
     * @param model Reference to the MeasurementViewer
     * @param name Name
     * @param desc Description
     */
    public ExportGraphAction(MeasurementViewer model, String name, String desc) {
        super(model);
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, desc);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        model.exportGraph();
    }

}
