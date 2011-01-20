/*
 * org.openmicroscopy.shoola.env.ui.FileDownloader 
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
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.views.CallHandle;


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
class FileLoader 
	extends UserNotifierLoader
{

	/** The id of the file to download. */
	private long 		fileID;
	
	/** The absolute of the new file. */
	private File 		file;
	
	/** The size of the file. */
	private long		size;
	
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /** Reference to the activity. */
    private ActivityComponent 		activity;
    
    /** Notifies the user that an error occurred. */
    protected void onException() { handleNullResult(); }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer Reference to the parent.
     * @param reg    Reference to the registry.
     * @param path	 The absolute path to the file.
     * @param fileID The file ID.
     * @param size   The size of the file.
     * @param activity 	The activity associated to this loader.
     */
	FileLoader(UserNotifier viewer, Registry reg, String path, long fileID, 
			long size, ActivityComponent activity)
	{
		super(viewer, reg);
		this.activity = activity;
		file = new File(path);
		this.fileID = fileID;
		this.size = size;
	}
	
	/** 
	 * Downloads the file. 
	 * @see UserNotifierLoader#cancel()
	 */
	public void load()
	{
		handle = mhView.loadFile(file, fileID, size, this);
	}
    
	/** 
	 * Cancels the data loading. 
	 * @see UserNotifierLoader#cancel()
	 */
	public void cancel()
	{ 
		handle.cancel();
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
        //viewer.setLoadingStatus(-1, fileID, file.getAbsolutePath());
    }
    
    /**
     * Notifies the user that it wasn't possible to download the file.
     * @see UserNotifierLoader#handleNullResult()
     */
    public void handleNullResult()
    { 
    	activity.notifyError("Unable to download the file");
    }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result) { activity.endActivity(); }
    
}
