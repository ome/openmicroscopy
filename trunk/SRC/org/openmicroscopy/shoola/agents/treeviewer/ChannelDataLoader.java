/*
 * org.openmicroscopy.shoola.agents.treeviewer.ChannelDataLoader
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

package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.env.data.views.CallHandle;


/** 
 * Retrieves the channels metadata for the given set of pixels.
 * This class calls the <code>loadChannelsData</code> method in the
 * <code>HierarchyBrowsingView</code>.
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ChannelDataLoader
    extends EditorLoader
{

    /** The id of the pixels set. */
    private long        pixelsID;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer    The Editor this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param pixelsID  The pixels id.
     */
    public ChannelDataLoader(Editor viewer, long pixelsID)
    {
        super(viewer);
        this.pixelsID = pixelsID;
    }
    
    /** 
     * Retrieves the emission wavelengths for the specified pixels set.
     * @see EditorLoader#load()
     */
    public void load()
    {
        handle = dmView.loadChannelsData(pixelsID, this);
    }
    
    /** 
     * Cancels the data loading. 
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see EditorLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        if (viewer.getState() == Editor.DISCARDED) return;  //Async cancel.
        viewer.setChannelsData((List) result);
    }
    
    /**
     * Overridden so that we don't notify the user that the channel metadata
     * retrieval has been cancelled.
     * @see EditorLoader#handleCancellation() 
     */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }
    
}
