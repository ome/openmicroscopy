/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata;

import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import omero.gateway.model.ExperimenterData;

/** 
 * Updates the experimenter.
 * This class calls the <code>updateExperimenter</code> method in the
 * <code>AdminView</code>.
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
public class ExperimenterEditor 
	extends MetadataLoader
{

	/** The experimenter to update. */
	private ExperimenterData	exp;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle			handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param exp	 The experimenter to update. Mustn't be <code>null</code>.
     * @param loaderID The identifier of the loader.
     */
    public ExperimenterEditor(MetadataViewer viewer, SecurityContext ctx,
    		ExperimenterData exp, int loaderID)
    {
    	super(viewer, ctx, null, loaderID);
    	if (exp == null)
    		throw new IllegalArgumentException("No experimenter to edit.");
    	this.exp = exp;
    }
    
    /** 
	 * Loads the data.
	 * @see MetadataLoader#cancel()
	 */
	public void load()
	{
		handle = adminView.updateExperimenter(ctx, exp, this);
	}
	
	/** 
	 * Cancels the data loading. 
	 * @see MetadataLoader#cancel()
	 */
	public void cancel() { handle.cancel(); }

	/**
     * Feeds the result back to the viewer.
     * @see MetadataLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
    	if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
    	viewer.onAdminUpdated((ExperimenterData) result);
    }

}
