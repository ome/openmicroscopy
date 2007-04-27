/*
 * org.openmicroscopy.shoola.agents.util.archived.ArchivedLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.archived;


//Java imports
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.archived.view.Downloader;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the archived files linked to the specified set of pixels.
 * This class calls the <code>loadArchivedFiles</code> method in the
 * <code>DataHandlerView</code>.
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
public class ArchivedFilesLoader 
	extends DownloaderLoader
{

	/** The pixels set ID. */
	private long			pixelsID;
	
	/** The location where to save the file. */
	private String			location;
	
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  	handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The Downloader this data loader is for.
     * 					Mustn't be <code>null</code>.
     * @param location	The location where to save the file.
     * @param pixelsID	The pixels set ID.
     */
	public ArchivedFilesLoader(Downloader viewer, String location, 
								long pixelsID)
	{
		super(viewer);
		this.pixelsID = pixelsID;
		this.location = location;
	}
	
	/**
     * Retrieves the archived files.
     * @see DownloaderLoader#load()
     */
    public void load()
    {
    	handle = dhView.loadArchivedFiles(location, pixelsID, this);
    }
    
	/** 
     * Cancels the data loading.
     * @see DownloaderLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer.
     * @see DownloaderLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Downloader.DISCARDED) return;  //Async cancel.
        viewer.setArchivedFiles((Map) result);
    }
    
}
