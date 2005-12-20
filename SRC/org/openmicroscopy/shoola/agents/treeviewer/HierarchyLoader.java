/*
 * org.openmicroscopy.shoola.agents.treeviewer.HierarchyLoader
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ProjectData;


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
public class HierarchyLoader
    extends DataLoader
{

    /** Indicates that the root node is of type <code>Project</code>. */
    public static final int PROJECT = 0;
    
    /** Indicates that the root node is of type <code>CategoryGroup</code>. */
    public static final int CATEGORY_GROUP = 1;
    
    /** Indicates that the root node is of type <code>Dataset</code>. */
    public static final int DATASET = 2;
    
    /** Indicates that the root node is of type <code>Category</code>. */
    public static final int CATEGORY = 3;
    /** 
     * <code>true</code> to retrieve the images,
     * <code>false</code> otherwise.
     */
    private boolean     images;
    
    /**
     * <code>true</code> to indicate that the output is displayed in a 
     * filtering widget, <code>false</code> otherwise.
     */
    private boolean     filter;
    
    /** The type of container. */
    private int         containerType;
    
    /** The type of the root node. */
    private Class       rootNodeType;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Returns the class corresponding to the specified type.
     * Returns <code>null</code> if the type is not supported.
     * 
     * @param type  The type of the root node.
     */
    private Class getClassType(int type)
    {
        switch (type) {
            case PROJECT:
                return ProjectData.class;
            case CATEGORY_GROUP:
                return CategoryGroupData.class;
            case CATEGORY:
                return CategoryData.class;
            case DATASET:
                return DatasetData.class;    
        }
        return null;
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param containerType One of the type defined by this class.
     */
    public HierarchyLoader(Browser viewer, int containerType)
    {
        this(viewer, containerType, false, false);
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param containerType One of the type defined by this class.
     * @param images        Passes <code>true</code> to retrieve the images,
     *                      <code>false</code> otherwise.
     * @param filter        Passes <code>true</code> to indicate that the 
     *                      output is displayed in a filtering widget,
     *                      <code>false</code> otherwise.
     */
    public HierarchyLoader(Browser viewer, int containerType, boolean images,
                            boolean filter)
    {
        super(viewer);
        rootNodeType = getClassType(containerType);
        if (rootNodeType == null)
            throw new IllegalArgumentException("Type not supported");
        this.containerType = containerType;
        this.images = images;
        this.filter = filter;
    }
    
    /**
     * Retrieves the data.
     * @see DataLoader#load()
     */
    public void load()
    {
        handle = dmView.loadContainerHierarchy(rootNodeType, null, images,
                                                this);
    }

    /**
     * Cancels the data loading.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** Feeds the result back to the viewer. */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        if (filter) viewer.setFilterNodes((Set) result, containerType);
        else viewer.setNodes((Set) result);
    }
    
}
