/*
 * org.openmicroscopy.shoola.agents.fsimporter.DataImporterLoader 
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
package org.openmicroscopy.shoola.agents.fsimporter;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.AdminView;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;

/** 
 * Parent of all classes that load data asynchronously for a {@link Importer}.
 * All these classes invoke methods of the {@link DataManagerView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call. This class provides
 * default implementations of some of the call-backs to notify the 
 * {@link Importer} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link Importer} back with the results.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public abstract class DataImporterLoader 
	extends DSCallAdapter
{

	/** The Importer this data loader is for. */
	protected final Importer viewer;

	/** Convenience reference for subclasses. */
	protected final Registry registry;

    /** Convenience reference for subclasses. */
    protected final ImageDataView ivView;
    
    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView mhView;
    
    /** Convenience reference for subclasses. */
    protected final AdminView adminView;

    /** Convenience reference for subclasses. */
    protected final DataManagerView dmView;
    
    /** Convenience reference for subclasses. */
    protected final HierarchyBrowsingView hiBrwView;
    
    /** The id of the user or <code>-1</code>. */
    protected long userID;
    
    /** The id of the group or <code>-1</code>. */
    protected long groupID;
    
    /** The security context.*/
    protected final SecurityContext ctx;
    
	/**
     * Helper method to return the ID of the currently logged in user.
     * 
     * @return See above.
     */
    protected long getCurrentUserID()
    {
    	return ImporterAgent.getUserDetails().getId();
    }

	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The Importer this data loader is for.
	 *               Mustn't be <code>null</code>.
	 * @param ctx The security context.
	 */
	protected DataImporterLoader(Importer viewer, SecurityContext ctx)
	{
		if (viewer == null) throw new NullPointerException("No viewer.");
        this.viewer = viewer;
        this.ctx = ctx;
		registry = ImporterAgent.getRegistry();
		ivView = (ImageDataView) 
			registry.getDataServicesView(ImageDataView.class);
		mhView = (MetadataHandlerView) 
			registry.getDataServicesView(MetadataHandlerView.class);
		adminView = (AdminView) registry.getDataServicesView(AdminView.class);
		dmView = (DataManagerView) 
			registry.getDataServicesView(DataManagerView.class);
		hiBrwView = (HierarchyBrowsingView) 
    			registry.getDataServicesView(HierarchyBrowsingView.class);
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


	/** Fires an asynchronous data loading. */
	public abstract void load();

	/** Cancels any ongoing data loading. */
	public abstract void cancel(); 

}
