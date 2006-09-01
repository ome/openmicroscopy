/*
 * org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierComponent
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
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.ImageData;


/** 
 * Implements the {@link Classifier} interface to the functionality
 * required of the classifier component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 * 
 * @see org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierModel
 * @see org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierUI
 * @see org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierControl
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class ClassifierComponent
    extends AbstractComponent
    implements Classifier
{

    /** The Model sub-component. */
    private ClassifierModel     model;
    
    /** The Controller sub-component. */
    private ClassifierControl   controller;
    
    /** The View sub-component. */
    private ClassifierUI        view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    ClassifierComponent(ClassifierModel model)
    {
        if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new ClassifierControl(this);
        view = new ClassifierUI();
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        controller.initialize(view);
        model.getParentModel().addPropertyChangeListener(
                    TreeViewer.THUMBNAIL_LOADED_PROPERTY, controller);
        view.initialize(controller, model);
    }
    
    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Classifier#getState()
     */
    public int getState() { return model.getState(); }
    
    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Classifier#activate()
     */
    public void activate()
    {
        switch (model.getState()) {
            case NEW:
                model.fireClassificationLoading();
                fireStateChange();
                break;
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
        }  
        
    }
    
    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Classifier#discard()
     */
    public void discard()
    {
        if (model.getState() != DISCARDED) {
            model.discard();
            fireStateChange();
        }
    }
    
    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Classifier#getUI()
     */
    public JComponent getUI()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return view;
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Classifier#setThumbnail(BufferedImage)
     */
    public void setThumbnail(BufferedImage thumbnail)
    {
        if (model.getState() != DISCARDED) {
            if (thumbnail == null)
                throw new IllegalArgumentException("No thumbnail.");
            view.setThumbnail(thumbnail);
        }
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Classifier#setClassifications(Set)
     */
    public void setClassifications(Set paths)
    {
        if (model.getState() != LOADING_CLASSIFICATION)
            throw new IllegalStateException("This method should only be " +
                    "invoked in the LOADING_CLASSIFICATION state.");
        if (paths == null)
            throw new IllegalArgumentException("No paths to set.");

        long userID = model.getUserID();
        long groupID = model.getParentModel().getRootGroupID();
        Set nodes = TreeViewerTranslator.transformDataObjectsCheckNode(paths,
                                                    userID, groupID);
        model.setPaths(nodes);
        view.showClassifications();
        fireStateChange();
        if (model.getDataObjects().length == 1)
            firePropertyChange(TreeViewer.THUMBNAIL_LOADING_PROPERTY, null,
                                model.getDataObject());
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Classifier#saveClassification(ImageData[], Set, int)
     */
    public void saveClassification(ImageData[] images, Set categories, int mode)
    {
        if (model.getState() != SAVING_CLASSIFICATION)
            throw new IllegalStateException("This method should only be " +
                    "invoked in the SAVE_CLASSIFICATION state.");
        if (categories == null)
            throw new IllegalArgumentException("Categories shouln't be null.");
        if (images == null)
            throw new IllegalArgumentException("No image.");
        if (images.length == 0)
            throw new IllegalArgumentException("No image.");
        if (mode != CLASSIFY_MODE && mode != DECLASSIFY_MODE)
            throw new IllegalArgumentException("Classification mode not " +
                    "supported.");
        model.saveClassification(images, categories, mode);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Classifier#getMode()
     */
    public int getMode()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return model.getMode();
    }

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#cancel()
     */
    public void cancel()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        model.cancel();
    }
    
    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#close()
     */
    public void close() 
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        firePropertyChange(CLOSE_CLASSIFIER_PROPERTY, Boolean.FALSE, 
                            Boolean.TRUE);
    }
    
}
