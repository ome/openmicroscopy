/*
 * org.openmicroscopy.shoola.agents.util.archived.view.DownloaderModel 
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
package org.openmicroscopy.shoola.agents.util.archived.view;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.archived.ArchivedFilesLoader;
import org.openmicroscopy.shoola.agents.util.archived.DownloaderLoader;

/** 
 * 
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
class DownloaderModel
{

    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
	private DownloaderLoader	currentLoader;
	
	/** The pixels set ID. */
	private long				pixelsID;
	
    /** Holds one of the state flags defined by {@link Downloader}. */
    private int					state;
    
    /** Reference to the component that embeds this model. */
    protected Downloader		component;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param pixelsID	The pixels set ID.
	 */
	DownloaderModel(long pixelsID)
	{
		this.pixelsID = pixelsID;
		state = Downloader.NEW;
	}
	
	/**
     * Called by the <code>Downloader</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
	void initialize(Downloader component) { this.component = component; }
	
	/**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link Downloader} interface.  
     */
	int getState() { return state; }    
   
	/**
     * Sets the object in the {@link Downloader#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
	void discard()
	{
		cancel();
        state = Downloader.DISCARDED;
    }
   
	/**
	 * Sets the object in the {@link Downloader#READY} state.
	 * Any ongoing data loading will be cancelled.
	 */
	void cancel()
	{
       if (currentLoader != null) {
           currentLoader.cancel();
           currentLoader = null;
       }
       state = Downloader.READY;
    }
	
	/** 
	 * Downloads asynchronously the archived files.
	 * 
	 * @param location The location where to save the archived files.
	 */
	void fireDownloadArchived(String location)
	{
		currentLoader = new ArchivedFilesLoader(component, location, pixelsID);
		currentLoader.load();
		state = Downloader.LOADING;
	}

	/**
	 * Sets the state of the component.
	 * 
	 * @param state The value to set.
	 */
	void setState(int state) { this.state = state; }
	
}
