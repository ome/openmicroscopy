/*
 * org.openmicroscopy.shoola.env.ui.UserNotifierLoader 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import org.openmicroscopy.shoola.env.log.LogMessage;

/** 
 * Class that each loader should extend. 
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
abstract class UserNotifierLoader
	extends DSCallAdapter
{

	/** Convenience reference for subclasses. */
    protected final Registry			registry;
    
    /** Convenience reference for subclasses. */
    protected final UserNotifier		viewer;
    
    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView	mhView;
    
    /** Convenience reference for subclasses. */
    protected final ImageDataView		ivView;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     */
    UserNotifierLoader(UserNotifier viewer,  Registry registry)
    {
    	if (viewer == null) throw new NullPointerException("No viewer.");
    	if (registry == null) throw new NullPointerException("No registry.");
    	this.viewer = viewer;
    	this.registry = registry;
    	mhView = (MetadataHandlerView) 
     		registry.getDataServicesView(MetadataHandlerView.class);
    	ivView = (ImageDataView) 
 			registry.getDataServicesView(ImageDataView.class);
    }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    public void handleNullResult() 
    {
        handleException(new Exception("No data available."));
    }
    
    /** Notifies the user that the data retrieval has been canceled. */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
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
        exc.printStackTrace();
        //registry.getUserNotifier().notifyError("Data Loading Failure", 
        //                                      s, exc);
        onException();
    }
    
    /** Subclasses should override this method. */
    protected void onException() {};
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel(); 
    
}
