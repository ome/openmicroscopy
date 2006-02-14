/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer
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

package org.openmicroscopy.shoola.agents.treeviewer.view;


//Java imports
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.AnnotationData;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;


/** 
 * Defines the interface provided by the tree viewer component.
 * The tree viewer provides a top-level window to host different types of
 * hierarchy display and let the user interact with it.
 * A display is a view with a visualization tree. A visualization tree
 * is a graphical tree that represents objects in a given  <i>OME</i> hierarchy,
 * like Project/Dataset/Image, Category Group/Category/Image or simply Images.
 * The component follows the <code>Lazy loading rule</code> i.e. the leaves 
 * of a given hierarchy are only retrieved if the parent is selected.
 * In practise, this means that we only display a Project/Dataset hierarchy
 * if a given Dataset is selected,  then the images in this Dataset are 
 * retrieved.
 * <p>The typical life-cycle of a tree viewer is as follows. The object
 * is first created using the {@link TreeViewerFactory}, the
 * {@link Browser}s hosting a hierarchy view are created. After
 * creation the object is in the {@link #NEW} state and is waiting for the
 * {@link #activate() activate} method to be called.
 * The data retrieval happens in the {@link Browser}.
 * 
 * When the user quits the window, the {@link #discard() discard} method is
 * invoked and the object transitions to the {@link #DISCARDED} state.
 * At which point, all clients should de-reference the component to allow for
 * garbage collection.
 * 
 * </p>
 *
 * @see Browser
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public interface TreeViewer
    extends ObservableComponent
{

    /** Flag to denote the <i>New</i> state. */
    public static final int         NEW = 1;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int         DISCARDED = 2;
    
    /** Flag to denote the <i>Loading Annotation</i> state. */
    public static final int         LOADING_ANNOTATION = 3;
    
    /** Flag to denote the <i>Loading Thumbnail</i> state. */
    public static final int         LOADING_THUMBNAIL = 4;
    
    /** Flag to denote the <i>Loading Classification</i> state. */
    public static final int         LOADING_CLASSIFICATION = 5;
    
    /** Flag to denote the <i>Loading Classification path</i> state. */
    public static final int         LOADING_CLASSIFICATION_PATH = 6;
    
    /** Flag to denote the <i>Save Edition</i> state. */
    public static final int         SAVE_EDITION = 7;
    
    /** Flag to denote the <i>Save Edition</i> state. */
    public static final int         SAVE_CLASSIFICATION = 8;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int         READY = 9;
    
    /** Identifies the <code>Create</code> type for the editor. */
    public static final int         CREATE_EDITOR = 100;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         PROPERTIES_EDITOR = 101;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         CLASSIFIER_EDITOR = 102;
    
    /** Identifies the <code>No Editor</code> type for the editor. */
    public static final int         NO_EDITOR = 103;
    
    /** 
     * Indicates that the root of the retrieved hierarchy is the 
     * <code>World</code>. 
     */
    public static final int			WORLD_ROOT = 200;
    
    /** 
     * Indicates that the root of the retrieved hierarchy is an OME 
     * <code>Group</code>.
     */
    public static final int			GROUP_ROOT = 201;
    
    /** 
     * Indicates that the root of the retrieved hierarchy is a
     * <code>User</code>. 
     */
    public static final int			USER_ROOT = 202;
    
    /** 
     * Indicates to create an annotation  when editing the 
     * <code>DataObject</code>.
     */
    public static final int			CREATE_ANNOTATION = 100;
    
    /** 
     * Indicates to update an annotation  when editing the 
     * <code>DataObject</code>.
     */
    public static final int			UPDATE_ANNOTATION = 101;
    
    /** 
     * Indicates to delete an annotation  when editing the 
     * <code>DataObject</code>.
     */
    public static final int			DELETE_ANNOTATION = 102;
    
    /** 
     * Indicates that no-operation for the annotation when editing the 
     * <code>DataObject</code>.
     */
    public static final int			NO_ANNOTATION_OP = 103;
    
    /** Identifies the <code>Create Object</code> operation. */
    public static final int			CREATE_OBJECT = 300;
    
    /** Identifies the <code>Update Object</code> operation. */
    public static final int			UPDATE_OBJECT = 301;
    
    /** Identifies the <code>Delete Object</code> operation. */
    public static final int			DELETE_OBJECT = 302;
    
    /** 
     * Bound properties indicating that {@link Browser} is selected or 
     * <code>null</code> if no there is no {@link Browser} currently selected.
     * 
     */
    public static final String		SELECTED_BROWSER_PROPERTY = 
        									"selectedBrowser";
    /**
     * Bound properties to indicate to remove the currently displayed editor.
     */
    public static final String      REMOVE_EDITOR_PROPERTY = "removeEditor";
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Starts the initialization sequence when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Returns the available {@link Browser}s.
     * 
     * @return See above.
     */
    public Map getBrowsers();
    
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /** 
     * Adds the {@link Browser} corresponding to the specified type to
     * the display.
     * 
     * @param browserType The browser's type.
     */
    public void addBrowser(int browserType);
    
    /**
     * Brings up the editor corresponding to the specified type ang object.
     * 
     * @param object The {@link DataObject} to edit or create.
     * @param editorType The type of editor.
     */
    public void showProperties(DataObject object, int editorType);
    
    /**
     * Saves the specified {@link DataObject} depending on the speficied
     * algorithm.
     * 
     * @param object 	The {@link DataObject} to save.
     * @param algorithm Identifies the type of operations to perform.
     * 					One of the folllowing constants: 
     * 					{@link TreeViewer#CREATE_OBJECT},
     * 					{@link TreeViewer#UPDATE_OBJECT},
     * 					{@link TreeViewer#DELETE_OBJECT}.
     */
    public void saveObject(DataObject object, int algorithm);
    
    /**
     * Updates the specified <code>DataObject</code>, and creates/updates or 
     * deletes the specified <code>Annotation</code> depending on the specified
     * algorithm.
     * @param object		The {@link DataObject} to save.
     * @param annotation 	The {@link AnnotationData} to handle. 
     * @param algorithm 	Identifies the type of operations to perform.
     * 						One of the folllowing constants: 
     * 						{@link TreeViewer#CREATE_ANNOTATION},
     * 						{@link TreeViewer#UPDATE_ANNOTATION},
     * 						{@link TreeViewer#DELETE_ANNOTATION}.
     */
    public void saveObject(DataObject object, AnnotationData annotation,
            				int algorithm);
    
    /** Cancels any ongoing data loading. */
    public void cancel();
    
    /** Removes the displayed editor from the panel. */
    public void removeEditor();
    
    /** 
     * Sets the results of a save action i.e. delete or update 
     * operation. 
     * 
     * @param object    The <code>DataObject</code> to update or to delete.
     * @param op        The type of operation performed, either
     *                  {@link #DELETE_OBJECT}, {@link #UPDATE_OBJECT} or 
     *                  {@link #CREATE_OBJECT}.
     */
    public void setSaveResult(DataObject object, int op);
    
    /**
     * Returns the user's details. Helper method
     * 
     * @return See above.
     */
    public ExperimenterData getUserDetails();
    
    /**
     * Shows if the passed parameter is <code>true</code>, hides
     * otherwise.
     * 
     * @param b <code>true</code> to show the component, <code>false</code>
     * 			to hide.
     */
    public void showFinder(boolean b);
    
    /** Hides the window and cancels any on-going data loading. */
    public void closing();
    
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
    public void setDataObjectThumbnail(BufferedImage thumbnail);
    
    /**
     * Retrieves the CategoryGroup/Categories containing the specified 
     * image.
     *  
     * @param imageID The id of the image.
     */
    public void retrieveClassification(int imageID);
    
    /**
     * Sets the retrieved classfication paths.
     * 
     * @param paths The paths to set.
     */
    public void setRetrievedClassification(Set paths);
    
    /**
     * Browses the specified data object.
     * 
     * @param object The object to browse.
     */
    public void browse(DataObject object);
    
    /**
     * Sets the available classifications paths.
     * 
     * @param mode  The type of classifier.
     * @param paths The paths to set.
     */
    public void setClassificationPaths(int mode, Set paths);
    
    /**
     * Brings up the classifier corresponding to the specified mode.
     * 
     * @param object The image object to classify or declassify.
     * @param mode The type of classifier. 
     */
    public void showClassifier(ImageData object, int mode);
    
    /**
     * Adds the currently selected image to selected categories.
     * 
     * @param map   Collection of selected categories, the key is the
     *              <code>image</code>, the value is the set of categories.
     */
    public void classifyImage(Map map);
    
    /**
     * Removes the currently selected image from selected categories.
     * 
     * @param map   Collection of selected categories, the key is the
     *              <code>image</code>, the value is the set of categories.
     */
    public void declassifyImage(Map map);
    
    /**
     * Indicates that the classification was successful or not and removes
     * the UI classification component from the display.
     * 
     * @param b Passed <code>true</code> if the save action was successful.
     *          <code>false</code> otherwise.
     */
    public void saveClassification(boolean b);
    
    /**
     * Returns the currently selected {@link Browser} or <code>null</code>
     * if no {@link Browser} is selected.
     * 
     * @return See above.
     */
    Browser getSelectedBrowser();
    
    /**
     * Sets the currently selected {@link Browser} or <code>null</code>
     * if no {@link Browser} is selected.
     * 
     * @param browser The currently selected {@link Browser}.
     */
    void setSelectedBrowser(Browser browser);
    
}
