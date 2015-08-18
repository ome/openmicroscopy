/*
 * org.openmicroscopy.shoola.agents.metadata.ROILoader 
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
package org.openmicroscopy.shoola.agents.metadata;

//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the ROI associated to the specified image.
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
public class ROILoader 
	extends EditorLoader
{

	/** The id of the image the ROIs are related to. */
	private long		imageID;
	
	/** The id of the user. */
	private long		userID;
	
	/** The index of the figure to create. */
	private int			index;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
   
    /**
     * Creates a new instance. 
     * 
     * @param viewer	The viewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param imageID	The id of the image the ROIs are related to.
     * @param userID	The id of the user.
     * @param index		The index of the figure to create.
     */
	public ROILoader(Editor viewer, SecurityContext ctx, long imageID,
			long userID, int index)
	{
		super(viewer, ctx);
		if (imageID < 0) 
			throw new IllegalArgumentException("No image specified.");
		this.imageID = imageID;
		this.userID = userID;
		this.index = index;
	}
	
	/**
     * Loads the ROI.
     * @see EditorLoader#load()
     */
    public void load()
    {
    	handle = imView.loadROIFromServer(ctx, imageID, userID, this);
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
    	//if (viewer.getState() == MeasurementViewer.DISCARDED) return;  //Async cancel.
    	viewer.setROI((Collection) result, imageID, index);
    }
    
}
