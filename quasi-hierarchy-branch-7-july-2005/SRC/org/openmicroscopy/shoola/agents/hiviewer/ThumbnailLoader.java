/*
 * org.openmicroscopy.shoola.agents.hiviewer.ThumbnailLoader
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.env.data.events.DSCallFeedbackEvent;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads all thumbnails for the specified images.
 * This class calls the <code>loadThumbnails</code> method in the
 * <code>HierarchyBrowsingView</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ThumbnailLoader
    extends DataLoader
{

    /** 
     * The <code>ImageSummary</code> objects for the images whose thumbnails 
     * have to be fetched.
     */
    private Set         images;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    
    /**
     * Creates a new instance.
     * 
     * @param viewer The viewer this data loader is for.
     *               Mustn't be <code>null</code>.
     * @param images The <code>ImageSummary</code> objects for the images whose 
     *               thumbnails have to be fetched. 
     */
    public ThumbnailLoader(HiViewer viewer, Set images)
    {
        super(viewer);
        this.images = images;
    }
    
    /**
     * Retrieves the thumbnails.
     * @see DataLoader#load()
     */
    public void load()
    {
        handle = hiBrwView.loadThumbnails(images, 
                                ThumbnailProvider.THUMB_MAX_WIDTH,
                                ThumbnailProvider.THUMB_MAX_HEIGHT, this);
    }
    
    /** Cancels the data loading. */
    public void cancel() { handle.cancel(); }
    
    /** Feeds the thumbnails back to the viewer, as they arrive. */
    public void update(DSCallFeedbackEvent fe) 
    {
        if (viewer.getState() == HiViewer.DISCARDED) return;  //Async cancel.
        String status = fe.getStatus();
        int percDone = fe.getPercentDone();
        if (status == null) 
            status = (percDone == 100) ? "Done" :  //Else
                                       ""; //Description wasn't available.   
        viewer.setStatus(status, percDone);
        ThumbnailData td = (ThumbnailData) fe.getPartialResult();
        if (td != null)  //Last fe has null object.
            viewer.setThumbnail(td.getImageID(), td.getThumbnail());
    }
    
    /**
     * Does nothing as the async call returns <code>null</code>.
     * The actual payload (thumbnails) is delivered progressively
     * during the updates.
     */
    public void handleNullResult() {}
    
    /**
     * Notifies the user that an error has occurred.
     */
    public void handleException(Throwable exc) 
    {
        String s = "Thumbnail Retrieval Failure: ";
        registry.getLogger().error(this, s+exc);
        registry.getUserNotifier().notifyError("Thumbnail Retrieval Failure", 
                                               s, exc);
    }

}
