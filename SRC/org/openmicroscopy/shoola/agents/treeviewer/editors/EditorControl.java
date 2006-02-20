/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.EditorControl
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.AnnotationData;
import pojos.DataObject;


/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class EditorControl
    implements ChangeListener, PropertyChangeListener
{

    /** 
     * Reference to the {@link Editor} component, which, in this context,
     * is regarded as the Model.
     */
    private Editor      model;
    
    /** Reference to the View. */
    private EditorUI    view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize(EditorUI) initialize} method 
     * should be called straight 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link Editor} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    EditorControl(Editor model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
    }
    
    /**
     * Links this Controller to its View.
     * 
     * @param view   Reference to the View.  Mustn't be <code>null</code>.
     */
    void initialize(EditorUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        model.addChangeListener(this);
    }
    
    /**
     * Browses or views the specified <code>DataObject</code>.
     * 
     * @param object The object to browse or view.
     */
    void browse(DataObject object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No object to browse.");
    }

    /**
     * Closes the {@link Editor}.
     * 
     * @param b Passed <code>true</code> to close the {@link Editor}. 
     */
    void close(boolean b)
    {
        if (b) model.close();
    }

    /**
     * Creates the specified <code>DataObject</code>.
     * 
     * @param object The object to create.
     */
    void createObject(DataObject object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No Data object to create.");
        model.saveObject(object, Editor.CREATE_OBJECT);
    }
    
    /**
     * Updates the specified <code>DataObject</code>.
     * 
     * @param object The object to update.
     */
    void updateObject(DataObject object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No Data object to update.");
        model.saveObject(object, Editor.UPDATE_OBJECT);
    }
    
    /**
     * Creates a new annotation.
     * 
     * @param object The annotation to create.
     */
    void createAnnotation(AnnotationData object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No annotation to create.");
        model.saveAnnotation(object, Editor.CREATE_ANNOTATION);
    }
    
    /**
     * Updates the specified annotation.
     * 
     * @param object The annotation to update.
     */
    void updateAnnotation(AnnotationData object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No annotation to update.");
        model.saveAnnotation(object, Editor.UPDATE_ANNOTATION);
    }
    
    /**
     * Rmoves the specified annotation.
     * 
     * @param object The annotation to remove.
     */
    void deleteAnnotation(AnnotationData object)
    {
        if (object == null) 
            throw new IllegalArgumentException("No annotation to delete.");
        model.saveAnnotation(object, Editor.DELETE_ANNOTATION);
    }
  
    /**
     * Updates the specified <code>DataObject</code> and creates a annotation 
     * for this object.
     * 
     * @param data      The object to update.
     * @param object    The annotation to create.
     */
    void createAnnotation(DataObject data, AnnotationData object)
    {
        if (data == null) 
            throw new IllegalArgumentException("No Data object to update.");
        if (object == null) 
            throw new IllegalArgumentException("No annotation to create.");
        model.saveObjectAndAnnotation(data, object, Editor.CREATE_ANNOTATION);
    }
    
    /**
     * Updates the specified <code>DataObject</code> and updates the specified 
     * annotation..
     * 
     * @param data      The object to update.
     * @param object    The annotation to update.
     */
    void updateAnnotation(DataObject data, AnnotationData object)
    {
        if (data == null) 
            throw new IllegalArgumentException("No Data object to update.");
        if (object == null) 
            throw new IllegalArgumentException("No annotation to update.");
        model.saveObjectAndAnnotation(data, object, Editor.UPDATE_ANNOTATION);
    }
    
    /**
     * Updates the specified <code>DataObject</code> and removes the specified 
     * annotation..
     * 
     * @param data      The object to update.
     * @param object    The annotation to remove.
     */
    void deleteAnnotation(DataObject data, AnnotationData object)
    {
        if (data == null) 
            throw new IllegalArgumentException("No Data object to update.");
        if (object == null) 
            throw new IllegalArgumentException("No annotation to remove.");
        model.saveObjectAndAnnotation(data, object, Editor.DELETE_ANNOTATION);
    }
    
    /**
     * Reacts to state changes in the {@link Editor}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        switch (model.getState()) {
            case Editor.SAVE_EDITION: 
                TreeViewerFactory.getLoadingWindow().setTitle(
                                TreeViewer.SAVING_TITLE);
            case Editor.LOADING_ANNOTATION:
            case Editor.LOADING_CLASSIFICATION: 
                UIUtilities.centerAndShow(TreeViewerFactory.getLoadingWindow());
                break;
            case Editor.READY:
            case Editor.DISCARDED:
                TreeViewerFactory.getLoadingWindow().setVisible(false);
                break;
        }
    }

    /**
     * Reacts to the {@link TreeViewer#THUMBNAIL_LOADED_PROPERTY}
     * property changes fired by the <code>TreeViewer</code>.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
        if (name.equals(TreeViewer.THUMBNAIL_LOADED_PROPERTY))
            model.setThumbnail((BufferedImage) pce.getNewValue());   
    }
 
}
