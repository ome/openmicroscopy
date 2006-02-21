/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorComponent
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

package org.openmicroscopy.shoola.agents.treeviewer.editors;


//Java imports
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.AnnotationData;
import pojos.DataObject;


/** 
 * Implements the {@link Editor} interface to the functionality
 * required of the classifier component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class EditorComponent
    extends AbstractComponent
    implements Editor
{

    /** The Model sub-component. */
    private EditorModel     model;
    
    /** The Controller sub-component. */
    private EditorControl   controller;
    
    /** The View sub-component. */
    private EditorUI        view;
    
    /**
     * Controls if the specified annotation operation is supported.
     * 
     * @param i The index to control.
     */
    private void checkAnnotationOperation(int i)
    {
        switch (i) {
            case CREATE_ANNOTATION:
            case UPDATE_ANNOTATION:
            case DELETE_ANNOTATION:    
                break;

            default:
                throw new IllegalArgumentException("Annotation operation not " +
                        "supported");
        }
    }
    
    /**
     * Controls if the specified data object operation is supported.
     * 
     * @param i The index to control.
     */
    private void checkDataObjectOperation(int i)
    {
        switch (i) {
            case CREATE_OBJECT:
            case UPDATE_OBJECT:  
                break;
            default:
                throw new IllegalArgumentException("DataObject operation not " +
                        "supported");
        }
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    EditorComponent(EditorModel model)
    {
        if (model == null) throw new NullPointerException("No model."); 
        this.model = model;
        controller = new EditorControl(this);
        view = new EditorUI();
    }
    
    /** Links up the MVC triad. */
    void initialize()
    {
        controller.initialize(view);
        view.initialize(controller, model);
        model.getParentModel().addPropertyChangeListener(
                TreeViewer.THUMBNAIL_LOADED_PROPERTY, controller);
    }
    
    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getState()
     */
    public void activate()
    {
        switch (model.getState()) {
            case NEW:
                if (model.getEditorType() == PROPERTIES_EDITOR) {
                    if (model.isAnnotable()) {
                        model.fireAnnotationsLoading();
                        fireStateChange();
                    }
                }
                break;
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
        }   
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#discard()
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
     * @see Editor#setAnnotations(Map)
     */
    public void setAnnotations(Map map)
    {
        if (model.getState() != LOADING_ANNOTATION)
            throw new IllegalStateException("This method can only be invoked" +
                    " in the LOADING_ANNOTATION state.");
        if (map == null) throw new IllegalArgumentException("No annotations.");
        model.setAnnotations(map);
        view.showAnnotations();
        if (model.hasThumbnail())
            firePropertyChange(TreeViewer.THUMBNAIL_LOADING_PROPERTY, null, 
                                model.getHierarchyObject());
        model.setState(READY);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setThumbnail(BufferedImage)
     */
    public void setThumbnail(BufferedImage thumbnail)
    {
        if (model.getState() != DISCARDED) {
            if (thumbnail == null)
                throw new IllegalArgumentException("No thumbnail.");
            view.setThumbnail(thumbnail);
            model.setState(READY);
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setRetrievedClassification(Set)
     */
    public void setRetrievedClassification(Set paths)
    {
        if (model.getState() != LOADING_CLASSIFICATION)
            throw new IllegalStateException("This method should only be " +
                    "invoked in the LOADING_CLASSIFICATION state.");
        if (paths == null)
            throw new IllegalArgumentException("No paths to set.");
        Set set = TreeViewerTranslator.transformHierarchy(paths);
        model.setClassifications(set);
        view.showClassifications();
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#cancel()
     */
    public void cancel()
    {
        if (model.getState() != DISCARDED) {
            model.cancel();
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#getUI()
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
     * @see Editor#close()
     */
    public void close()
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        firePropertyChange(CLOSE_EDITOR_PROPERTY, Boolean.FALSE, Boolean.TRUE);
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#setSaveResult(DataObject, int)
     */
    public void setSaveResult(DataObject object, int operation)
    {
        if (model.getState() != SAVE_EDITION)
            throw new IllegalStateException(
                    "This method can be invoked in the SAVE_EDITION state.");
        checkDataObjectOperation(operation);
        HashMap map = new HashMap(1);
        map.put(new Integer(operation), object);
        firePropertyChange(TreeViewer.SAVE_EDITION_PROPERTY, null, map);
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#saveObject(DataObject, int)
     */
    public void saveObject(DataObject object, int operation)
    {
        switch (model.getState()) {
            case DISCARDED:
            case LOADING_ANNOTATION:   
            case LOADING_CLASSIFICATION:  
                throw new IllegalStateException(
                "This method cannot be invoked in the DISCARDED, " +
                "LOADING_ANNOTATION or LOADING_CLASSIFICATION state.");
        }
        if (object == null)
            throw new IllegalArgumentException("No DataObject.");
        checkDataObjectOperation(operation);
        switch (operation) {
            case CREATE_OBJECT:
                model.fireDataObjectCreation(object);
                break;
            case UPDATE_OBJECT:
                model.fireDataObjectUpdate(object);
                break;
        }
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#saveAnnotation(AnnotationData, int)
     */
    public void saveAnnotation(AnnotationData object, int operation)
    {
        switch (model.getState()) {
            case DISCARDED:
            case LOADING_ANNOTATION:   
            case LOADING_CLASSIFICATION:  
                throw new IllegalStateException(
                "This method cannot be invoked in the DISCARDED, " +
                "LOADING_ANNOTATION or LOADING_CLASSIFICATION state.");
        }
        if (object == null)
            throw new IllegalArgumentException("No annotation.");
        checkAnnotationOperation(operation);
        switch (operation) {
            case CREATE_ANNOTATION:
                model.fireAnnotationCreate(object);
                break;
            case UPDATE_ANNOTATION:
                model.fireAnnotationUpdate(object);
                break;
            case DELETE_ANNOTATION:
                model.fireAnnotationDelete(object);
        }
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Editor} interface.
     * @see Editor#saveObjectAndAnnotation(DataObject, AnnotationData, int)
     */
    public void saveObjectAndAnnotation(DataObject data, AnnotationData object,
            int operation)
    {
        switch (model.getState()) {
            case DISCARDED:
            case LOADING_ANNOTATION:   
            case LOADING_CLASSIFICATION:  
                throw new IllegalStateException(
                "This method cannot be invoked in the DISCARDED, " +
                "LOADING_ANNOTATION or LOADING_CLASSIFICATION state.");
        }
        if (object == null)
            throw new IllegalArgumentException("No annotation.");
        if (data == null)
            throw new IllegalArgumentException("No DataObject.");
        checkAnnotationOperation(operation);
        switch (operation) {
            case CREATE_ANNOTATION:
                model.fireAnnotationCreate(data, object);
                break;
            case UPDATE_ANNOTATION:
                model.fireAnnotationUpdate(data, object);
                break;
            case DELETE_ANNOTATION:
                model.fireAnnotationDelete(data, object);
        }
        fireStateChange();
    }
    
}
