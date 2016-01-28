/*
 * org.openmicroscopy.shoola.agents.treeviewer.ContainerCounterLoader
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

package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the number of elements contained in the specified nodes. 
 * The nodes should correspond to either <code>Dataset</code> or 
 * <code>Image</code>.
 * This class calls the <code>countContainerItems</code> method in the
 * <code>DataManagerView</code>.
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

    /** The collection of <code>DataObject</code>s we want to analyze. */
    private Set					rootIDs;
    
    /** The collection of corresponding nodes. */
    private Set<TreeImageSet> 	nodes;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  		handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer  Reference to the Model. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param rootIDs The collection of <code>DataObject</code>s 
     *                we want to analyze.
     * @param nodes   The collection of corresponding nodes.
     */
    public ContainerCounterLoader(Browser viewer, SecurityContext ctx,
    	Set rootIDs, Set<TreeImageSet> nodes)
    {
        super(viewer, ctx);
        if (rootIDs == null)
            throw new IllegalArgumentException("Collection shouldn't be null.");
        this.rootIDs = rootIDs;
        this.nodes = nodes;
    }

    /**
     * Retrieves the number of items contained in each specified container.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
        handle = dmView.countContainerItems(ctx, rootIDs, this);
    }

    /** 
     * Cancels the data loading. 
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        Map map = (Map) result;
        if (map == null) return;
        Entry entry, child;
        Iterator i = map.entrySet().iterator();
        Long containerID;
        Long value;
        Map<Long, Long> values;
        Iterator j;
        while (i.hasNext()) {
        	entry = (Entry) i.next();
        	values = (Map<Long, Long>) entry.getValue();
        	j = values.entrySet().iterator();
        	while (j.hasNext()) {
				child = (Entry) j.next();
				containerID = (Long) child.getKey();
				value = (Long) child.getValue();
				viewer.setContainerCountValue(containerID.longValue(),
						value.longValue(), nodes);
			}
        }
    }
    
}
