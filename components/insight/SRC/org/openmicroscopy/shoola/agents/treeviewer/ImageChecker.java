/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013-2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer;

import java.util.List;
import java.util.Map;

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.model.ImageCheckerResult;

import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import omero.gateway.model.DataObject;
/**
 * Checks if the images in the specified containers are split between
 * or not all selected.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class ImageChecker
	extends DataTreeViewerLoader
{

	public enum ImageCheckerType {
		/** Indicates that the action is a <code>Delete</code> action.*/
		DELETE,
		
		/** 
		 * Indicates that the action is a <code>Change group</code> action.
		 */
		CHGRP;
	}
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The objects to handle. */
    private Map<SecurityContext, List<DataObject>> objects;
    
    /** The action post check.*/
    private Object action;
    
    /** One of the constants defined but this class.*/
    private ImageCheckerType index;

    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param objects The objects to handle.
     * @param action The object to handle after the check.
     * @param index The type of action.
     */
	public ImageChecker(TreeViewer viewer, SecurityContext ctx,
			Map<SecurityContext, List<DataObject>> objects,
			Object action, ImageCheckerType index)
	{
		super(viewer, ctx);
		if (objects == null || objects.size() == 0)
			throw new IllegalArgumentException("No nodes specified.");
		this.objects = objects;
		this.action = action;
		this.index = index;
	}
	
	/**
     * Checks if the images are split.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	handle = dmView.getImagesBySplitFilesets(objects, this);
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
    	viewer.handleSplitImage((ImageCheckerResult) result,
    			action, index);
    }
}
