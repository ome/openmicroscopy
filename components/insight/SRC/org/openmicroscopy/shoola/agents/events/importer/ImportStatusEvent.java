/*
 * org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.events.importer;


import java.util.List;

import org.openmicroscopy.shoola.env.event.RequestEvent;
import pojos.DataObject;

/** 
 * Event indicating if there are on-going imports.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ImportStatusEvent
    extends RequestEvent
{

    /** Flag indicating if there are any on-going import. */
    private boolean importing;

    /** The collection of containers that will have to be refreshed. */
    private List<DataObject> containers;

    /** Flag indicating that the tree needs to be refreshed. */
    private boolean toRefresh;

    /** The successfully imported object or the failure.*/
    private Object importResult;

    /**
     * Creates a new instance.
     * 
     * @param importing Pass <code>true</code> to indicate on-going imports,
     *                  <code>false</code> otherwise.
     * @param containers The containers to refresh.
     * @param importResult The result of the import.
     */
    public ImportStatusEvent(boolean importing, List<DataObject> containers,
            Object importResult)
    {
        this.importing = importing;
        this.containers = containers;
        this.importResult = importResult;
    }

    /**
     * Sets to <code>true</code> to indicate to refresh the tree, 
     * <code>false</code> otherwise.
     * 
     * @param toRefresh Pass <code>true</code> to indicate to refresh the tree, 
     *                  <code>false</code> otherwise.
     */
    public void setToRefresh(boolean toRefresh) { this.toRefresh = toRefresh; }

    /**
     * Returns <code>true</code> to indicate to refresh the tree, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isToRefresh() { return toRefresh; }

    /**
     * Returns the containers that will have to be refreshed.
     *
     * @return See above.
     */
    public List<DataObject> getContainers() { return containers; }

    /**
     * Returns <code>true</code> if on-going imports, 
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    public boolean isImporting() { return importing; }

    /**
     * Returns the result of the import
     *
     * @return See above.
     */
    public Object getImportResult() { return importResult; }

}
