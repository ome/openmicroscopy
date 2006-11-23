/*
 * org.openmicroscopy.shoola.agents.hiviewer.ChannelMetadataLoader
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

package org.openmicroscopy.shoola.agents.hiviewer;




//Java imports
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.ImageData;


/** 
 * Retrieves the channels metadata for the given set of pixels.
 * This class calls the <code>loadChannelsData</code> method in the
 * <code>DataManagerView</code>.
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
public class ChannelMetadataLoader
    extends CBDataLoader
{
    
    /** The image to handle.  */
    private ImageData		image;

    /** Handle to the async call so that we can cancel it. */
    private CallHandle      handle;
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer    The viewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param image  	The image to handle.                         
     */                        
    public ChannelMetadataLoader(ClipBoard viewer, ImageData image)
    {
        super(viewer);
        if (image == null) throw new IllegalArgumentException("No image.");
        this.image = image;
    }
    
    /**
     * Retrives the channels metadata.
     * @see CBDataLoader#load()
     */
    public void load()
    {
        handle = hiBrwView.loadChannelsData(image.getDefaultPixels().getId(), 
        									this);
    }
    
    /** 
     * Cancels the data saving. 
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see CBDataLoader#handleResult(Object)
     */
    public void handleResult(Object result) 
    {
        if (clipBoard.getState() == ClipBoard.DISCARDED_ANNOTATIONS) return;
        clipBoard.setChannelsMetadata((List) result, image);
    }
    
    /**
     * Overridden so that we don't notify the user that the channel metadata
     * retrieval has been cancelled.
     * @see CBDataLoader#handleCancellation() 
     */
    public void handleCancellation() 
    {
        String info = "The data retrieval has been cancelled.";
        registry.getLogger().info(this, info);
    }
    
}
