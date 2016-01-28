/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.util.Collection;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ScreenData;

/** 
 * Loads the screen/plate.
 * This class calls the <code>loadScreenPlates</code> method in the
 * <code>DataManagerView</code>. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta3
 */
public class ScreenPlateLoader
	extends DataBrowserLoader
{

	/** Indicates that the root node is of type <code>Screen</code>. */
    public static final int SCREEN = 0;
    
    /** Indicates that the root node is of type <code>Plate</code>. */
    public static final int PLATE = 1;
    
    /** The type of the root node. */
    private Class       		rootType;
    
    /** The parent the nodes to retrieve are for. */
    private TreeImageSet		parent;
    
    /** The node hosting the experimenter the data are for. */
    private TreeImageSet		expNode;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  		handle;
    
    /**
     * Returns the class corresponding to the specified type.
     * Returns <code>null</code> if the type is not supported,
     * otherwise the corresponding class.
     * 
     * @param type  The type of the root node.
     * @return See above.
     */
    private Class getClassType(int type)
    {
        switch (type) {
            case SCREEN: return ScreenData.class;
            case PLATE: return PlateData.class;
        }
        return null;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer  The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param expNode The node hosting the experimenter/group the data are for.
     * 				  Mustn't be <code>null</code>.
     * @param type 	  The type of the root node.
     * @param parent  The parent the nodes are for.
     */
	public ScreenPlateLoader(Browser viewer, SecurityContext ctx,
			TreeImageSet expNode, int type, TreeImageSet parent)
	{
		super(viewer, ctx);
		if (expNode == null)
        	throw new IllegalArgumentException("Node not valid.");
		Object ho = expNode.getUserObject();
		if (!(ho instanceof ExperimenterData || ho instanceof GroupData))
			throw new IllegalArgumentException("Node not valid.");
		rootType = getClassType(type);
		this.expNode = expNode;
		this.parent = parent;
	}
	
	/**
     * Creates a new instance.
     * 
     * @param viewer  The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param expNode The node hosting the experimenter the data are for.
     * 				  Mustn't be <code>null</code>.
     *  @param type   The type of the root node.
     */
	public ScreenPlateLoader(Browser viewer, SecurityContext ctx,
			TreeImageSet expNode, int type)
	{
		this(viewer, ctx, expNode, type, null);
	}
	
	 /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	long expID = -1;
    	if (expNode.getUserObject() instanceof ExperimenterData)
    		expID = ((ExperimenterData) expNode.getUserObject()).getId();
    	if (parent == null) 
    		handle = dmView.loadContainerHierarchy(ctx, ScreenData.class, null,
    				false, expID, this);
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
        if (parent == null) 
        	viewer.setExperimenterData(expNode, (Collection) result);
        else {
        	
        }
    }

}
