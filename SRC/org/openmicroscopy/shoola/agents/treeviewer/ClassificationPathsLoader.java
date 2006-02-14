/*
 * org.openmicroscopy.shoola.agents.treeviewer.ClassificationPathsLoader
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.OmeroPojoService;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

/** 
 * Loads the CategoryGroup/Category paths containing the specified image. 
 * This class calls the <code>loadClassificationPaths</code> method in the
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
public class ClassificationPathsLoader
    extends DataTreeViewerLoader
{
    
    /** The id of the image. */
    private int         imageID;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer    The TreeViewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param imageID   The id of the image. 
     */
    public ClassificationPathsLoader(TreeViewer viewer, int imageID)
    {
        super(viewer);
        if (imageID < 0) 
            throw new IllegalArgumentException("Image Id not valid.");
        this.imageID = imageID;
    }

    /** 
     * Retrieves the CategoryGroup/Category paths containing the image. 
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        handle = dmView.loadClassificationPaths(imageID,
                                    OmeroPojoService.DECLASSIFICATION, this);
    }

    /** 
     * Cancels the data loading. 
     * @see DataTreeViewerLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see #handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == TreeViewer.DISCARDED) return;  //Async cancel.
        viewer.setRetrievedClassification((Set) result);
    }

}
