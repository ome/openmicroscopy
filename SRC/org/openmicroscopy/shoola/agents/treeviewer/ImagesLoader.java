/*
 * org.openmicroscopy.shoola.agents.treeviewer.ImagesLoader
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

import java.util.Set;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.data.views.CallHandle;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImagesLoader
    extends DataBrowserLoader
{

    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    private Set         nodeIDs;
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     */
    public ImagesLoader(Browser viewer)
    { 
        this(viewer, null);
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param nodeIDs       The collection of root node IDs, either 
     *                      <code>Dataset</code> or <code>Category</code>.
     */
    public ImagesLoader(Browser viewer, Set nodeIDs)
    {
        super(viewer);
        this.nodeIDs = nodeIDs;
    }
    
    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    { 
        if (nodeIDs == null) handle = dmView.loadImages(this);
        else handle = dmView.loadImages(this);
    }

    /**
     * Retrieves the Category tree.
     * @see DataBrowserLoader#load()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer. 
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        viewer.setNodes((Set) result);
    }
    
}
