/*
 * org.openmicroscopy.shoola.agents.treeviewer.ClassificationSaver
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

import pojos.ImageData;

/** 
 * Classifies or declassifies the specified image depending on the selected 
 * {@link #mode}.
 * This class calls the <code>classify</code> method in the
 * <code>DataManagerView</code> to classify the image and the 
 * <code>declassify</code> method in the <code>DataManagerView</code> to 
 * declassify.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ClassificationSaver
    extends ClassifierLoader
{
    
    /** Indicates to classify the specified image. */
    public static final int CLASSIFY = 0;
    
    /** Indicates to declassify the specified image. */
    public static final int DECLASSIFY = 1;
    
    /** The image to classify or declassify. */
    private ImageData   image;
    
    /** The type of classifier. */
    private int         mode;
    
    /** 
     * The selected categories to add to or remove from depending on the 
     * mode.
     */
    private Set         categories;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;

    /**
     * Controls if the specified mode is supported.
     * 
     * @param m The mode to control.
     */
    private void checkMode(int m)
    {
        switch (m) {
            case CLASSIFY:
            case DECLASSIFY:   
                return;
            default:
                throw new IllegalArgumentException(
                        "Classification mode not supported.");
        }
    }
    
    /**
     * Converts the constant defined by this class into it's corresponding
     * UI constant.
     * 
     * @return See above.
     */
    private int convertMode()
    {
        switch (mode) {
            case CLASSIFY:
                return Classifier.CLASSIFY_MODE;
            case DECLASSIFY:
                return Classifier.DECLASSIFY_MODE;
            default:
                throw new IllegalArgumentException(
                    "Classification mode not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The TreeViewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param mode          The classification's mode.
     *                      One of the constants defined by this class.
     * @param image         The image to classify or declassify.
     * @param categories    The categories to add to or remove from.
     *                      Mustn't be <code>null</code>.
     */
    public ClassificationSaver(Classifier viewer, int mode, ImageData image, 
                            Set categories)
    {
        super(viewer);
        checkMode(mode);
        if (image == null) 
            throw new IllegalArgumentException("Image not valid.");
        if (categories == null || categories.size() == 0) 
            throw new IllegalArgumentException("No category selected.");
        this.image = image; 
        this.mode = mode;
        this.categories = categories;
    }
    
    /**
     * Classifies or declassfies the specified image depending on the mode. 
     * @see DataTreeViewerLoader#load()
     */
    public void load()
    {
        switch (mode) {
            case CLASSIFY:
                handle = dmView.classify(image.getId(), categories, this);
                break;
            case DECLASSIFY:
                handle = dmView.declassify(image.getId(), categories, this);
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
        if (viewer.getState() == Classifier.DISCARDED) return; 
        viewer.saveClassification(image, ((Set) result), convertMode());
    }

}
