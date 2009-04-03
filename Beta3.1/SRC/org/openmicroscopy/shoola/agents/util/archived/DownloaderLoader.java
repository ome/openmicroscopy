/*
 * org.openmicroscopy.shoola.agents.util.archived.DownloaderLoader 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.archived.view.Downloader;
import org.openmicroscopy.shoola.agents.util.archived.view.DownloaderFactory;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;
import org.openmicroscopy.shoola.env.log.LogMessage;

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
public abstract class DownloaderLoader 
	extends DSCallAdapter
{

	/** The Downloader this data loader is for. */
    protected final Downloader			viewer;
    
	 /** Convenience reference for subclasses. */
    protected final Registry            registry;
    
    /** Convenience reference for subclasses. */
    protected final DataHandlerView		dhView;

    /**
     * Creates a new instance.
     * 
     * @param viewer The Downloader this data loader is for.
     *               Mustn't be <code>null</code>.
     */
    protected DownloaderLoader(Downloader viewer)
    {
        if (viewer == null) throw new NullPointerException("No viewer.");
        this.viewer = viewer;
        registry = DownloaderFactory.getRegistry();
        dhView = (DataHandlerView) 
        		registry.getDataServicesView(DataHandlerView.class);
    }
    
    /** Notifies the {@link #viewer} that the data retrieval is finished. */
    public void onEnd() {}
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    public void handleNullResult() 
    {
        handleException(new Exception("No data available."));
    }
    
    /** Notifies the user that the data retrieval has been cancelled. */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
        registry.getUserNotifier().notifyInfo("Data Retrieval Cancellation", 
                                              info);
    }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable) 
     */
    public void handleException(Throwable exc) 
    {
    	String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
        viewer.cancel();
    }
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel(); 
    
}
