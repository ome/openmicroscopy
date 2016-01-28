/*
 * org.openmicroscopy.shoola.agents.metadata.MetadataLoader 
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.TreeBrowserDisplay;
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.AdminView;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import omero.log.LogMessage;

/** 
 * Parent of all classes that load data asynchronously for a 
 * {@link MetadataViewer}.
 * All these classes invoke methods of the {@link MetadataHandlerView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call. This class provides
 * default implementations of some of the callbacks to notify the 
 * {@link MetadataViewer} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link MetadataViewer} back with the results.
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
public abstract class MetadataLoader
	extends DSCallAdapter
{

	/** The node of reference i.e. where to feed back the results. */
	protected final TreeBrowserDisplay refNode;
	
	/** The viewer this data loader is for. */
    protected final MetadataViewer viewer;
    
	/** Convenience reference for subclasses. */
    protected final Registry registry;
    
    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView mhView;
    
    /** Convenience reference for subclasses. */
    protected final DataManagerView dmView;
    
    /** Convenience reference for subclasses. */
    protected final ImageDataView ivView;
    
    /** Convenience reference for subclasses. */
    protected final AdminView adminView;
    
    /** The security context.*/
    protected final SecurityContext ctx;
    
    /** The identifier of the loader.*/
    protected int loaderID;
    
    /**
     * Logs the error and notifies the user depending on the specified flag.
     * 
     * @param exc The exception to handle.
     * @param notify Pass <code>true</code> to notify the user,
     * <code>false</code> otherwise.
     */
    protected void handleException(Throwable exc, boolean notify)
    {
    	int state = viewer.getState();
    	String s = "Data Retrieval Failure: ";
    	LogMessage msg = new LogMessage();
        msg.print("State: "+state);
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        if (notify && state != MetadataViewer.DISCARDED)
        	registry.getUserNotifier().notifyError("Data Retrieval Failure",
                                               s, exc);
        viewer.cancel(loaderID);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param loaderID The identifier of the loader.
     */
    public MetadataLoader(MetadataViewer viewer, SecurityContext ctx, int
    		loaderID)
    {
    	this(viewer, ctx, null, loaderID);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param refNode The node of reference. Mustn't be <code>null</code>.
     * @param loaderID The identifier of the loader.
     */
    public MetadataLoader(MetadataViewer viewer, SecurityContext ctx,
    		TreeBrowserDisplay refNode, int loaderID)
    {
    	if (viewer == null) throw new NullPointerException("No viewer.");
    	if (ctx == null)
    		throw new NullPointerException("No security context.");
    	this.ctx = ctx;
    	this.viewer = viewer;
    	this.refNode = refNode;
    	this.loaderID = loaderID;
    	registry = MetadataViewerAgent.getRegistry();
    	mhView = (MetadataHandlerView) 
    	registry.getDataServicesView(MetadataHandlerView.class);
    	dmView = (DataManagerView) 
    	registry.getDataServicesView(DataManagerView.class);
    	ivView = (ImageDataView) 
    	registry.getDataServicesView(ImageDataView.class);
    	adminView = (AdminView) registry.getDataServicesView(AdminView.class);
    }
    
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
    }
    
    /**
     * Notifies the user that an error has occurred and discards the 
     * {@link #viewer}.
     * @see DSCallAdapter#handleException(Throwable)
     */
    public void handleException(Throwable exc)
    {
    	handleException(exc, true);
    }
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel(); 
    
}
