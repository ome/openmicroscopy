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

package org.openmicroscopy.shoola.agents.treeviewer.view;

import java.util.List;

import org.openmicroscopy.shoola.env.event.RequestEvent;

import pojos.DataObject;

/**
 * Event to indicate that the user has selected one or more search result
 * objects.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 * @since 5.0
 */
public class SearchSelectionEvent extends RequestEvent {

    /** The selected objects */
    private List<DataObject> dataObjects;

    /**
     * Creates a new instance
     *
     * @param dataObjects The data objects to handle.
     */
    public SearchSelectionEvent(List<DataObject> dataObjects) {
        this.dataObjects = dataObjects;
    }

    /**
     * Get the selected {@link DataObject}s
     *
     * @return See above.
     */
    public List<DataObject> getDataObjects() {
        return dataObjects;
    }

    /**
     * Set the selected {@link DataObject}s
     *
     * @param dataObjects The data objects to handle.
     */
    public void setDataObjects(List<DataObject> dataObjects) {
        this.dataObjects = dataObjects;
    }

}
