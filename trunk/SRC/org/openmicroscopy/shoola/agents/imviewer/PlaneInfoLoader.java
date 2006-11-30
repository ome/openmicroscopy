/*
 * org.openmicroscopy.shoola.agents.imviewer.PlaneInfoLoader
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.imviewer;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.treeviewer.DataBrowserLoader;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the plane info associated to the currently displayed planes.
 * This class calls <code></code> method in the
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
    private long        pixelsID;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance
     * 
     * @param viewer    The view this loader is for.
     *                  Mustn't be <code>null</code>.
     * @param pixelsID  The id of pixels set.
     */
    public PlaneInfoLoader(ImViewer viewer, long pixelsID)
    {
        super(viewer);
        this.pixelsID = pixelsID;
    }

    /**
     * Retrieves the information on the plane currently displayed i.e.
     * depending on z-section, timepoint and selected channels.
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
        // TODO Auto-generated method stub
        
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataBrowserLoader#cancel()
     */
    public void cancel() { handle.cancel(); }

    /** 
     * Feeds the result back to the viewer. 
     * @see DataBrowserLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        //viewer.setChannelMetadata((Set) result);
    }
    
}
