/*
 * org.openmicroscopy.shoola.agents.treeviewer.ContainerCounterLoader
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.CategoryData;
import pojos.DatasetData;

/** 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public class ContainerCounterLoader
	extends DataBrowserLoader
{

    /** The collection of containers we want to analyse. */
    private Set			rootIDs;
    
    /** The type of the root node. */
    private Class		rootType;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Controls if the specified type is supported.
     * 
     * @param type The type to control.
     * @return 	<code>true</code> if the type is supported, <code>false</code>
     * 			otherwise.
     */
    private boolean checkRootType(Class type)
    {
        if ((type.equals(DatasetData.class)) || 
            (type.equals(CategoryData.class)))
            return true;
        return false;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer
     * @param rootType
     * @param rootIDs
     */
    public ContainerCounterLoader(Browser viewer, Class rootType, Set rootIDs)
    {
        super(viewer);
        if (rootIDs == null)
            throw new IllegalArgumentException("Collection shouldn't be null.");
        if (!(checkRootType(rootType)))
            throw new IllegalArgumentException("Root type not supported");
        this.rootIDs = rootIDs;
        this.rootType = rootType;
    }

    /**
     *  Retrieves the number of items contained in each specified container.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
        handle = dmView.countContainerItems(rootType, rootIDs, this);
    }

    /** Cancels the data loading. */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == Browser.DISCARDED) return; //Async cancel
        Map map = (Map) fe.getPartialResult();
        if (map == null) return; //Last fe has null object.
        //map should be only size == 1
        if (map.size() == 1) {
            Iterator i = map.keySet().iterator();
            Integer containerID;
            Integer value;
            while (i.hasNext()) {
                containerID = (Integer) i.next();
                value = (Integer) map.get(containerID);
                viewer.setContainerCountValue(containerID.intValue(),
                        						value.intValue());
            }
        }
    }

    
    /**
     * Does nothing as the async call returns <code>null</code>.
     * The actual payload (thumbnails) is delivered progressively
     * during the updates.
     */
    public void handleNullResult() {}
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataBrowserLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Thumbnail Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("Thumbnail Retrieval Failure", 
                                               s, exc);
    }
    
}
