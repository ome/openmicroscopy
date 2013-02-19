/*
 * org.openmicroscopy.shoola.env.ui.ArchivedLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;



//Java imports
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ImageData;

/** 
 * Loads the image.
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
public class ArchivedLoader 
	extends UserNotifierLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** The archived image to load. */
    private ImageData 				image;

    /** The file where to export the image. */
    private String					folderPath;
    
    /** Flag indicating that the export has been marked to be cancel.*/
    private boolean cancelled;
    
    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String, Throwable)
     */
    protected void onException(String message, Throwable ex)
    { 
    	activity.notifyError("Unable to download the image", 
				message, ex);
    }
    
	/**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param registry Convenience reference for subclasses.
     * @param ctx The security context.
     * @param image The image to export.
     * @param folderPat The location where to export the image.
     * @param activity The activity associated to this loader.
     */
	public ArchivedLoader(UserNotifier viewer,  Registry registry,
			SecurityContext ctx, ImageData image, String folderPath,
			ActivityComponent activity)
	{
		super(viewer, registry, ctx, activity);
		if (image == null)
			throw new IllegalArgumentException("Image not valid.");
		this.image = image;
		this.folderPath = folderPath;
	}
	
	/**
     * Downloads the archived image.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	handle = mhView.loadArchivedImage(ctx, image.getId(), folderPath, this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel()
    {
    	cancelled = true;
    	if (handle != null) handle.cancel();
    }
 
	
    /**
     * Notifies the user that no archived images were found.
     * @see UserNotifierLoader#handleNullResult()
     */
    public void handleNullResult()
    {
    	activity.endActivity(new ArrayList<File>());
    }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (result == null && !cancelled) handleNullResult();
    	else {
    		Map m = (Map) result;
    		List l = (List) m.get(Boolean.valueOf(false));
    		List files = (List) m.get(Boolean.valueOf(true));
    		if (l != null && l.size() > 0) {
    			if (!cancelled)
    			onException("Missing "+l.size()+" out of "+files.size()+" " +
    					"files composing the image", null);
    		} else {
    			if (cancelled) {
    				Iterator i = files.iterator();
    				while (i.hasNext()) {
						((File) i.next()).delete();
					}
    			} else {
    				activity.endActivity(files);
    			}
    		}
    	}
    }
    
}
