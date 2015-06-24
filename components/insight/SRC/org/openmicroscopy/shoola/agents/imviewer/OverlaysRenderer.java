/*
 * org.openmicroscopy.shoola.agents.imviewer.OverlaysRenderer 
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
package org.openmicroscopy.shoola.agents.imviewer;

//Java imports
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import omero.romio.PlaneDef;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Call to render the image with or without the overlays.
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
public class OverlaysRenderer
	extends DataLoader
{

	/** The ID of the pixels set. */
    private long pixelsID;
    
    /** The plane to render. */
    private PlaneDef pd;
    
    /** The id of the table. */
    private long tableID;
    
    /** The overlays to render. */
    private Map<Long, Integer> overlays;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID The id of the pixels set.
     * @param pd The plane to render. 
     * @param tableID The id of the table.
     * @param overlays The overlays to render.
     */
    public OverlaysRenderer(ImViewer viewer, SecurityContext ctx,
    	long pixelsID, PlaneDef pd, long tableID, Map<Long, Integer> overlays)
    {
        super(viewer, ctx);
        this.pixelsID = pixelsID;
        this.pd = pd;
        this.tableID = tableID;
        this.overlays = overlays;
    }

    /**
     * Renders a 2D-plane.
     * @see DataLoader#load()
     */
    public void load()
    {
    	handle = ivView.renderOverLays(ctx, pixelsID, pd, tableID, overlays,
    	        this);
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
        viewer.setImage(result);
    }
    
}
