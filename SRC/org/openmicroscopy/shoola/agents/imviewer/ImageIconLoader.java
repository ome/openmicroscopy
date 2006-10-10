/*
 * org.openmicroscopy.shoola.agents.imviewer.ImageIconLoader
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
import java.awt.image.BufferedImage;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class ImageIconLoader
    extends DataLoader
{

    /** The ID of the pixels set. */
    private long        pixelsID;
    
    /** The width of the thumbnail. */
    private int         iconWidth;
    
    /** The height of the thumbnail. */
    private int         iconHeight;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    
    /**
     * Creates a new instance
     * 
     * @param viewer        The view this loader is for.
     *                      Mustn't be <code>null</code>.
     * @param pixelsID      The id of the pixels set.
     * @param iconWidth     The width of the icon.
     * @param iconHeight    The height of the icon.
     */
    public ImageIconLoader(ImViewer viewer, long pixelsID, int iconWidth, 
                            int iconHeight)
    {
        super(viewer);
        this.pixelsID = pixelsID;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }
    
    /**
     * Retrieves the rendering settings for the selected pixels set.
     * @see DataLoader#load()
     */
    public void load()
    {
        handle = ivView.loadIconImage(pixelsID, iconWidth, iconHeight, this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see DataLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        BufferedImage img = (BufferedImage) fe.getPartialResult();
        if (img != null)  //Last fe has null object.
            viewer.setIconImage(img);
    }
    
    /**
     * Does nothing as the async call returns <code>null</code>.
     * The actual payload (thumbnails) is delivered progressively
     * during the updates.
     */
    public void handleNullResult() {}
    
}
