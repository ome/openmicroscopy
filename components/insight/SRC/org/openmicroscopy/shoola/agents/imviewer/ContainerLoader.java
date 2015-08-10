/*
 * org.openmicroscopy.shoola.agents.imviewer.ContainerLoader 
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
package org.openmicroscopy.shoola.agents.imviewer;


//Java imports
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.LookupNames;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ProjectData;

/** 
 * Loads the containers containing the passed image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
public class ContainerLoader 
	extends DataLoader
{

	/** The id of the image. */
	private long imageID;
	
	/** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model.
     * @param ctx The security context.
     * @param imageID The id of the image. 
     */
	public ContainerLoader(ImViewer model, SecurityContext ctx, long imageID)
	{
		super(model, ctx);
		this.imageID = imageID;
	}

	/**
     * Creates a new instance.
     * 
     * @param model Reference to the model.
     * @param ctx The security context.
     */
	public ContainerLoader(ImViewer model, SecurityContext ctx)
	{
		super(model, ctx);
		this.imageID = -1;
	}
	
	/**
     * Loads the container.
     * @see DataLoader#load()
     */
    public void load()
    {
    	long userID = -1;
    	if (viewer.getDisplayMode() == LookupNames.EXPERIMENTER_DISPLAY)
    		userID = ImViewerAgent.getUserDetails().getId();
        handle = dmView.loadContainerHierarchy(ctx, ProjectData.class, null,
				false, userID, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        viewer.setContainers((Collection) result);
    }
    
}
