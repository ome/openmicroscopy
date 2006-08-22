/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.Classifier
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

package org.openmicroscopy.shoola.agents.hiviewer.clsf;


//Java imports
import java.util.Set;


//Third-party libraries

//Application-internal dependencies
import pojos.ImageData;

/** 
 * Defines the interface provided by the classifier component.
 * The classifier provides a top-level window to let the user classify or
 * declassify a given image.
 * <p>The typical life-cycle of a classifier is as follows.  The object is first
 * created using the {@link ClassifierFactory}, specifying what Image the
 * classifier will handle and if the Image has to be classified (classification
 * mode) or declassified (declassification mode).  After creation the object is
 * in the {@link #NEW} state and is waiting for the {@link #activate() activate}
 * method to be called.  Such a call triggers an asynchronous retrieval of all
 * the metadata needed to classify/declassify the given Image &#151; this will
 * be the Category Group/Category paths that contain the Image (declassfication
 * mode) or those that don't contain it (classification mode).  The object is
 * now in the {@link #LOADING_METADATA} state and a progress window is shown on
 * screen &#151; the user can close this window and thus cancel the data 
 * loading; in this case the object transitions to the {@link #DISCARDED}
 * state described below.</p> 
 * <p>After all the nodes have been retrieved, a classification/declassification
 * dialog is built and set on screen and the object transitions to the 
 * {@link #READY} state.  At which point the user can interact with the widget
 * to select the Category under which the Image should be classified 
 * (classification mode) or the Category from which the Image should be removed
 * (declassification mode).  Upon selection the Image is classified/declassified
 * and the the object transitions to the {@link #DISCARDED} state &#151; the
 * dialog is automatically closed and disposed.  The user can decide to close
 * the dialog before selecting a Category, in which case the object goes into
 * the {@link #DISCARDED} state but no classification/declassification takes
 * place.  In any case, when the object reaches the {@link #DISCARDED} state,
 * all clients should de-reference the component to allow for garbage 
 * collection.</p>
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
public interface Classifier
{

    /** Flag to denote the <i>New</i> state. */
    public static final int     NEW = 1;
    
    /** Flag to denote the <i>Loading Metadata</i> state. */
    public static final int     LOADING_METADATA = 2;
    
    /** Flag to denote the <i>Saving Metadata</i> state. */
    public static final int     SAVING_METADATA = 3;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     READY = 4;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     DISCARDED = 5;

    /** 
     * Flag to denote that a {@link Classifier} was created to classify an
     * Image.
     */
    public static final int     CLASSIFICATION_MODE = 100;
    
    /** 
     * Flag to denote that a {@link Classifier} was created to declassify an
     * Image.
     */
    public static final int     DECLASSIFICATION_MODE = 101;
    
    
    /**
     * Tells whether this component was created to classify or declassify
     * an Image.
     * 
     * @return One of the mode constants defined by this interface.
     */
    public int getMode();
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Returns the image this component is for.
     * 
     * @return See above.
     */
    public ImageData getImage();
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the progress window on screen.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Callback used by a data loader to store the metadata needed to 
     * classify/declassify the Image this component is working with.
     * The loader will set all the paths in the Category Group trees that
     * contain the Image this component is working with, and so can be used
     * to declassify the Image if this component was created to declassify.
     * Otherwise (classification mode), the loader will set all the paths
     * that don't contain the Image this component is working with. 
     * 
     * @param availablePaths All the paths in the Category Group trees that
     *                       are available for classification/declassification.
     *                       This is a set of <code>CategoryGroup/CategoryData
     *                       </code> objects that represent those mentioned 
     *                       paths.
     * @throws IllegalStateException If the current state is not
     *                               {@link #LOADING_METADATA}.
     * @see org.openmicroscopy.shoola.agents.hiviewer.ClassifPathsLoader
     * @see org.openmicroscopy.shoola.agents.hiviewer.DeclassifPathsLoader
     */
    public void setMetadata(Set availablePaths);
    
    /**
     * All the paths in the Category Group trees that this Model is working
     * with.
     * 
     * @return A set of <code>CategoryGroup/CategoryData</code> objects that
     *         represent the above mentioned paths.
     * @throws IllegalStateException If the current state is not {@link #READY}.
     * @see #setMetadata(Set)
     */
    public Set getMetadata();
    
    /**
     * Saves the classification state back to the DB.
     * If this component is in classification mode, then the Image this
     * component is working with will be classified under the specified
     * <code>category</code>.  If this component was created to declassify
     * instead, then the Image will be removed from the specified <code>
     * category</code>.
     *
     * @param categories    The data object that represents the categories
     *                      to/from which this Model's Image should be 
     *                      added/removed. Mustn't be <code>null</code> and 
     *                      should come from the {@link #getMetadata() 
     *                      available} paths.
     * @throws IllegalStateException If the current state is not {@link #READY}.
     */
    public void save(Set categories);
    
    /**
     * Transitions the classifier to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();

    
    /**
     * Sets the categories in which the image has been added to or removed from.
     * 
     * @param categories The categories. 
     */
    public void saveClassification(Set categories);
    
    
    
}
