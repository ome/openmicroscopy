/*
 * org.openmicroscopy.shoola.env.ui.FileLoader 
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies
import omero.cmd.OriginalMetadataResponse;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.RequestCallback;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.util.OriginalMetadataParser;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;


/** 
 * Hosts information about the file to load i.e. absolute path, size, etc.
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
public class FileLoader 
	extends UserNotifierLoader
{

	/** Indicates to load the original file if original file is not set. */
	public static final int ORIGINAL_FILE = MetadataHandlerView.ORIGINAL_FILE;
	
	/** Indicates to load the file annotation if original file is not set. */
	public static final int FILE_ANNOTATION = 
		MetadataHandlerView.FILE_ANNOTATION;
	
	/** Indicates to load the metadata from the image ID.*/
	public static final int METADATA_FROM_IMAGE = 
			MetadataHandlerView.METADATA_FROM_IMAGE;
	
	/** The id of the object to handle. */
	private long id;
	
	/** The absolute of the new file. */
	private File file;

	/** Pass <code>true</code> to load, <code>false</code> otherwise. */
	private boolean toLoad;
	
	/** One of the constants defined by this class. */
	private int index;
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;

    /**
     * Notifies that an error occurred.
     * @see UserNotifierLoader#onException(String, Throwable)
     */
    protected void onException(String message, Throwable ex)
    { 
    	activity.notifyError("Unable to download the file", message, ex);
    }
	
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the parent.
     * @param reg Reference to the registry.
     * @param ctx The security context.
     * @param file The absolute path to the file.
     * @param id The file ID.
     * @param toLoad Indicates to download the file.
     * @param activity The activity associated to this loader.
     */
	FileLoader(UserNotifier viewer, Registry reg, SecurityContext ctx,
		File file, long id, boolean toLoad, ActivityComponent activity)
	{
		super(viewer, reg, ctx, activity);
		this.file = file;
		this.id = id;
		this.toLoad = toLoad;
		index = -1;
	}
	
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the parent.
     * @param reg Reference to the registry.
     * @param file The absolute path to the file.
     * @param id The id of the file to handle.
     * @param index One of the constants defined by this class.
     * @param toLoad Indicates to download the file.
     * @param activity The activity associated to this loader.
     */
	FileLoader(UserNotifier viewer, Registry reg, SecurityContext ctx,
		File file, long fileID, int index, boolean toLoad,
		ActivityComponent activity)
	{
		super(viewer, reg, ctx, activity);
		this.file = file;
		this.id = fileID;
		this.toLoad = toLoad;
		this.index = index;
	}
	
	/** 
	 * Downloads the file.
	 * @see UserNotifierLoader#load()
	 */
	public void load()
	{
		if (toLoad) {
			switch (index) {
				case ORIGINAL_FILE:
				case FILE_ANNOTATION:
				case METADATA_FROM_IMAGE:
					handle = mhView.loadFile(ctx, file, id, index, this);
					break;
				default:
					handle = mhView.loadFile(ctx, file, id, this);
			}
		} else handleResult(file);
	}
    
	/** 
	 * Cancels the data loading.
	 * @see UserNotifierLoader#cancel()
	 */
	public void cancel()
	{ 
		if (handle != null) handle.cancel();
		file.delete();
	}
	
	/** 
	 * Notifies the user that the data retrieval has been canceled.
	 * @see UserNotifierLoader#handleResult(Object)
	 */
    public void handleCancellation()
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }

    /**
     * Notifies the user that it wasn't possible to retrieve the file.
     * @see UserNotifierLoader#handleNullResult()
     */
    public void handleNullResult()
    {
    	if (index != METADATA_FROM_IMAGE && activity != null)
    		activity.notifyError("File no longer exists",
    				"The file you wish to download no longer exists.", null);
    }
    
    /** 
     * Sets the adapter.
     * @see UserNotifierLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe)
    {
    	Object o = fe.getPartialResult();
        if (o != null) {
        	if (o instanceof Boolean) {
        		handleNullResult();
        	} else {
        		RequestCallback callBack = (RequestCallback) o;
            	callBack.setAdapter(this);
        	}
        }
    }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
    	if (result == null) onException(MESSAGE_RESULT, null);
    	else {
    		if (index == METADATA_FROM_IMAGE) {
    			if (result instanceof Boolean) {
    	    		boolean b = ((Boolean) result).booleanValue();
    	    		if (!b) onException(MESSAGE_RESULT, null);
    			} else if (result instanceof OriginalMetadataResponse) {
    	    		OriginalMetadataParser parser =
    	    				new OriginalMetadataParser(file);
    	        	try {
    	        		parser.read((OriginalMetadataResponse) result, "=");
    	    		} catch (Exception e) {
    	    			onException(MESSAGE_RESULT, null);
    	    			return;
    	    		}
    	    	}
    		}
    		if (activity != null) activity.endActivity(result);
    	}
    }
    
}
