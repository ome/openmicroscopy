/*
 * org.openmicroscopy.shoola.agents.metadata.PlaneInfoLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the information for the planes.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class PlaneInfoLoader 
	extends EditorLoader
{

	/** The ID of the pixels set. */
    private long        pixelsID;
    
    /** The selected channel. */
    private int 		channel;
    
    /** The selected z-section or <code>-1</code>. */
    private int			defaultZ;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID  The id of pixels set.
     * @param defaultZ  The selected z-section.
     * @param channel	The selected channel.
     */
	public PlaneInfoLoader(Editor viewer, SecurityContext ctx, long pixelsID,
			int channel, int defaultZ)
	{
		super(viewer, ctx);
		this.pixelsID = pixelsID;
		this.channel = channel;
		this.defaultZ = defaultZ;
	}

    /**
     * Creates a new instance.
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID  The id of pixels set.
     */
	public PlaneInfoLoader(Editor viewer, SecurityContext ctx, long pixelsID)
	{
		this(viewer, ctx, pixelsID, -1, -1);
	}
	
    /**
     * Loads the plane information
     * @see EditorLoader#load()
     */
    public void load()
    {
    	handle = imView.loadPlaneInfo(ctx, pixelsID, defaultZ, -1, channel,
    			this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer. 
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        //if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        viewer.setPlaneInfo((Collection) result, pixelsID, channel);
    }

}
