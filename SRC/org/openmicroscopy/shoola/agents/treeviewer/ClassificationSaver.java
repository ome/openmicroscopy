/*
 * org.openmicroscopy.shoola.agents.treeviewer.ClassificationSaver
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.treeviewer;


//Java imports
import java.util.HashSet;
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
    
    /** The images to classify or declassify. */
    private ImageData[] images;
    
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
            case CLASSIFY: return Classifier.CLASSIFY_MODE;
            case DECLASSIFY: return Classifier.DECLASSIFY_MODE;
            default:
                throw new IllegalArgumentException(
                    "Classification mode not supported.");
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer        The Classifier this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param mode          The classification's mode.
     *                      One of the constants defined by this class.
     * @param images        The images to classify or declassify.
     * @param categories    The categories to add to or remove from.
     *                      Mustn't be <code>null</code>.
     */
    public ClassificationSaver(Classifier viewer, int mode, ImageData[] images, 
                            Set categories)
    {
        super(viewer);
        checkMode(mode);
        if (images == null) 
            throw new IllegalArgumentException("No Image.");
        if (images.length == 0) 
            throw new IllegalArgumentException("No Image.");
        if (categories == null || categories.size() == 0) 
            throw new IllegalArgumentException("No category selected.");
        this.images = images; 
        this.mode = mode;
        this.categories = categories;
    }
    
    /**
     * Classifies or declassfies the specified image depending on the mode. 
     * @see ClassifierLoader#load()
     */
    public void load()
    {
        Set objects = new HashSet(images.length);
        for (int i = 0; i < images.length; i++)
            objects.add(images[i]);
        switch (mode) {
            case CLASSIFY:
                handle = dmView.classify(objects, categories, this);
                break;
            case DECLASSIFY:
                handle = dmView.declassify(objects, categories, this);
        }       
    }

    /** 
     * Cancels the data loading. 
     * @see ClassifierLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see ClassifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == Classifier.DISCARDED) return; 
        viewer.saveClassification(images, ((Set) result), convertMode());
    }

}
