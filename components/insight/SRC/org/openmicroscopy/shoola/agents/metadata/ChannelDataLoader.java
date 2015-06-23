/*
 * org.openmicroscopy.shoola.agents.metadata.ChannelDataLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
import java.util.HashMap;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the channels information related to a given set of pixels.
 * This class calls one of the <code>loadChannelsData</code> methods in the
 * <code>DataManagerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ChannelDataLoader 
	extends EditorLoader
{

    /** The id of the pixels set. */
    private long pixelsID;
    
    /** The id of the user. */
    private long  userID;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the viewer. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     * @param userID The id of the user.
     */
	public ChannelDataLoader(Editor viewer, SecurityContext ctx, long pixelsID,
			long userID)
	{
		super(viewer, ctx);
		this.pixelsID = pixelsID;
		this.userID = userID;
	}
	
    /** 
     * Retrieves the channels for the specified pixels set.
     * @see EditorLoader#load()
     */
    public void load()
    {
        handle = dmView.loadChannelsData(ctx, pixelsID, userID, this);
    }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    public void handleNullResult() 
    {
    	viewer.setChannelsData(new HashMap(), true);
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
        viewer.setChannelsData((Map) result, true);
    }

}
