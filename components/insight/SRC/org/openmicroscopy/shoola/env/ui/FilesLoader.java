/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmicroscopy.shoola.agents.metadata.EditorLoader;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.FileAnnotationData;

/** 
 * Loads the files to zip.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class FilesLoader 
	extends UserNotifierLoader
{

	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle	handle;
    
    /** The files to load. */
    private Map<FileAnnotationData, File> files;
    
    /** The files loaded. */
    private List<File> results;
    
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
     * @param viewer 	Reference to the parent.
     * @param reg    	Reference to the registry.
     * @param ctx The security context.
     * @param file	 	The absolute path to the file.
     * @param fileID 	The file ID.
     * @param size   	The size of the file.
     * @param toLoad 	Indicates to download the file.
     * @param activity 	The activity associated to this loader.
     */
	FilesLoader(UserNotifier viewer, Registry reg, SecurityContext ctx, 
			Map<FileAnnotationData, File> files, ActivityComponent activity)
	{
		super(viewer, reg, ctx, activity);
		if (files == null || files.size() == 0)
			throw new IllegalArgumentException("No files to download");
		this.files = files;
		results = new ArrayList<File>();
	}
	
	/** 
	 * Downloads the file. 
	 * @see UserNotifierLoader#cancel()
	 */
	public void load()
	{
		handle = mhView.loadFiles(ctx, true, files, this);
	}
    
	/** 
	 * Cancels the data loading. 
	 * @see UserNotifierLoader#cancel()
	 */
	public void cancel()
	{ 
		if (handle != null) handle.cancel();
		Iterator<File> i = files.values().iterator();
		while (i.hasNext()) {
			i.next().delete();
		}
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
	 * Feeds the file back to the viewer, as they arrive.
	 * @see EditorLoader#update(DSCallFeedbackEvent)
	 */
	public void update(DSCallFeedbackEvent fe) 
	{
		Map m = (Map) fe.getPartialResult();
		if (m != null) {
			Entry entry;
			Iterator i = m.entrySet().iterator();
			FileAnnotationData fa;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				fa = (FileAnnotationData) entry.getKey();
				results.add((File) entry.getValue());
			}
			if (results.size() == files.size() && activity != null) {
				activity.endActivity(results); 
			}
		}
	}
	
    /**
     * Does nothing as the asynchronous call returns <code>null</code>.
     * The actual payload is delivered progressively during the updates
     * if data is <code>null</code>.
     * @see EditorLoader#handleNullResult()
     */
    public void handleNullResult() {}
    
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    { 
    	if (result == null) onException(MESSAGE_RESULT, null);
    	else {
    		if (activity != null) activity.endActivity(result); 
    	}
    }
    
}
