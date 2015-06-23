/*
 * org.openmicroscopy.shoola.agents.metadata.RenderingControlShutDown 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee & Open Microscopy Environment.
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



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import omero.log.LogMessage;

/** 
 * Shuts down the rendering control.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class RenderingControlShutDown
	extends DSCallAdapter
{

	/** Convenience reference for subclasses. */
    private final Registry registry;
    
    /** Convenience reference for subclasses. */
    private final ImageDataView imView;
    
	 /** The ID of the pixels set. */
    private long pixelsID;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The security context.*/
    private SecurityContext ctx;
    
    /**
     * Creates a new instance.
     * 
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     */
    public RenderingControlShutDown(SecurityContext ctx, long pixelsID)
    {
    	this.pixelsID = pixelsID;
    	this.ctx = ctx;
    	registry = MetadataViewerAgent.getRegistry();
    	imView = (ImageDataView) 
    	registry.getDataServicesView(ImageDataView.class);
    }
    
    /**
     * Retrieves the rendering control proxy for the selected pixels set.
     * @see EditorLoader#load()
     */
    public void load()
    {
        handle = imView.shutDownRenderingControl(ctx, pixelsID, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc)
    {
    	String s = "Problem closing rendering engine: ";
    	LogMessage log = new LogMessage();
    	log.print(s);
    	log.print(exc);
		registry.getLogger().error(this, log);
    }

}
