/*
 * org.openmicroscopy.shoola.agents.hiviewer.ClassificationsLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.agents.hiviewer;



//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

import pojos.ExperimenterData;

/** 
 * Loads, asynchronously, the CategoryGroup/Categgory nodes
 * containing the the specified image.
 * This class calls the <code>loadClassificationPaths</code> method in the
 * <code>HierarchyBrowsingView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ClassificationsLoader
    extends CBDataLoader
{

    /** The id of the image. */
    private long        imageID;
    
    /** The id of the root node. */
    private long        rootID;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param clipBoard     The {@link ClipBoard} this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param imageID       The id of the image.
     * @param rootID        The id of the root.    
     */
    public ClassificationsLoader(ClipBoard clipBoard, long imageID, long rootID)
    {
        super(clipBoard);
        this.imageID = imageID;
        this.rootID = rootID;
    }
    
    /**
     * Retrieves all the annotations linked to the specified image.
     * @see CBDataLoader#load()
     */
    public void load()
    {
        Set<Long> ids = new HashSet<Long>(1);
        ids.add(new Long(imageID));
        handle = hiBrwView.loadClassificationPaths(ids,
                        HierarchyBrowsingView.DECLASSIFICATION, 
                        ExperimenterData.class, rootID, this);
    }

    /** 
     * Cancels the data loading. 
     * @see CBDataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Overridden so that we don't notify the user that the annotation
     * retrieval has been cancelled.
     * @see CBDataLoader#handleCancellation() 
     */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }
    
    /**
     * Feeds the result back to the viewer.
     * @see CBDataLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        clipBoard.setClassifications((Set) result);
    }
    
}
