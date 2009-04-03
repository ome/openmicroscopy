/*
 * org.openmicroscopy.shoola.agents.treeviewer.editors.Editor
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
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.DataObject;


/** 
 * The component hosting the editor view.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface Editor
    extends ObservableComponent
{
    
    /** Flag to denote the <i>New</i> state. */
    public static final int         NEW = 1;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int         DISCARDED = 2;
    
    /** Flag to denote the <i>Loading Annotation</i> state. */
    public static final int         LOADING_ANNOTATION = 3;
    
    /** Flag to denote the <i>Loading Classification</i> state. */
    public static final int         LOADING_CLASSIFICATION = 4;
    
    /** Flag to denote the <i>Save Edition</i> state. */
    public static final int         SAVE_EDITION = 5;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int         READY = 6;
    
    /** Identifies the <code>Create</code> type for the editor. */
    public static final int         CREATE_EDITOR = TreeViewer.CREATE_EDITOR;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         PROPERTIES_EDITOR = 
                                                TreeViewer.PROPERTIES_EDITOR;
    
    /** Identifies the <code>Create Object</code> operation. */
    public static final int         CREATE_OBJECT = 300;
    
    /** Identifies the <code>Update Object</code> operation. */
    public static final int         UPDATE_OBJECT = 301;
    
    /** Identifies the <code>Update Object</code> operation. */
    public static final int         DELETE_OBJECT = 302;
    
    /** 
     * Indicates to create an annotation  when editing the 
     * <code>DataObject</code>.
     */
    public static final int         CREATE_ANNOTATION = 100;
    
    /** 
     * Indicates to update an annotation  when editing the 
     * <code>DataObject</code>.
     */
    public static final int         UPDATE_ANNOTATION = 101;
    
    /** 
     * Indicates to delete an annotation  when editing the 
     * <code>DataObject</code>.
     */
    public static final int         DELETE_ANNOTATION = 102;
    
    /** 
     * Indicates that no-operation for the annotation when editing the 
     * <code>DataObject</code>.
     */
    public static final int         NO_ANNOTATION_OP = 103;
    
    /** Bounds property to indicate to close the {@link Editor}. */
    public static final String      CLOSE_EDITOR_PROPERTY = "closeEditor";
    
    /** 
     * Closes the {@link Editor}. 
     * 
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    void close();
    
    /**
     * Creates or updates the specified <code>DataObject</code> depending
     * on the specified operation.
     * 
     * @param object    The object to handle.
     * @param operation The type of operation. One of the following constants:
     *                  {@link #CREATE_OBJECT} or {@link #UPDATE_OBJECT}.
     */
    void saveObject(DataObject object, int operation);
    
    /**
     * Updates the specified <code>DataObject</code> and creates, updates or
     * deletes the specified annotation depending on the specified operation.
     * 
     * @param data      The object to update.
     * @param object    The annotation to handle.
     * @param operation The type of operation. One of the following constants:
     *                  {@link #CREATE_ANNOTATION}, {@link #UPDATE_ANNOTATION}
     *                  or {@link #DELETE_ANNOTATION}.
     */
    void saveObjectAndAnnotation(DataObject data, AnnotationData object, 
                                int operation);

    /** Reloads the classifications. */
    void reloadClassifications();
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the progress window on screen.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Transitions the classifier to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /**
     * Sets the annotations retrieved.
     * 
     * @param map The annotations.
     */
    public void setAnnotations(Map map);
    
    /**
     * Sets the thumbnail associated to the currently edited Image.
     * 
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(BufferedImage thumbnail);
    
    /**
     * Sets the retrieved classfication paths.
     * 
     * @param paths The paths to set.
     */
    public void setRetrievedClassification(Set paths);

    /** Cancels any ongoing data loading. */
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
     * Sets the result of the save operation.
     * 
     * @param object    The <code>DataObject</code> saved.
     * @param operation The type of save i.e. <code>CREATE</code>, 
     *                  <code>UPDATE</code> or <code>DELETE</code>.
     */
    public void setSaveResult(DataObject object, int operation);
    
    /**
     * Sets the images' annotations. This method should be invoked 
     * if the edited object contains images i.e. if the container is a
     * <code>Dataset</code> or a <code>Category</code>.
     * 
     * @param annotations   The annotations to set.
     */
    public void setLeavesAnnotations(Map annotations);
    
}
