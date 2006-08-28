/*
 * org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierWin
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

package org.openmicroscopy.shoola.agents.treeviewer.clsf;

//Java imports
import java.awt.image.BufferedImage;
import java.util.Set;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;

import pojos.ImageData;

/** 
 * The component hosting the classification tree.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface Classifier
    extends ObservableComponent
{

    /** Flag to denote the <i>New</i> state. */
    public static final int     NEW = 1;
    
    /** Flag to denote the <i>Loading Classification</i> state. */
    public static final int     LOADING_CLASSIFICATION = 2;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     READY = 3;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     DISCARDED = 4;
    
    /** Flag to denote the <i>Saving classification</i> state. */
    public static final int     SAVING_CLASSIFICATION = 5;
    
    /** Identifies the classify model. */
    public static final int     CLASSIFY_MODE = 0;
    
    /** Identifies the classify panel. */
    public static final int     DECLASSIFY_MODE = 1;
    
    /** Bounds property to indicate to close the classifier. */
    public static final String  CLOSE_CLASSIFIER_PROPERTY = "closeClassifier";

    /**
     * Sets the specified thumbnail 
     * 
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(BufferedImage thumbnail);
    
    /**
     * Sets the retrieved CategoryGroup/Category paths.
     * 
     * @param paths The paths to set.
     */
    public void setClassifications(Set paths);
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the progress window on screen.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();

    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Transitions the classifier to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /**
     * Cancels any ongoing data loading.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public void cancel();

    /** 
     * Returns the UI component. 
     * 
     * @return See above.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public JComponent getUI();

    /**
     * Returns the classifier mode.
     * 
     * @return See above.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public int getMode();
    
    /**
     * Sets the categories in which the image has been added to or removed from.
     * 
     * @param images        The image classified or declassified.
     * @param categories    The categories. 
     * @param m             The type of operation. one of the following
     *                      constants: {@link #CLASSIFY_MODE} or
     *                      {@link #DECLASSIFY_MODE}.
     */
    public void saveClassification(ImageData[] images, Set categories, int m);
    
    /** 
     * Closes the {@link Classifier}. 
     * 
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    void close();   

}
