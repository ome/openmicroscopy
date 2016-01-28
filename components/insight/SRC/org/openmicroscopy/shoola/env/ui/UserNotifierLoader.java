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

import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import omero.log.LogMessage;

/** 
 * Class that each loader should extend. 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
abstract class UserNotifierLoader
	extends DSCallAdapter
{

	/** Message indicating that no results were returned. */
	static final String MESSAGE_RESULT = "No result returned.";
	
	/** Message indicating that no results were returned. */
	static final String MESSAGE_RUN = "Unable to start the script.";
	
	/** Convenience reference for subclasses. */
    protected final Registry			registry;
    
    /** Convenience reference for subclasses. */
    protected final UserNotifier		viewer;
    
    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView	mhView;
    
    /** Convenience reference for subclasses. */
    protected final ImageDataView		ivView;
    
    /** Convenience reference for subclasses. */
    protected final DataHandlerView		dhView;
    
    /** Convenience reference for subclasses. */
    protected final DataManagerView		dmView;
    
    /** Convenience reference to the activity. */
    protected final ActivityComponent 	activity;
    
    /** The security context.*/
    protected final SecurityContext ctx;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *                Mustn't be <code>null</code>.
     * @param registry Convenience reference for subclasses.
     * @param ctx The security context.
     * @param activity Convenience reference to the activity.
     */
    UserNotifierLoader(UserNotifier viewer, Registry registry,
		SecurityContext ctx, ActivityComponent activity)
    {
    	if (viewer == null) throw new NullPointerException("No viewer.");
    	if (registry == null) throw new NullPointerException("No registry.");
    	this.activity = activity;
    	this.viewer = viewer;
    	this.ctx = ctx;
    	this.registry = registry;
    	mhView = (MetadataHandlerView) 
     		registry.getDataServicesView(MetadataHandlerView.class);
    	ivView = (ImageDataView) 
 			registry.getDataServicesView(ImageDataView.class);
    	dhView = (DataHandlerView) 
			registry.getDataServicesView(DataHandlerView.class);
    	dmView = (DataManagerView) 
		registry.getDataServicesView(DataManagerView.class);
    }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    /*
    public void handleNullResult() 
    {
        handleException(new Exception("No data available."));
    }
    */
    
    /** Notifies the user that the data retrieval has been canceled. */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
        if (activity != null) activity.onActivityCancelled();
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
        onException(exc.getMessage(), exc);
    }
    
    /** Subclasses should override this method.
     * 
     * @param message The message to display.
     * @param ex The exception to handle.
     */
    protected void onException(String message, Throwable ex) {};
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel(); 
    
}
