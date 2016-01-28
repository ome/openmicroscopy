/*
 * org.openmicroscopy.shoola.agents.dataBrowser.DataObjectSaver 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DataObjectSaver 
	extends DataBrowserLoader
{
	
	/** The nodes to add to the passed objects. */
	private Collection	datasets;
	
	/** The nodes to add to the passed objects. */
	private Collection	images;
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param datasets	The datasets to add the images to.
     * 					Mustn't be <code>null</code>.
     * @param images	The images to add. Mustn't be <code>null</code>.
     */
    public DataObjectSaver(DataBrowser viewer, SecurityContext ctx,
    		Collection datasets, Collection images)
    {
    	super(viewer, ctx);
    	if (datasets == null || datasets.size() == 0) 
    		throw new IllegalArgumentException("No datasets to add the images" +
    				" to.");
    	if (images == null || images.size() == 0) 
    		throw new IllegalArgumentException("No images to add.");
    	this.datasets = datasets;
    	this.images = images;
    }
    
    /** 
	 * Cancels the data loading. 
	 * @see DataBrowserLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/** 
	 * Adds the passed images to the datasets.
	 * @see DataBrowserLoader#load()
	 */
	public void load()
	{

        handle = dmView.addExistingObjects(ctx, datasets, images, this);
	}
	
	/**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
    	viewer.refresh();
    }
    
}
