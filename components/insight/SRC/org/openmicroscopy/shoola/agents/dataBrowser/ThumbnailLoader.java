/*
 * org.openmicroscopy.shoola.agents.dataBrowser.ThumbnailLoader 
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
package org.openmicroscopy.shoola.agents.dataBrowser;



//Java imports
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ImageData;

/** 
 * Loads all thumbnails for the specified images.
 * This class calls the <code>loadThumbnails</code> method in the
 * <code>HierarchyBrowsingView</code>.
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
	extends DataBrowserLoader
{

	/** The number of thumbnails to load. */
	private int                     max;
	
	/** 
	 * The <code>ImageData</code> objects for the images whose thumbnails 
	 * have to be fetched.
	 */
    private Collection<ImageData>	images;
    
    /** Flag indicating to retrieve thumbnail. */
    private boolean					thumbnail;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle 	 			handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param images 	The <code>ImageData</code> objects for the images whose 
     *               	thumbnails have to be fetched. 
     * 					Mustn't be <code>null</code>.
     */
    public ThumbnailLoader(DataBrowser viewer, Collection<ImageData> images)
    {
        this(viewer, images, true);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param images 	The <code>ImageData</code> objects for the images whose 
     *               	thumbnails have to be fetched. 
     * 					Mustn't be <code>null</code>.
     * @param thumbnail	Pass <code>true</code> to retrieve image at a thumbnail
     * 					size, <code>false</code> otherwise.
     */
    public ThumbnailLoader(DataBrowser viewer, Collection<ImageData> images, 
    		              boolean thumbnail)
    {
        super(viewer);
        if (images == null)
            throw new IllegalArgumentException("Collection shouldn't be null.");
        this.images = images;
        this.thumbnail = thumbnail;
        max = images.size();
    }
    
    /**
     * Retrieves the thumbnails.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	long userID = DataBrowserAgent.getUserDetails().getId();
    	if (thumbnail) 
    		handle = hiBrwView.loadThumbnails(images, 
                    ThumbnailProvider.THUMB_MAX_WIDTH,
                    ThumbnailProvider.THUMB_MAX_HEIGHT,
                    userID, this);
    	else 
    		handle = hiBrwView.loadThumbnails(images, 
                    3*ThumbnailProvider.THUMB_MAX_WIDTH,
                    3*ThumbnailProvider.THUMB_MAX_HEIGHT,
                    userID, this);
    		//handle = hiBrwView.loadImagesAsThumbnails(images, userID, this);
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
        String status = fe.getStatus();
        int percDone = fe.getPercentDone();
        if (thumbnail) {
        	if (status == null) 
                status = (percDone == 100) ? "Done" :  //Else
                                           ""; //Description wasn't available.   
            viewer.setStatus(status, percDone);
            List l = (List) fe.getPartialResult();
            if (l != null) {
            	Iterator i = l.iterator();
            	ThumbnailData td;
            	while (i.hasNext()) {
            		td = (ThumbnailData) i.next();
            		viewer.setThumbnail(td.getImageID(), td.getThumbnail(), 
            				td.isValidImage(), max);
				}
            }
        } else {
        	if (status == null) 
        		status = (percDone == 100) ? "Done" :  //Else
        			""; //Description wasn't available.   
        	/*
        	viewer.setSlideViewStatus(status, percDone);
            ThumbnailData td = (ThumbnailData) fe.getPartialResult();
            if (td != null)  //Last fe has null object.
                viewer.setSlideViewImage(td.getImageID(), td.getThumbnail());
            */
        	viewer.setSlideViewStatus(status, percDone);
        	List l = (List) fe.getPartialResult();
            if (l != null) {
            	Iterator i = l.iterator();
            	ThumbnailData td;
            	while (i.hasNext()) {
            		td = (ThumbnailData) i.next();
            		viewer.setSlideViewImage(td.getImageID(), td.getThumbnail());
				}
            }
        }
    }
    
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual payload (thumbnails) is delivered progressively
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
