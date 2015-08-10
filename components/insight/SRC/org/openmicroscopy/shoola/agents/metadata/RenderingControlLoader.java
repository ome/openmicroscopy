/*
 * org.openmicroscopy.shoola.agents.metadata.RenderingControlLoader 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.RendererUnloadedEvent;
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.FSAccessException;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import omero.log.LogMessage;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;

/** 
 * Loads the rendering control proxy for the specified pixels' set.
 * This class calls the <code>loadRenderingControl</code> method in the
 * <code>ImageDataView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RenderingControlLoader 
	extends EditorLoader
{

	/** Indicates to load the rendering engine. */
	public static final int LOAD = ImageDataView.LOAD;
	
	/** Indicates to reload the rendering engine. */
	public static final int RELOAD = ImageDataView.RELOAD;
	
	/** Indicates to reload the rendering engine. */
	public static final int RESET = ImageDataView.RESET;
	
    /** The ID of the pixels set. */
    private long        pixelsID;
    
    /** One of the constants defined by this class. */
    private int			index;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /** 
     * Controls if the passed value is supported.
     * 
     * @param value The value to check.
     */
    private void checkIndex(int value)
    {
    	switch (value) {
			case LOAD:
			case RELOAD:
			case RESET:
				return;
			default:
				throw new IllegalArgumentException("Index not supported.");
		}
    }
    
    /**
     * Creates a new instance
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID  The id of the pixels set.
     * @param index		One of the constants defined by this class.
     */
    public RenderingControlLoader(Editor viewer, SecurityContext ctx,
    		long pixelsID, int index)
    {
        super(viewer, ctx);
        checkIndex(index);
        this.pixelsID = pixelsID;
        this.index = index;
    }

    /**
     * Retrieves the rendering control proxy for the selected pixels set.
     * @see EditorLoader#load()
     */
    public void load()
    {
        handle = imView.loadRenderingControl(ctx, pixelsID, index, this);
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
    	String msg = "The image could not be opened.\n" +
    	"The image is not a valid image.";
    	if (exc instanceof FSAccessException) {
    		FSAccessException fsa = (FSAccessException) exc;
    		if (fsa.getIndex() == FSAccessException.PYRAMID) {
    			msg = fsa.getMessage();
    		}
    	} else {
    		String s = "Data Retrieval Failure: ";
        	LogMessage log = new LogMessage();
        	log.print(s);
        	log.print(exc);
    		registry.getLogger().error(this, log);
    	}
    	if (registry.getAdminService().isConnected())
    		registry.getUserNotifier().notifyInfo("Loading Rendering data",
    				msg);
    	viewer.setRenderingControl(null);
    	registry.getEventBus().post(new RendererUnloadedEvent(pixelsID));
    }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        switch (index) {
			case LOAD:
			case RELOAD:
			case RESET:
				viewer.setRenderingControl((RenderingControl) result);
				break;
		}
    }
    
}
