/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;

import omero.gateway.SecurityContext;

import org.openmicroscopy.shoola.env.data.views.CallHandle;

import com.google.common.collect.Multimap;

import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;

/** 
 * Loads the thumbnails for the fields of a given well.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ThumbnailFieldsLoader
	extends DataBrowserLoader
{

	/** 
	 * The <code>DataObject</code> objects for the images whose thumbnails 
	 * have to be fetched.
	 */
    private Multimap<Point, ImageData> images;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
	/** The loaded thumbnails.*/
	private List<Object> result;
	
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param images The <code>ImageData</code> objects for the images whose 
     *               thumbnails have to be fetched. 
     *               Mustn't be <code>null</code>.
     */
    public ThumbnailFieldsLoader(DataBrowser viewer, SecurityContext ctx,
            Multimap<Point, ImageData> images)
    {
        super(viewer, ctx);
        if (images == null)
            throw new IllegalArgumentException("Collection shouldn't be null.");
        this.images = images;
    }
    
    /**
     * Retrieves the thumbnails.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	long userID = DataBrowserAgent.getUserDetails().getId();
    	
    	Collection<DataObject> imgs = new ArrayList<DataObject>();
    	for(ImageData i : images.values()) 
    	    imgs.add(i);
    	
    	handle = hiBrwView.loadThumbnails(ctx, imgs, 
                ThumbnailProvider.THUMB_MAX_WIDTH,
                ThumbnailProvider.THUMB_MAX_HEIGHT,
                userID, ThumbnailLoader.IMAGE, this);
    }
    
    /** 
     * Cancels the data loading. 
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see DataBrowserLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == DataBrowser.DISCARDED) return;  //Async cancel.
        ThumbnailData td = (ThumbnailData) fe.getPartialResult();
    	if (td != null) {
    		if (result == null) 
    		    result = new ArrayList<Object>();
        	result.add(td);
        	
        	boolean complete = result.size() == images.values().size();
        	
        	Point well = null;
        	for(Point p : images.keys()) {
        	    for(ImageData img : images.get(p)) {
        	        if(img.getId() == td.getImageID()) {
        	            well = p;
        	            break;
        	        }
        	    }
        	}
        	
        	viewer.updateThumbnailsFields(well, td, complete);
    	}
    }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual pay-load (thumbnails) is delivered progressively
     * during the updates.
     * @see DataBrowserLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataBrowserLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Thumbnail Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("Thumbnail Retrieval Failure", 
                                               s, exc);
    }
    
}
