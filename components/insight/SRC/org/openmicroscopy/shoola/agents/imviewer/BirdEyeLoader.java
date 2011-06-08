/*
 * org.openmicroscopy.shoola.agents.imviewer.BirdEyeLoader 
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
package org.openmicroscopy.shoola.agents.imviewer;

//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import pojos.ImageData;

/** 
 * Loads the image for the bird eye view.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class BirdEyeLoader 
	extends DataLoader
{
	
	/** The maximum size for the bird eye view.*/
	public static final int 	BIRD_EYE_SIZE = 128;
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /** The object the image is for. */
    private ImageData	image;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The view this loader is for.
     * 					Mustn't be <code>null</code>.
     * @param image	  	The image to handle.
     */
	public BirdEyeLoader(ImViewer viewer, ImageData image)
	{
		super(viewer);
		if (image == null)
			throw new IllegalArgumentException("No image to load.");
		this.image = image;
	}
	
	/**
     * Loads the image.
     * @see DataLoader#load()
     */
    public void load()
    {
    	Set<Long> ids = new HashSet<Long>();
    	ids.add(ImViewerAgent.getUserDetails().getId());
    	handle = mhView.loadThumbnails(image, ids, Factory.THUMB_DEFAULT_WIDTH,
    			Factory.THUMB_DEFAULT_HEIGHT, this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual pay-load (tile) is delivered progressively
     * during the updates.
     * @see DataLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataBrowserLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Bird Eye Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError(s, s, exc);
    }
    
    /** 
     * Feeds the tiles back to the viewer, as they arrive. 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
        ThumbnailData td = (ThumbnailData) fe.getPartialResult();
        if (td != null)
        	viewer.setBirdEyeView(td.getThumbnail());
    }

}
