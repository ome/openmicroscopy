/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee & Open Microscopy Environment.
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
package org.openmicroscopy.shoola.agents.metadata;

import java.util.Set;

import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.FilesetData;

/** 
 * Loads the file set associated to the specified image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 4.4
 */
public class FilesetLoader
	extends EditorLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The id of the image.*/
    private long imageId;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     * Mustn't be <code>null</code>.
     * @param ctx The security context.
	 * @param imageId The id of the image.
     */
    public FilesetLoader(Editor viewer, SecurityContext ctx,
    		long imageId)
    {
    	super(viewer, ctx);
    	this.imageId = imageId;
    }
    
    /** 
	 * Cancels the data loading.
	 * @see EditorLoader#cancel()
	 */
	public void cancel()
	{
		handle.cancel();
	}

	/** 
	 * Loads the file set.
	 * @see EditorLoader#load()
	 */
	public void load()
	{
		handle = mhView.loadFileset(ctx, imageId, this);
	}
	
    /**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (result == null) return;
    	viewer.setFileset((Set<FilesetData>) result);
    }

}
