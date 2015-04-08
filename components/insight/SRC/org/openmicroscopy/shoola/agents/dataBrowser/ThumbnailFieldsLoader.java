/*
 * org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailFieldsLoader 
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
package org.openmicroscopy.shoola.agents.dataBrowser;


//Java imports
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;

/** 
 * Loads the thumbnails for the fields of a given well.
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
public class ThumbnailFieldsLoader
	extends DataBrowserLoader
{

	/** 
	 * The <code>DataObject</code> objects for the images whose thumbnails 
	 * have to be fetched.
	 */
    private Collection<DataObject> images;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /** The row identifying the well. */
    private int row;
    
    /** The column identifying the well. */
    private int column;
    
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
     * @param row	 The row identifying the well.
     * @param column The column identifying the well.
     */
    public ThumbnailFieldsLoader(DataBrowser viewer, SecurityContext ctx,
    				Collection<DataObject> images, int row, int column)
    {
        super(viewer, ctx);
        if (images == null)
            throw new IllegalArgumentException("Collection shouldn't be null.");
        this.images = images;
        this.row = row;
        this.column = column;
    }
    
    /**
     * Retrieves the thumbnails.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	long userID = DataBrowserAgent.getUserDetails().getId();
    	handle = hiBrwView.loadThumbnails(ctx, images, 
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
    		if (result == null) result = new ArrayList<Object>();
        	result.add(td);
    		if (result.size() == images.size())
    			viewer.setThumbnailsFieldsFor(result, row, column);
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
