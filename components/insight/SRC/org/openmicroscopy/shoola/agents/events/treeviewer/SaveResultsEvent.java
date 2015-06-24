/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.events.treeviewer;

//Java imports
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.env.event.RequestEvent;



/**
 * Post information about files to import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1
 */

public class SaveResultsEvent
    extends RequestEvent
{

    /** The object to import and results to save post import.*/
    private ResultsObject object;

    /** Flag indicating to first import the images.*/
    private boolean firstImport;

    /**
     * Creates a new instance.
     *
     * @param object The object to import.
     */
    public SaveResultsEvent(ResultsObject object)
    {
        this(object, false);
    }
    
    /**
     * Creates a new instance.
     *
     * @param object The object to import.
     * @param firstImport Pass <code>true</code> to first import the images,
     *                    <code>false</code> otherwise.
     */
    public SaveResultsEvent(ResultsObject object, boolean firstImport)
    {
        this.object = object;
        this.firstImport = firstImport;
    }

    /**
     * Returns <code>true</code> to import the images, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isFirstImport() { return firstImport; }

    /**
     * Returns the object to handle.
     *
     * @return See above.
     */
    public ResultsObject getObject() { return object; }

}
