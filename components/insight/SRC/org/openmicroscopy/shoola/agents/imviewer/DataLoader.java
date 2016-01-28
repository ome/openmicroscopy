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

package org.openmicroscopy.shoola.agents.imviewer;

import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.DataHandlerView;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import org.openmicroscopy.shoola.env.data.views.ImageDataView;
import org.openmicroscopy.shoola.env.data.views.MetadataHandlerView;
import omero.log.LogMessage;
import omero.gateway.model.ExperimenterData;

/** 
 * Parent of all classes that load data asynchronously for a {@link ImViewer}.
 * All these classes invoke methods of the {@link DataHandlerView},
 * {@link MetadataHandlerView} or {@link ImageDataView},
 * which this class makes available through a <code>protected</code> field.
 * Also, this class extends {@link DSCallAdapter} so that subclasses
 * automatically become observers to an asynchronous call.  This class provides
 * default implementations of some of the call-backs to notify the 
 * {@link ImViewer} of the progress and the user in the case of errors. 
 * Subclasses should at least implement the <code>handleResult</code> method 
 * to feed the {@link ImViewer} back with the results.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public abstract class DataLoader
    extends DSCallAdapter
{

    /** The viewer this data loader is for. */
    protected final ImViewer            	viewer;
    
    /** Convenience reference for subclasses. */
    protected final Registry registry;
    
    /** Convenience reference for subclasses. */
    protected final ImageDataView ivView;
    
    /** Convenience reference for subclasses. */
    protected final DataHandlerView dhView;
    
    /** Convenience reference for subclasses. */
    protected final MetadataHandlerView mhView;
    
    /** Convenience reference for subclasses. */
    protected final DataManagerView dmView;
    
    /** Convenience reference for subclasses. */
    protected final HierarchyBrowsingView hiBrwView;
    
    /** The security context.*/
    protected final SecurityContext ctx;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     */
    protected DataLoader(ImViewer viewer, SecurityContext ctx)
    {
        if (viewer == null) throw new NullPointerException("No viewer.");
        if (ctx == null) throw new NullPointerException("No security context.");
        this.viewer = viewer;
        this.ctx = ctx;
        registry = ImViewerAgent.getRegistry();
        ivView = (ImageDataView)
                    registry.getDataServicesView(ImageDataView.class);
        dhView = (DataHandlerView)
					registry.getDataServicesView(DataHandlerView.class);
        mhView = (MetadataHandlerView)
					registry.getDataServicesView(MetadataHandlerView.class);
        dmView = (DataManagerView)
		registry.getDataServicesView(DataManagerView.class);
        hiBrwView = (HierarchyBrowsingView)
        			registry.getDataServicesView(HierarchyBrowsingView.class);
    }
    
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
     * Notifies the {@link #viewer} one last time.
     * This way the {@link #viewer} has a chance to hide the progress bar.
     */
    //public void onEnd() { viewer.setStatus(null, -1); }
    
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
        if (state != ImViewer.DISCARDED)
        	registry.getUserNotifier().notifyError("Data Retrieval Failure", 
                                               s, exc);
        viewer.discard();
        //TODO: Change this.  What to do in the case of failure is up to
        //the viewer.  So we need to refactor this b/c the decision is
        //made in the wrong place!
    }
    
    /** Fires an asynchronous data loading. */
    public abstract void load();
    
    /** Cancels any ongoing data loading. */
    public abstract void cancel();
    
}
