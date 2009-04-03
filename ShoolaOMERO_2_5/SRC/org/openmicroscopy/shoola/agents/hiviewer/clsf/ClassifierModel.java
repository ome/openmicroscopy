/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.ClassifierModel
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
import org.openmicroscopy.shoola.agents.hiviewer.ClassifLoader;

/** 
 * The Model component in the {@link Classifier} MVC triad.
 * This class tracks the {@link Classifier}'s state, knows how to initiate data
 * retrievals, store the results and how to trigger the (de-)classification of
 * an Image.  The {@link ClassifierComponent} intercepts the results of data 
 * loadings, feeds them back to this class and fires state transitions as 
 * appropriate.  Similarly, it intercepts requests to (de-)classify an Image
 * and forwards to this class.
 * <p>This class is abstract because it doesn't know whether the component is
 * in classification or declassification mode.  Subclasses fill this gap by
 * providing a {@link ClassifLoader} to retrieve metadata and by saving the
 * metadata, depending on the classification mode.</p>
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
abstract class ClassifierModel
{
    
    /**
     * Stores the metadata needed to classify/declassify the Image the 
     * component is working with.
     * This will be all the Category Group/Category paths that lead to the
     * Image when in classification mode.  Otherwise (de-classification mode)
     * all the paths that don't lead to the Image.
     */
    private Set                 availablePaths;
    
    /** Holds one of the state flags defined by {@link Classifier}. */
    protected int               state;
    
    /** Loads all the required metadata. */
    protected ClassifLoader     loader;
    
    /** The id of the Image this Model is for. */
    protected int               imageID;
    
    /** Reference to the component that embeds this model. */
    protected Classifier        component;
    
    
    /**
     * Creates a new object and sets its state to {@link Classifier#NEW}.
     * The {@link #initialize(Classifier) initialize} method should be
     * called straight after creation to complete initialization.
     * 
     * @param imageID The id of the Image this Model is for.
     */
    protected ClassifierModel(int imageID) 
    { 
        this.imageID = imageID;
        state = Classifier.NEW; 
    }
    
    /**
     * Called after creation to allow this object to store a back reference to 
     * the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Classifier component) { this.component = component; }
    
    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link Classifier} interface.  
     */
    int getState() { return state; }
    
    /**
     * Returns the id of the Image this Model is working with.
     * @return See above.
     */
    int getImageID() { return imageID; }
    
    /**
     * Starts the asynchronous retrieval of the metadata needed by this
     * model and sets the state to {@link Classifier#LOADING_METADATA}. 
     */
    void fireMetadataLoading()
    {
        state = Classifier.LOADING_METADATA;
        loader = createClassifLoader();
        loader.load();
    }
    
    /**
     * Stores the metadata needed by this Model and sets the state to
     * {@link Classifier#READY}.
     * 
     * @param availablePaths All the paths in the Category Group trees that this
     *                       Model will be working with.
     */
    void setMetadata(Set availablePaths) 
    {
        if (availablePaths == null)
            throw new NullPointerException("No available paths.");
        this.availablePaths = availablePaths;
        state = Classifier.READY;
    }
    
    /**
     * All the paths in the Category Group trees that this Model is working
     * with.
     * 
     * @return A set of <code>CategoryGroup/CategoryData</code> objects that
     *         represent the above mentioned paths.
     */
    Set getMetadata() { return availablePaths; }
    
    /**
     * Sets the object in the {@link Classifier#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
        if (loader != null) {
            loader.cancel();
            loader = null;
        }
        state = Classifier.DISCARDED;
    }
    
    /**
     * Tells whether this is a classification or declassification Model.
     * 
     * @return One of the mode constants defined by the {@link Classifier} 
     *         interface.
     */
    protected abstract int getMode();
    
    /**
     * Creates a new data loader that can retrieve the hierarchy objects
     * needed by this Model.
     * This will be an object to load all the CG/C paths that can be
     * used for classification or declassification, depending on the
     * concrete Model.
     * 
     * @return A suitable data loader.
     */
    protected abstract ClassifLoader createClassifLoader();
    
    /**
     * Saves the classification state back to the DB.
     * This operation depends on the classification mode and thus is left
     * to concrete models. A classification Model will classify the Image
     * this Model is working with. A declassification Model will declassify
     * it. 
     *
     * @param categories Collection of categories to add the image to or 
     *                   remove the image from.
     */
    protected abstract void save(Set categories);
    
}
