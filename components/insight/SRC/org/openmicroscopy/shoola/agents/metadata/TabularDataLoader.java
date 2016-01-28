/*
 * org.openmicroscopy.shoola.agents.metadata.TabularDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.metadata;


import java.util.Arrays;

import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the tabular data.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class TabularDataLoader 
	extends EditorLoader
{

	/** Parameters to load the table. */
	private TableParameters parameters;

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /**
     * Flag indicating to load all annotations available or 
     * to only load the user's annotation.
     */
    private boolean loadAll;
    
    /**	
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param originalFileID The identifier of the table.
     * @param loadAll Pass <code>true</code> indicating to load all
     * 				  annotations available if the user can annotate,
     *                <code>false</code> to only load the user's annotation.
     */
    public TabularDataLoader(Editor viewer, SecurityContext ctx,
    		long originalFileID, boolean loadAll)
    {
    	super(viewer, ctx);
    	if (originalFileID < 0)
    		throw new IllegalArgumentException("No file to retrieve.");
    	parameters = new TableParameters(Arrays.asList(originalFileID));
    	this.loadAll = loadAll;
    }
    
    /** 
	 * Loads the tags. 
	 * @see EditorLoader#cancel()
	 */
	public void load()
	{
		long userID = getCurrentUser();
		if (loadAll) userID = -1;
		handle = mhView.loadTabularData(ctx, parameters, userID, this);
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see EditorLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }
	
	/**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	//if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
    	//viewer.setExistingTags((Collection) result);
    	//decide what to do with result
    } 

}
