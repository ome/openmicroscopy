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



//Java imports
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.editor.Editor;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ImageData;

/** 
 * Loads the thumbnails, one per specified user for a given set of 
 * pixels. This class calls the <code>loadThumbnails</code> method in the
 * <code>MetadataHandlerView</code>.
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
	extends EditorLoader
{

	/** The standard width of the thumbnail. */
    private static final int            STANDARD_WIDTH = 96; 
    
    /** The standard height of the thumbnail. */
    private static final int            STANDARD_HEIGHT = 96;
    
    /** The width of the thumbnail. */
    private static final int            THUMB_MAX_WIDTH = 200; 
    
    /** The maximum height of the thumbnail. */
    private static final int            THUMB_MAX_HEIGHT = 200;
  
    /** The object the thumbnails are for. */
    private ImageData					image;
    
    /** Collection of users who viewed the image. */
    private Set<Long>					userIDs;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  				handle;
    
    /** The collection of thumbnails. */
    private Map<Long, BufferedImage>	thumbnails;
    
    /** Flag indicating to retrieve on thumbnail only. */
    private boolean						single;
    
    /**	
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param image		The image.
     * @param userIDs	The node of reference. Mustn't be <code>null</code>.
     */
    public ThumbnailLoader(Editor viewer, ImageData image, 
    						Set<Long> userIDs)
    {
    	 this(viewer, image, userIDs, false);
    }

    /**	
     * Creates a new instance.
     * 
     * @param viewer 	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param image		The image.
     * @param userIDs	The node of reference. Mustn't be <code>null</code>.
     * @param single	Pass <code>true</code> to indicate that we retrieve 
     * 					a single thumbnail, <code>false</code> otherwise.
     */
    public ThumbnailLoader(Editor viewer, ImageData image, 
    						Set<Long> userIDs, boolean single)
    {
    	 super(viewer);
         this.image = image;
         this.userIDs = userIDs;
         thumbnails = new HashMap<Long, BufferedImage>();
         this.single = single;
    }
    
    /**
     * Retrieves the thumbnails.
     * @see EditorLoader#load()
     */
    public void load()
    {
    	if (single) 
    		handle = mhView.loadThumbnails(image, userIDs, THUMB_MAX_WIDTH,
                	THUMB_MAX_HEIGHT, this);
    	else
    		handle = mhView.loadThumbnails(image, userIDs, STANDARD_WIDTH,
                	STANDARD_HEIGHT, this);	
    }
    
    /** 
     * Cancels the data loading. 
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see EditorLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        ThumbnailData td = (ThumbnailData) fe.getPartialResult();
        if (single) {
        } else {
        	if (td != null)  {
            	//Last fe has null object.
            	thumbnails.put(td.getUserID(), td.getThumbnail());
            } 
            if (thumbnails.size() == userIDs.size())
            	viewer.setThumbnails(thumbnails, image.getId());
        }
    }
    
    /**
     * Does nothing as the async call returns <code>null</code>.
     * The actual payload (thumbnails) is delivered progressively
     * during the updates.
     * @see EditorLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
}
