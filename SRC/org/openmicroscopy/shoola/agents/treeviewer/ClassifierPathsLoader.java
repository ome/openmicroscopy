/*
 * org.openmicroscopy.shoola.agents.treeviewer.ClassifierPathsLoader
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
import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;

/** 
 * Loads the CategoryGroup/Category paths containing the specified image
 * if the mode is {@link Classifier#DECLASSIFY_MODE} or loads
 * the available CategoryGroup/Category paths if the mode is 
 * {@link Classifier#CLASSIFY_MODE}.
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
public class ClassifierPathsLoader
    extends ClassifierLoader
{

    /** The id of the image to classify or declassify. */
    private long        imageID;
    
    /** The type of classifier. */
    private int         mode;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Controls if the specified mode is supported.
     * 
     * @param m The value to control.
     */
    private void checkMode(int m)
    {
        switch (m) {
            case Classifier.CLASSIFY_MODE:
            case Classifier.DECLASSIFY_MODE:   
                return;
            default:
                throw new IllegalArgumentException("Mode not supported.");
        }
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer    The TreeViewer this data loader is for.
     *                  Mustn't be <code>null</code>.
     * @param imageID   The id of the image. 
     * @param mode      The type of classifier. One of the following constants:
     *                  {@link Classifier#DECLASSIFY_MODE} or 
     *                  {@link Classifier#CLASSIFY_MODE}.
     */
    public ClassifierPathsLoader(Classifier viewer, long imageID, int mode)
    {
        super(viewer);
        if (imageID < 0) 
            throw new IllegalArgumentException("Image Id not valid.");
        checkMode(mode);
        this.imageID = imageID;
        this.mode = mode;
    }
    
    /** 
     * Retrieves the CategoryGroup/Category paths containing the image
     * if the mode is {@link Classifier#DECLASSIFY_MODE} or 
     * retrieves the available paths if the mode is 
     * {@link Classifier#CLASSIFY_MODE}.
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        switch (mode) {
            case Classifier.DECLASSIFY_MODE:
                handle = dmView.loadClassificationPaths(imageID,
                        DataManagerView.DECLASSIFICATION, this);
                break;
            case Classifier.CLASSIFY_MODE:
                handle = dmView.loadClassificationPaths(imageID,
                        DataManagerView.CLASSIFICATION_ME, this);
        } 
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
        if (viewer.getState() == Classifier.DISCARDED) return; //Async cancel.
        viewer.setClassifications((Set) result);
    }
}
