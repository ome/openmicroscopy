/*
 * org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierModel
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
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.ClassificationSaver;
import org.openmicroscopy.shoola.agents.treeviewer.ClassifierLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ClassifierPathsLoader;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import pojos.DataObject;
import pojos.ImageData;

/** 
 * The Model component in the <code>Classifier</code> MVC triad.
 * This class tracks the <code>Classifier</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. However, this class doesn't know the actual hierarchy
 * the <code>Classifier</code> is for. Subclasses fill this gap and provide  
 * a suitable data loader. The {@link ClassifierComponent} intercepts the 
 * results of data loadings, feeds them back to this class and fires state
 * transitions as appropriate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClassifierModel
{
    
    /** One of the constants defined by this {@link Classifier}. */
    private int                 mode;
    
    /** The images to classify or declassify. */
    private ImageData[]         images;
    
    private int                 state;
    
    /**
     * All the paths in the Category Group trees that
     * are available for classification/declassification.
     */
    private Set                 paths;
    
    /** Back pointer to the parent model. */
    private TreeViewer          parentModel;
    
    /** 
     * Will either be a data loader or
     * <code>null</code> depending on the current state. 
     */
    private ClassifierLoader    currentLoader;
    
    /** Reference to the component that embeds this model. */
    protected Classifier        component;
    
    /**
     * Creates a new instance.
     * 
     * @param parentModel   Reference to the parent model.
     *                      Mustn't be <code>null</code>.
     * @param mode          One of the following constants: 
     *                      {@link Classifier#CLASSIFY_MODE} or
     *                      {@link Classifier#DECLASSIFY_MODE}.
     * @param images        The image to handle. Mustn't be <code>null</code>.
     */
    ClassifierModel(TreeViewer parentModel, int mode, ImageData[] images)
    {
        if (parentModel == null)
            throw new IllegalArgumentException("No parent model.");
        if (images == null)
            throw new IllegalArgumentException("No image.");
        if (images.length == 0)
            throw new IllegalArgumentException("No image.");
        if (mode != Classifier.CLASSIFY_MODE &&
                mode != Classifier.DECLASSIFY_MODE)
            throw new IllegalArgumentException("Classification mode not " +
                    "supported.");
        this.parentModel = parentModel;
        this.images = images;
        this.mode = mode;
        state = Classifier.NEW;
    }
    
    /**
     * Called by the <code>Classifier</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Classifier component) { this.component = component; }
    
    /**
     * Returns the {@link TreeViewer} parent model. This method should
     * only be invoked to register the control with property change.
     * 
     * @return See above.
     */
    TreeViewer getParentModel() { return parentModel; }
    
    /**
     * Returns the current state.
     * 
     * @return One of the flags defined by the {@link Classifier} interface.  
     */
    int getState() { return state; }
    
    /**
     * Returns the retrieved classfication paths.
     * 
     * @return See above.
     */
    Set getPaths() { return paths; }
    
    /**
     * Returns the classifier mode. One of the following contants:
     * {@link Classifier#CLASSIFY_MODE} or {@link Classifier#DECLASSIFY_MODE}.
     * 
     * @return See above
     */
    int getMode() { return mode; }

    /**
     * Sets the retrieved classfication paths.
     * 
     * @param paths The paths to set.
     */
    void setPaths(Set paths) { this.paths = paths; }
    
    /**
     * Returns the currently edited {@link ImageData}.
     * 
     * @return See above.
     */
    ImageData getDataObject() { return images[0]; }
    
    /**
     * Returns the images this classifier is for.
     * 
     * @return See above.
     */
    ImageData[] getDataObjects() { return images; }
    
    /**
     * Fires an asynchronous retrieval of the CategoryGroup/Category paths.
     */
    void fireClassificationLoading()
    {
        state = Classifier.LOADING_CLASSIFICATION;
        Set ids = new HashSet(images.length);
        for (int i = 0; i < images.length; i++)
            ids.add(new Long(images[i].getId()));
        currentLoader = new ClassifierPathsLoader(component, ids, mode);
        currentLoader.load();
    }
    
    /**
     * Fires an asynchronous call to classify the currently selected image
     * in the specified categories.
     * 
     * @param categories Collection of selected categories.
     */
    void fireClassificationSaving(Set categories)
    {
        state = Classifier.SAVING_CLASSIFICATION;
        currentLoader = new ClassificationSaver(component, 
                ClassificationSaver.CLASSIFY, images, categories);
        currentLoader.load();  
    }
    
    /**
     * Fires an asynchronous call to remove the currently selected image
     * from the specified categories.
     * 
     * @param categories Collection of selected categories.
     */
    void fireDeclassificationSaving(Set categories)
    {
        state = Classifier.SAVING_CLASSIFICATION;
        currentLoader = new ClassificationSaver(component, 
                ClassificationSaver.DECLASSIFY, images, categories);
        currentLoader.load();  
    }

    /** 
     * Browses or view the specified <code>DataObject</code>.
     * 
     * @param object The object to browse or view.
     */
    void browse(DataObject object)
    {
        if (object != null) {
            ViewCmd cmd = new ViewCmd(parentModel, object);
            cmd.execute();
        }
    }
    
    /**
     * Sets the object in the {@link Classifier#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
        cancel();
        state = Classifier.DISCARDED;
    }

    /**
     * Cancels any ongoing data and sets the state to 
     * {@link Classifier#READY}.
     */
    void cancel()
    {
        if (currentLoader != null) {
            currentLoader.cancel();
            currentLoader = null;
        }
        state = Classifier.READY;
    }

    /**
     * Updates the different views when the image has been classified or 
     * declassified.
     * 
     * @param images        The classified or declassified image.
     * @param categories    The categories in which the image was added to or
     *                      removed from.
     * @param mode          The type of operation i.e. classification or 
     *                      declassification.
     */
    void saveClassification(ImageData[] images, Set categories, int mode)
    {
        state = Classifier.READY;
        parentModel.onImageClassified(images, categories, mode);
    }
    
    /**
     * Returns the user's id. Helper method
     * 
     * @return See above.
     */
    long getUserID() { return parentModel.getUserDetails().getId(); }
    
}
