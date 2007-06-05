/*
 * org.openmicroscopy.shoola.agents.treeviewer.ImagesInContainerLoader
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.CategoryData;
import pojos.DataObject;
import pojos.DatasetData;

/** 
 * Loads the images contained in the specified container i.e. 
 * Images in a given Dataset or Category.
 * This class calls the <code>loadContainerHierarchy</code> method in the
 * <code>DataManagerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ImagesInContainerLoader
    extends DataBrowserLoader
{
    
    /** Indicates that the container is a <code>Dataset</code>. */
    public static final int DATASET = 0;
    
    /** Indicates that the container is a <code>Category</code>. */
    public static final int CATEGORY = 1;
    
    /** Collection of the ID of the selected nodes. */
    private Set<Long>		nodeIDs;
    
    /** The type of the node. */
    private Class           nodeType;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle      handle;
    
    /**
     * Returns the class corresponding to the specified type.
     * Returns <code>true</code> if the type is supported,
     * <code>false</code> otherwise.
     * 
     * @param type  The type of the root node.
     * @return See above.
     */
    private boolean validate(Class type)
    {
        return ((type.equals(DatasetData.class) || 
                type.equals(CategoryData.class)));
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param nodeType      The type of the node.
     * @param nodeID        The ID of the node.
     */
    public ImagesInContainerLoader(Browser viewer, Class nodeType, long nodeID)
    {
        super(viewer);
        if (!validate(nodeType))
            throw new IllegalArgumentException("Type not supported");
        if (nodeID < 0)
            throw new IllegalArgumentException("RootId not valid");
        this.nodeType = nodeType;
        nodeIDs = new HashSet<Long>(1);
        nodeIDs.add(new Long(nodeID));
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param nodeType      The type of the node.
     * @param nodeIDs       Collection of the ID of the selected nodes.
     */
    public ImagesInContainerLoader(Browser viewer, Class nodeType, 
    							Set<Long> nodeIDs)
    {
        super(viewer);
        if (!validate(nodeType))
            throw new IllegalArgumentException("Type not supported");
        if (nodeIDs == null || nodeIDs.size() == 0)
            throw new IllegalArgumentException("RootIds not valid");
        this.nodeType = nodeType;
        this.nodeIDs = nodeIDs;
    }
    
    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    { 
    	handle = dmView.loadContainerHierarchy(nodeType, nodeIDs, true,
    			convertRootLevel(), viewer.getRootID(), this);
    }

    /** 
     * Cancels the data loading. 
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        //viewer.setNodes((Set) result);
        Set nodes = (Set) result;
		Iterator i = nodes.iterator();
		DataObject object;
		Set images = new HashSet();
		while (i.hasNext()) {
			object = (DataObject) i.next();
			if (object instanceof DatasetData) 
				images.addAll(((DatasetData) object).getImages());
			else if (object instanceof CategoryData) 
				images.addAll(((CategoryData) object).getImages());
		}
		 viewer.setNodes(images);
    }
 
}

