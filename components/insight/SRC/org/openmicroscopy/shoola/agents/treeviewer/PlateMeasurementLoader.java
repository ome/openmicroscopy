/*
 * org.openmicroscopy.shoola.agents.treeviewer.PlateMeasurementLoader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.DataObject;
import pojos.ExperimenterData;

/** 
 * Loads the measurement associated to a given plate.
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
public class PlateMeasurementLoader
	extends DataBrowserLoader
{

	/** The node to load the data for. */
	private TreeImageSet 	node;
	
    /** The node hosting the experimenter the data are for. */
    private TreeImageSet	expNode;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  	handle;
    
	/**
	 * Creates a new instance.
	 * 
	 * @param viewer The viewer this data loader is for.
	 * 				 Mustn't be <code>null</code>.
	 * @param ctx The security context.
	 * @param node	 The node hosting the plate.
	 */
	public PlateMeasurementLoader(Browser viewer, SecurityContext ctx,
			TreeImageSet expNode, TreeImageSet node)
	{
		super(viewer, ctx);
		if (node == null) throw new IllegalArgumentException("No node set.");
		this.expNode = expNode;
		this.node = node;
	}
	
    /**
     * Retrieves the data.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
    	ExperimenterData exp = (ExperimenterData) expNode.getUserObject();
    	DataObject object = (DataObject) node.getUserObject();
    	handle = mhView.loadROIMeasurement(ctx, object, exp.getId(), this);
    }

    /**
     * Cancels the data loading.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /**
     * Feeds the result back to the viewer.
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Browser.DISCARDED) return;  //Async cancel.
        viewer.setLeaves((Collection) result, node, expNode); 
    }
    
}
