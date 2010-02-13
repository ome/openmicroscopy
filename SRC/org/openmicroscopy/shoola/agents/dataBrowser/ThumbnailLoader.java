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
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FileData;

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

	/** 
	 * Indicates that the thumbnails are associated to an 
	 * <code>ImageData</code>.
	 */
	private static final int 		IMAGE = 0;
	
	/** 
	 * Indicates that the thumbnails are associated to an 
	 * <code>ExperimenterData</code>.
	 */
	private static final int 		EXPERIMENTER = 1;
	
	/** 
	 * Indicates that the thumbnails are associated to an <code>FileData</code>.
	 */
	private static final int 		FS_FILE = 2;

	
	/** The number of thumbnails to load. */
	private int                     max;
	
	/** 
	 * The <code>ImageData</code> objects for the images whose thumbnails 
	 * have to be fetched.
	 */
    private Collection<DataObject>	objects;
    
    /**  
     * Indicates the types of thumbnails to retrieve. One of the constants
     * defined by this class.
     */
    private int						type;
    
    /** Flag indicating to retrieve thumbnail. */
    private boolean					thumbnail;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle 	 			handle;

    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param objects 	The <code>DataObject</code>s associated to the images
     *					to fetch. Mustn't be <code>null</code>.
     */
    public ThumbnailLoader(DataBrowser viewer, Collection<DataObject> objects)
    {
        this(viewer, objects, true);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param objects 	The <code>DataObject</code>s associated to the images
     *					to fetch. Mustn't be <code>null</code>.
     * @param thumbnail	Pass <code>true</code> to retrieve image at a thumbnail
     * 					size, <code>false</code> otherwise.
     */
    public ThumbnailLoader(DataBrowser viewer, Collection<DataObject> objects, 
    		              boolean thumbnail)
    {
        super(viewer);
        if (objects == null)
            throw new IllegalArgumentException("Collection shouldn't be null.");
        DataObject o = null;
        Iterator<DataObject> i = objects.iterator();
        while (i.hasNext()) {
        	if (o != null) break;
			o = i.next();
		}
        this.objects = objects;
        this.thumbnail = thumbnail;
        max = objects.size();
        type = IMAGE;
        if (o instanceof ExperimenterData) type = EXPERIMENTER;
        else if (o instanceof FileData) type = FS_FILE;
    }
    
    /**
     * Retrieves the thumbnails.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	long userID = DataBrowserAgent.getUserDetails().getId();
    	if (thumbnail) 
    		handle = hiBrwView.loadThumbnails(objects, 
                    ThumbnailProvider.THUMB_MAX_WIDTH,
                    ThumbnailProvider.THUMB_MAX_HEIGHT,
                    userID, this);
    	else 
    		handle = hiBrwView.loadThumbnails(objects, 
                    3*ThumbnailProvider.THUMB_MAX_WIDTH,
                    3*ThumbnailProvider.THUMB_MAX_HEIGHT,
                    userID, this);
    	
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
            	Object ref;
            	while (i.hasNext()) {
            		td = (ThumbnailData) i.next();
            		ref = td.getRefObject();
            		if (ref == null) ref = td.getImageID();
            		viewer.setThumbnail(ref, td.getThumbnail(), 
            				td.isValidImage(), max);
				}
            }
        } else {
        	if (status == null) 
        		status = (percDone == 100) ? "Done" :  //Else
        			""; //Description wasn't available.   
        	viewer.setSlideViewStatus(status, percDone);
        	List l = (List) fe.getPartialResult();
            if (l != null) {
            	Iterator i = l.iterator();
            	ThumbnailData td;
            	while (i.hasNext()) {
            		td = (ThumbnailData) i.next();
            		viewer.setSlideViewImage(td.getImageID(), 
            				td.getThumbnail());
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
