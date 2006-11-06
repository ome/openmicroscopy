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
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.DataManagerView;
import pojos.CategoryGroupData;
import pojos.ExperimenterData;
import pojos.GroupData;

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

    /** The id of the images to classify or declassify. */
    private Set        imageIDs;
    
    /** The type of classifier. */
    private int         mode;
    
    /** 
     * The level of the root node. One of the following constants.
     * {@link TreeViewer#USER_ROOT} or {@link TreeViewer#GROUP_ROOT}.
     */
    private int         rootLevel;
    
    /** The id of the root node. */
    private long        rootNodeID;
    
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
     * Converts the UI rootLevel into its corresponding class.
     * @return See above.
     */
    protected Class convertRootLevel()
    {
        switch (rootLevel) {
            case TreeViewer.USER_ROOT: return ExperimenterData.class;
            case TreeViewer.GROUP_ROOT: return GroupData.class;
            default:
                throw new IllegalArgumentException("Level not supported");
        }
    }
    
    /**
     * Determines the rootID depending on the rootLevel.
     *     
     * @return See above.
     */
    protected long getRootID()
    {
        switch (rootLevel) {
            case TreeViewer.USER_ROOT:
                ExperimenterData exp = (ExperimenterData) 
                registry.lookup(LookupNames.CURRENT_USER_DETAILS);
                return exp.getId();  
            case TreeViewer.GROUP_ROOT:
                return rootNodeID;
            default:
                throw new IllegalArgumentException("Level not supported");
        }
    }
    
    /**
     * Creates a new instance. 
     * 
     * @param viewer        The TreeViewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param imageIDs      The id of the images. 
     * @param mode          The type of classifier. One of the following 
     *                      constants:
     *                      {@link Classifier#DECLASSIFY_MODE} or 
     *                      {@link Classifier#CLASSIFY_MODE}.
     * @param rootLevel     The level of the root.
     * @param rootNodeID    The id of the root.    
     */
    public ClassifierPathsLoader(Classifier viewer, Set imageIDs, int mode,
                                int rootLevel, long rootNodeID)
    {
        super(viewer);
        if (imageIDs == null) 
            throw new IllegalArgumentException("No images.");
        if (imageIDs.size() == 0) 
            throw new IllegalArgumentException("No images.");
        checkMode(mode);
        this.rootLevel = rootLevel;
        this.rootNodeID = rootNodeID;
        this.imageIDs = imageIDs;
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
                handle = dmView.loadClassificationPaths(imageIDs,
                        DataManagerView.DECLASSIFICATION, this);
                break;
            case Classifier.CLASSIFY_MODE:
                handle = dmView.loadContainerHierarchy(CategoryGroupData.class, 
                                    null, false,
                                    convertRootLevel(), getRootID(), this);
               // handle = dmView.loadClassificationPaths(imageIDs,
                //        DataManagerView.CLASSIFICATION_NME, this);
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
