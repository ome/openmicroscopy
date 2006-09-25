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
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
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
    extends EditorLoader
{
    
    /** The id of the images. */
    private Set        imageIDs;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer    The Editor this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param imageIDs  The id of the image. 
     */
    public ClassificationPathsLoader(Editor viewer, Set imageIDs)
    {
        super(viewer);
        if (imageIDs == null) 
            throw new IllegalArgumentException("No image.");
        if (imageIDs.size() == 0) 
            throw new IllegalArgumentException("No image.");
        this.imageIDs = imageIDs;
    }

    /** 
     * Retrieves the CategoryGroup/Category paths containing the image. 
     * @see EditorLoader#load()
     */
    public void load()
    {
        handle = dmView.loadClassificationPaths(imageIDs,
                                    OmeroDataService.DECLASSIFICATION, this);
    }

    /** 
     * Cancels the data loading. 
     * @see EditorLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see #handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Editor.DISCARDED) return;  //Async cancel.
        viewer.setRetrievedClassification((Set) result);
    }

}
