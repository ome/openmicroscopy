/*
 * org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserLoader 
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
package org.openmicroscopy.shoola.agents.dataBrowser;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import omero.log.LogMessage;

/** 
 * Parent of all classes that load data asynchronously for a {@link DataBrowser}.
 * All these classes invoke methods of the {@link HierarchyBrowsingView},
 * {@link DataHandlerView} and {@link MetadataHandlerView}
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call. This class provides
 * default implementations of some of the callbacks to notify the 
 * {@link DataBrowser} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link DataBrowser} back with the results.
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
public abstract class DataBrowserLoader
	extends DSCallAdapter
{

	/** The viewer this data loader is for. */
    protected final DataBrowser viewer;
    
    /** Convenience reference for subclasses. */
    protected final Registry registry;
    
    /** Convenience reference for subclasses. */
    protected final HierarchyBrowsingView hiBrwView;
    
    /** Convenience reference for subclasses. */
    protected final DataHandlerView dhView;
    
    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView mhView;
    
    /** Convenience reference for subclasses. */
    protected final DataManagerView dmView;
    
    /** The security context.*/
    protected final SecurityContext ctx;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     */
    protected DataBrowserLoader(DataBrowser viewer, SecurityContext ctx)
    {
    	if (viewer == null) throw new NullPointerException("No viewer.");
        if (ctx == null) throw new NullPointerException("No security context.");
        this.viewer = viewer;
        this.ctx = ctx;
        registry = DataBrowserAgent.getRegistry();
        hiBrwView = (HierarchyBrowsingView) 
        			registry.getDataServicesView(HierarchyBrowsingView.class);
        dhView = (DataHandlerView) 
        			registry.getDataServicesView(DataHandlerView.class);
        mhView = (MetadataHandlerView) 
     				registry.getDataServicesView(MetadataHandlerView.class);
        dmView = (DataManagerView) 
			registry.getDataServicesView(DataManagerView.class);
    }
    
    /**
     * Returns the id of the user currently logged in.
     * 
     * @return See above.
     */
    protected long getCurrentUser()
    {
    	return DataBrowserAgent.getUserDetails().getId();
    }
    
    /**
     * Notifies the {@link #viewer} one last time.
     * This way the {@link #viewer} has a chance to hide the progress bar.
     */
    public void onEnd() { viewer.setStatus(null, 100); }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    public void handleNullResult() 
    {
        handleException(new Exception("No data available."));
    }
    
    /**
     * Notifies the user that the data retrieval has been cancelled.
     */
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
    	int state = viewer.getState();
    	String s = "Data Retrieval Failure: ";
        LogMessage msg = new LogMessage();
        msg.print("State: "+state);
        msg.print(s);
        msg.print(exc);
        registry.getLogger().error(this, msg);
        if (state != DataBrowser.DISCARDED)
        	registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
        //viewer.discard();
        //TODO: Change this.  What to do in the case of failure is up to
        //the viewer.  So we need to refactor this b/c the decision is
        //made in the wrong place!
    }
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel();
    
}
