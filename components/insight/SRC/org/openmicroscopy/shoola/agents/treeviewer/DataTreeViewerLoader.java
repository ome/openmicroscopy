/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer;

import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.AdminView;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import omero.log.LogMessage;
import omero.gateway.model.ExperimenterData;

/** 
 * Parent of all classes that load data asynchronously for a {@link TreeViewer}.
 * All these classes invoke methods of the {@link DataManagerView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call. This class provides
 * default implementations of some of the call-backs to notify the 
 * {@link TreeViewer} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link TreeViewer} back with the results.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public abstract class DataTreeViewerLoader
  	extends DSCallAdapter
{
  
	/** The TreeViewer this data loader is for. */
	protected final TreeViewer viewer;

	/** Convenience reference for subclasses. */
	protected final Registry registry;

	/** Convenience reference for subclasses. */
	protected final DataManagerView dmView;

	/** Convenience reference for subclasses. */
	protected final DataHandlerView dhView;
	
    /** Convenience reference for subclasses. */
    protected final HierarchyBrowsingView hiBrwView;
    
    /** Convenience reference for subclasses. */
    protected final ImageDataView ivView;

    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView mhView;
    
    /** Convenience reference for subclasses. */
    protected final AdminView adminView;
    
    /** The security context.*/
    protected final SecurityContext ctx;
    
	/**
	 * Converts the UI rootLevel into its corresponding class.
	 * @return See above.
	 */
	protected Class convertRootLevel() { return ExperimenterData.class; }

	/**
     * Helper method to return the ID of the currently logged in user.
     * 
     * @return See above.
     */
    protected long getCurrentUserID()
    {
    	return ((ExperimenterData) registry.lookup(
		        LookupNames.CURRENT_USER_DETAILS)).getId();
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The TreeViewer this data loader is for.
	 *               Mustn't be <code>null</code>.
	 * @param ctx The security context.
	 */
	protected DataTreeViewerLoader(TreeViewer viewer, SecurityContext ctx)
	{
		if (viewer == null) throw new NullPointerException("No viewer.");
		if (ctx == null) throw new NullPointerException("No security context.");
		this.viewer = viewer;
		this.ctx = ctx;
		registry = TreeViewerAgent.getRegistry();
		dmView = (DataManagerView) 
		registry.getDataServicesView(DataManagerView.class);
		dhView = (DataHandlerView) 
		registry.getDataServicesView(DataHandlerView.class);
		hiBrwView = (HierarchyBrowsingView) registry.
		getDataServicesView(HierarchyBrowsingView.class);
		ivView = (ImageDataView) 
		registry.getDataServicesView(ImageDataView.class);
		mhView = (MetadataHandlerView) 
			registry.getDataServicesView(MetadataHandlerView.class);
		adminView = (AdminView) registry.getDataServicesView(AdminView.class);
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

	/** Notifies the user that the data retrieval has been canceled. */
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
        if (state != TreeViewer.DISCARDED)
        	registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
		viewer.cancel();
	}

	/** Fires an asynchronous data loading. */
	public abstract void load();

	/** Cancels any ongoing data loading. */
	public abstract void cancel(); 
  
}
