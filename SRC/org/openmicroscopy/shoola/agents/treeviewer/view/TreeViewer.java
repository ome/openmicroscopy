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
import java.awt.Component;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JDialog;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
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
    
    /** Flag to denote the <i>Save Edition</i> state. */
    public static final int         SAVE = 3;
    
    /** Flag to denote the <i>Loading Thumbnail</i> state. */
    public static final int         LOADING_THUMBNAIL = 4;
    
    /** Flag to denote the <i>Loading Data</i> state. */
    public static final int         LOADING_DATA = 5;
    
    /** Flag to denote the <i>Loading Data</i> state. */
    public static final int         DIALOG_SELECTION = 6;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int         READY = 7;
    
    /** Identifies the <code>Create</code> type for the editor. */
    public static final int         CREATE_EDITOR = 100;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         PROPERTIES_EDITOR = 101;
    
    /** Identifies the <code>Edit</code> type for the editor. */
    public static final int         CLASSIFIER_EDITOR = 102;
    
    /** Identifies the <code>No Editor</code> type for the editor. */
    public static final int         NO_EDITOR = 103;
    
    /** 
     * Indicates that the root of the retrieved hierarchy is an OME 
     * <code>Group</code>.
     */
    public static final int			GROUP_ROOT = 200;
    
    /** 
     * Indicates that the root of the retrieved hierarchy is a
     * <code>User</code>. 
     */
    public static final int			USER_ROOT = 201;
    
    /** Identifies the <code>Delete Object</code> operation. */
    public static final int         REMOVE_OBJECT = 302;
    
    /** Identifies the <code>Manager</code> menu. */
    public static final int         MANAGER_MENU = 0;
    
    /** Identifies the <code>Manager</code> menu. */
    public static final int         CLASSIFIER_MENU = 1;
    
    /** Bounds property to indicate that the data retrieval is cancelled. */
    public static final String      CANCEL_LOADING_PROPERTY = "cancelLoading";
    
    /** Bounds property to indicate to load a thumbnail for a given image. */
    public static final String      THUMBNAIL_LOADING_PROPERTY = 
                                                    "thumbnailLoading";
    
    /** Bounds property to indicate that the thumbanil is loaded. */
    public static final String      THUMBNAIL_LOADED_PROPERTY = 
                                                    "thumbnailLoaded";
    
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
    
    /** Bound property name indicating to set the filters nodes.  */
    public static final String      FILTER_NODES_PROPERTY = "filterNodes";
    
    /** 
     * Bound property name indicating to show/hide the component from the
     * display.
     */
    public static final String      FINDER_VISIBLE_PROPERTY = "finderVisible";
    
    /** Bound property indicating to change the root of the hierarchy. */
    public static final String      HIERARCHY_ROOT_PROPERTY = "hierarchyRoot";
    
    /** Bound property indicating to state of the components has changed. */
    public static final String      ON_COMPONENT_STATE_CHANGED_PROPERTY = 
                                        "OnComponentStateChanged";
    /** 
     * The title displayed in the {@link LoadingWindow} during the saving 
     * process.
     */
    public static final String      SAVING_TITLE = "Saving Data";
    
    /** 
     * The title displayed in the {@link LoadingWindow} during the saving 
     * process.
     */
    public static final String      LOADING_TITLE = "Loading Data";
    
    /** Identifies the <code>Create Object</code> operation. */
    public static final int         CREATE_OBJECT = 300;
    
    /** Identifies the <code>Update Object</code> operation. */
    public static final int         UPDATE_OBJECT = 301;
    
    /** Identifies the <code>Update Object</code> operation. */
    public static final int         DELETE_OBJECT = 302;
    
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
    
    /**
     * Retrieves the thumbnail for the specified image.
     * 
     * @param image The image the thumbnail is for.
     */
    void retrieveThumbnail(ImageData image);
    
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
     * Adds or removes the {@link Browser} corresponding to the specified type
     * to the display depending on the actual status of the browseer.
     * 
     * @param browserType The browser's type.
     */
    public void displayBrowser(int browserType);
    
    /**
     * Brings up the editor corresponding to the specified type ang object.
     * 
     * @param object The {@link DataObject} to edit or create.
     * @param editorType The type of editor. One of the following constants
     * {@link #CREATE_EDITOR} or {@link #PROPERTIES_EDITOR}.
     */
    public void showProperties(DataObject object, int editorType);
    
    /**
     * Removes the {@link DataObject} hosted by the passed node.
     * 
     * @param object The node hosting the {@link DataObject} to remove.
     */
    public void removeObject(TreeImageDisplay object);
    
    /**
     * Removes the {@link DataObject}s hosted by the passed nodes.
     * 
     * @param nodes The nodes hosting the {@link DataObject}s to remove.
     */
    public void removeObjects(List nodes);
    
    /** Cancels any ongoing data loading. */
    public void cancel();
    
    /** Removes the displayed editor from the panel. */
    public void removeEditor();

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
    public void closeWindow();
    
    /**
     * Brings up the classifier corresponding to the specified mode.
     * 
     * @param images    The images to classify or declassify depending on the
     *                  given mode.
     * @param mode      The type of classifier. One of the following constants:
     *                  {@link Classifier#CLASSIFY_MODE} or
     *                  {@link Classifier#DECLASSIFY_MODE}.
     */
    public void showClassifier(ImageData[] images, int mode);

    /**
     * Returns the dialog indicating a data retrieval.
     * 
     * @return See above.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public JDialog getLoadingWindow();

    /**
     * Sets the thumbnail associated to the currently edited Image.
     * 
     * @param thumbnail The thumbnail to set.
     */
    public void setThumbnail(BufferedImage thumbnail); 
    
    /**
     * Reacts to a node selection in the currently selected {@link Browser}.
     */
    public void onSelectedDisplay();
    
    /**
     * Updates the views when the data object is saved.
     * The method only supports map of size one. 
     * The key is one the following constants: 
     * <code>CREATE_OBJECT</code>, <code>UPDATE_OBJECT</code> or
     * <code>REMOVE_OBJECT</code>.
     * The value is the <code>DataObject</code> created, removed or updated.
     * 
     * @param data      The save <code>DataObject</code>. Mustn't be 
     *                  <code>null</code>.
     * @param operation The type of operation.
     */
    public void onDataObjectSave(DataObject data, int operation);
    
    /** Refreshes the view when the nodes have been removed. */
    public void onNodesRemoved();
    
    /**
     * 
     * Updates the view when the image has been classified or declassified.
     * 
     * @param images        The image classified or declassified. Mustn't 
     *                      be <code>null</code>.
     * @param categories    The categories the image was added to or 
     *                      removed from. Mustn't be <code>null</code>.
     * @param mode          The type of operation i.e. classification or 
     *                      declassification.
     */
    public void onImageClassified(ImageData[] images, Set categories, int mode);
    
    /** Clears the result of a previous find action. */
    public void clearFoundResults();

    /**
     * Moves the window to the back.
     * @throws IllegalStateException If the current state is not
     *                               {@link #DISCARDED}.
     */
    public void moveToBack();
    
    /**
     * Moves the window to the front.
     * @throws IllegalStateException If the current state is not
     *                               {@link #DISCARDED}.
     */
    public void moveToFront();
    
    /**
     * Returns the id of the group, the current user is using as the logging
     * group. By default, the method returns the default group. If the
     * user belongs to more than one group, the method returns the 
     * currently selected group.
     * 
     * @return See above.
     */
    public long getRootGroupID();

    /**
     * Returns of the following constants: 
     * {@link #GROUP_ROOT} or {@link #USER_ROOT}.
     * 
     * @return See above.
     */
    public int getRootLevel();
    
    /**
     * Sets the root of the retrieved hierarchies. 
     * The rootID is taken into account if and only if the passed 
     * <code>rootLevel</code> is {@link #GROUP_ROOT}.
     * 
     * @param rootLevel The level of the root. One of the following constants:
     *                  {@link #GROUP_ROOT} and {@link #USER_ROOT}.
     * @param rootID    The Id of the root.
     */
    public void setHierarchyRoot(int rootLevel, long rootID);
    
    /**
     * Returns <code>true</code> if the specified data object is readable,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho    The data object to check.
     * @return See above.
     */
    public boolean isObjectWritable(DataObject ho);
    
    /** 
     * Adds existing objects to the currently selected node. 
     * 
     * @param ho The node the objects are added to.
     */
    public void addExistingObjects(DataObject ho);
    
    /**
     * Displays the collection of existing nodes.
     * 
     * @param nodes The nodes to display.
     */
    public void setExistingObjects(Set nodes);

    /**
     * Adds the specified notes to the tree.
     * 
     * @param set The nodes to add.
     */
    public void addExistingObjects(Set set);

    /** Navigates into the selected node or displays the main tree. */
    public void navigate();
    
    /**
     * Brings up the menu on top of the specified component at 
     * the specified location.
     * 
     * @param menuID    The id of the menu. One out of the following constants:
     *                  {@link #MANAGER_MENU}, {@link #CLASSIFIER_MENU}.
     * @param c         The component that requested the popup menu.
     * @param p         The point at which to display the menu, relative to the
     *                  <code>component</code>'s coordinates.
     */
    public void showMenu(int menuID, Component c, Point p);
    
    /**
     * Sets the text in the status bar and modifies display.
     * 
     * @param enable    Pass <code>true</code> to allow cancellation, 
     *                  <code>false</code> otherwise.
     * @param text      The text to display.
     * @param hide      Pass <code>true</code> to hide the progress bar.
     *                  <code>false</code> otherwise.
     */
    public void setStatus(boolean enable, String text, boolean hide);
    
    /**
     * Enables the components composing the display depending on the specified
     * parameter.
     * 
     * @param b Pass <code>true</code> to enable the component, 
     *          <code>false</code> otherwise.
     */
    public void onComponentStateChange(boolean b);
    
}
