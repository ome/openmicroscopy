/*
 * org.openmicroscopy.shoola.agents.metadata.ThumbnailLoader 
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

import java.util.List;
import java.util.Set;

import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ThumbnailProvider;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.metadata.util.ThumbnailView;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import org.openmicroscopy.shoola.env.log.LogMessage;

import pojos.ImageData;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
 * @since OME3.0
 */
public class ThumbnailLoader 
	extends DSCallAdapter
{

	/** The viewer this data loader is for. */
    protected final ThumbnailView		viewer;
    
	/** Convenience reference for subclasses. */
    protected final Registry        	registry;
    
    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView	mhView;
    
    private ImageData					image;
    
    private Set<Long>					userIDs;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  				handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param image		The id of the image.
     * @param userIDs	The node of reference. Mustn't be <code>null</code>.
     */
    public ThumbnailLoader(ThumbnailView viewer, ImageData image, 
    						Set<Long> userIDs)
    {
    	 if (viewer == null) throw new NullPointerException("No viewer.");
         this.viewer = viewer;
         registry = MetadataViewerAgent.getRegistry();
         mhView = (MetadataHandlerView) 
         	registry.getDataServicesView(MetadataHandlerView.class);
         this.image = image;
         this.userIDs = userIDs;
    }
    
    /**
     * Does nothing as the async call returns <code>null</code>.
     * The actual payload (thumbnails) is delivered progressively
     * during the updates.
     */
    public void handleNullResult() {}
    
    /** Notifies the user that the data retrieval has been cancelled. */
    public void handleCancellation() 
    {
        String info = "The thumbnails retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
    	String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
    }
    
    /**
     * Retrieves the thumbnails.
     * @see DSCallAdapter#load()
     */
    public void load()
    {
        handle = mhView.loadThumbnails(image, userIDs, 
                                ThumbnailView.THUMB_MAX_WIDTH,
                                ThumbnailView.THUMB_MAX_HEIGHT, this);
    }
    
    /** 
     * Cancels the data loading. 
     * @see DSCallAdapter#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see DSCallAdapter#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        //if (viewer.getState() == HiViewer.DISCARDED) return;  //Async cancel.
        
        ThumbnailData td = (ThumbnailData) fe.getPartialResult();
        if (td != null)  //Last fe has null object.
            viewer.setThumbnail(td.getThumbnail(), td.getImageID(), 
            					td.getUserID());
    }
    
}
