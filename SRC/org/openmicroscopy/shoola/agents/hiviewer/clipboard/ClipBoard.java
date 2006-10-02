/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard;

//Java imports
import java.awt.Point;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindData;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;

/** 
 * Defines the interface provided by clip board component.
 * The clip board provides a UI component hosting the <code>ImageDisplay</code>
 * manipulation e.g. <code>Search</code> panel, <code>Annotation</code> panel.
 * Use the {@link ClipBoardFactory} to create an object implementing this 
 * interface.
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface ClipBoard
    extends ObservableComponent
{
    
    /** Identifies the index of the <code>Find</code> panel. */
    public static final int     FIND_PANE = 0;
    
    /** Identifies the index of the <code>Annotation</code> pane. */
    public static final int     ANNOTATION_PANE = 1;
    
    /** Identifies the index of the <code>Information</code> pane. */
    public static final int     EDITOR_PANE = 2;
    
    /** Identifies the index of the <code>Information</code> pane. */
    public static final int     INFO_PANE = 3;
    
    /** Identifies the <i>Loading annotations</i> state. */
    public static final int     LOADING_ANNOTATIONS = 200;
    
    /** Identifies the <i>Edit annotations</i> state. */
    public static final int     EDIT_ANNOTATIONS = 201;
    
    /** Identifies the <i>Ready</i> state. */
    public static final int     ANNOTATIONS_READY = 202;
    
    /** Identifies the <i>Discarded annotations</i> state. */
    public static final int     DISCARDED_ANNOTATIONS = 203;
    
    /** Indicates to retrieve the image annotations. */
    public static final int     IMAGE_ANNOTATIONS = 300;
    
    /** Indicates to retrieve the dataset annotations. */
    public static final int     DATASET_ANNOTATIONS = 301;
    
    /** Identifies to create a new annotation. */
    public static final int     CREATE_ANNOTATION = 100;
    
    /** Identifies to update the currently edited annotation. */
    public static final int     UPDATE_ANNOTATION = 101;
    
    /** Identifies to delete the currently edited annotation. */
    public static final int     DELETE_ANNOTATION = 102;
    
    /** Any ongoing data loading is cancelled. */
    public void discard();
    
    /**
     * Returns the UI representation of this component. 
     * 
     * @return See above.
     */
    public JComponent getUI();
    
    /** 
     * Sets the results of the search.
     * 
     * @param foundNodes The set of found nodes.
     */
    public void setFindResults(Set foundNodes);
    
    /**
     * Sets the annotations retrieved.
     * 
     * @param map The annotations.
     */
    public void setAnnotations(Map map);
    
    /**
     * Retrieves the annotations for the specified object.
     * 
     * @param object    The annotated <code>DataObject</code>.
     *                  Mustn't be <code>null</code>.
     */
    public void retrieveAnnotations(DataObject object);

    /**
     * Creates, updates or deletes the annotation depending on the specified
     * index. 
     * @param data  The annotation to edit. Mustn't be <code>null</code>.
     * @param index One of the following constants: {@link #CREATE_ANNOTATION},
     *              {@link #UPDATE_ANNOTATION} or {@link #DELETE_ANNOTATION}.
     */
    public void editAnnotation(AnnotationData data, int index);
    
    /**
     * Transitions the viewer to the {@link #DISCARDED_ANNOTATIONS} state.
     * Any ongoing data loading is cancelled.
     */
    public void discardAnnotation();
    
    /**
     * Sets the result of the annotation edition.
     * 
     * @param object The annotated object. Mustn't be <code>null</code>.
     */
    public void setAnnotationEdition(DataObject object);

    /**
     * Sets the selected {@link ClipBoardPane}.
     * 
     * @param index The index of the {@link ClipBoardPane}. One of the following
     *              constants: {@link #FIND_PANE}, {@link #ANNOTATION_PANE}.
     * @param node  Passed <code>null</code> to modify the diplay.
     */
    public void setSelectedPane(int index, ImageDisplay node);

    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Brings up the popup menu for the specified {@link ImageDisplay} node.
     * 
     * @param invoker   The component in whose space the popup menu is to
     *                  appear.
     * @param p         The coordinate in invoker's coordinate space at which 
     *                  the popup menu is to be displayed.
     * @param node      The {@link ImageDisplay} object.                 
     */
    public void showMenu(JComponent invoker, Point p, ImageDisplay node);
    
    /**
     * Returns the current user's details. Helper method
     * 
     * @return See above.
     */
    public ExperimenterData getUserDetails();
    
    /**
     * Returns the ordered retrieved annotations.
     * 
     * @return See above.
     */
    public Map getAnnotations();
    
    /**
     * Returns the annotation <code>DataObject</code> for the current user.
     * 
     * @return See above.
     */
    public AnnotationData getUserAnnotationData();
    
    /**
     * Returns the index of the currently selected {@link ClipBoardPane}. 
     * One of the following constants: {@link #FIND_PANE},
     * {@link #ANNOTATION_PANE}.
     * 
     * @return See above.
     */
    public int getSelectedPaneIndex();
    
    /** Clears the results of a previous find action. */
    public void clear();
    
    /**
     * Finds the patterns in the browser depending on the context.
     * 
     * @param p             The pattern to find. Mustn't be <code>null</code>.
     * @param findContext   The context of the find action. 
     *                      Mustn't be <code>null</code>.
     */
    public void find(Pattern p, FindData findContext);
    
    /**
     * Returns the currently selected hierarchy object, <code>null</code>
     * if no node selected.
     * 
     * @return See above.
     */
    public Object getHierarchyObject();

    /**
     * Returns <code>true</code> if the specified data object is readable,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho    The data object to check.
     * @return See above.
     */
    public boolean isObjectWritable(DataObject ho);

    /** 
     * Saves the specified data object.
     * 
     * @param object The object to save.
     */
    public void saveObject(DataObject object);

    /**
     * Displays the properties of the currently selected node.
     * 
     * @param object The Data object to edit.
     */
    public void showProperties(DataObject object);
    
}

