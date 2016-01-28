/*
 * org.openmicroscopy.shoola.agents.imviewer.PlaneInfoLoader
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the plane info related to a given pixels set.
 * This class calls <code>loadPlaneInfo</code> method in the
 * <code>ImViewerView</code>.
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
public class PlaneInfoLoader
    extends DataLoader
{

    /** The ID of the pixels set. */
    private long pixelsID;
    
    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle handle;
    
    /** The selected z-section or <code>-1</code>. */
    private int defaultZ;
    
    /** The selected timepoint or <code>-1</code>. */
    private int defaultT;
    
    /**
     * Creates a new instance
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID  The id of pixels set.
     * @param defaultZ  The selected z-section.
     * @param defaultT  The selected timepoint.
     */
    public PlaneInfoLoader(ImViewer viewer, SecurityContext ctx, long pixelsID,
    		int defaultZ, int defaultT)
    {
        super(viewer, ctx);
        this.pixelsID = pixelsID;
        this.defaultZ = defaultZ;
        this.defaultT = defaultT;
    }
    
    /**
     * Creates a new instance
     * 
     * @param viewer The view this loader is for. Mustn't be <code>null</code>.
     * @param ctx The security context.
     * @param pixelsID  The id of pixels set.
     */
    public PlaneInfoLoader(ImViewer viewer, SecurityContext ctx, long pixelsID)
    {
       this(viewer, ctx, pixelsID, -1, -1);
    }

    /**
     * Loads the plane information
     * @see DataLoader#load()
     */
    public void load()
    {
    	if (defaultT < 0 || defaultZ < 0)
    		handle = ivView.loadPlaneInfo(ctx, pixelsID, -1, -1, -1, this);
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
        viewer.setPlaneInfo((Collection) result);
    }

}
