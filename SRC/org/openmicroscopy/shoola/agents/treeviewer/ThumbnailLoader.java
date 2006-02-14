/*
 * org.openmicroscopy.shoola.agents.treeviewer.ThumbnailLoader
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import pojos.ImageData;

/** 
 * Loads the thumbnail for the specified image.
 * This class calls the <code>loadThumbnail</code> method in the
 * <code>DataManagerView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ThumbnailLoader
    extends DataTreeViewerLoader
{

    /** The maximum width of the thumbnail. */
    private static final int            THUMB_MAX_WIDTH = 96; 
    
    /** The maximum height of the thumbnail. */
    private static final int            THUMB_MAX_HEIGHT = 96;
    
    /** The parent of the thumbnail. */
    private ImageData   image;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The TreeViewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param image  The <code>ImageData</code> object.
     */
    public ThumbnailLoader(TreeViewer viewer, ImageData image)
    {
        super(viewer);
        if (image == null) throw new IllegalArgumentException("No image.");
        this.image = image;
    }

    /** 
     * Retrieves the thumbnail. 
     * @see DataBrowserLoader#load()
     */
    public void load()
    {
        handle = dmView.loadThumbnail(image, THUMB_MAX_WIDTH,
                                        THUMB_MAX_HEIGHT, this);
    }

    /** 
     * Cancels the data loading. 
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the thumbnails back to the viewer, as they arrive. 
     * @see DataTreeViewerLoader#update(DSCallFeedbackEvent)
     */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        ThumbnailData td = (ThumbnailData) fe.getPartialResult();
        if (td != null)  //Last fe has null object.
            viewer.setDataObjectThumbnail(td.getThumbnail());
    }
    
    /**
     * Does nothing as the async call returns <code>null</code>.
     * The actual payload (thumbnails) is delivered progressively
     * during the updates.
     */
    public void handleNullResult() {}
    
    /**
     * Notifies the user that an error has occurred.
     * @see DataTreeViewerLoader#handleException(Throwable)
     */
    public void handleException(Throwable exc) 
    {
        String s = "Thumbnail Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("Thumbnail Retrieval Failure", 
                                               s, exc);
    }
    
}
