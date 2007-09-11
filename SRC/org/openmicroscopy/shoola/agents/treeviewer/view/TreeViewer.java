/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
import javax.swing.JFrame;

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
    
    /** Flag to denote the <i>Loading Selection</i> state. */
    public static final int         LOADING_SELECTION = 6;
    
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
    
    /** Identifies the <code>Delete Object</code> operation. */
    public static final int         REMOVE_OBJECT = 302;
    
    /** Identifies the <code>Manager</code> menu. */
    public static final int         MANAGER_MENU = 0;
    
    /** Identifies the <code>Classifier</code> menu. */
    public static final int         CLASSIFIER_MENU = 1;
    
    /** Identifies the <code>Full popUp menu</code> menu. */
    public static final int         FULL_POP_UP_MENU = 2;
    
    /** Identifies the <code>Partial popUp menu</code> menu. */
    public static final int         PARTIAL_POP_UP_MENU = 3;
    
    /** Identifies the <code>Copy and Paste</code> action. */
    public static final int         COPY_AND_PASTE = 400;
    
    /** Identifies the <code>Cut and Paste</code> action. */
    public static final int         CUT_AND_PASTE = 401;
    
    /** Bounds property to indicate that the data retrieval is cancelled. */
    public static final String      CANCEL_LOADING_PROPERTY = "cancelLoading";
    
    /** Bounds property to indicate to load a thumbnail for a given image. */
    public static final String      THUMBNAIL_LOADING_PROPERTY = 
                                                    "thumbnailLoading";
    
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
                                        "onComponentStateChanged";
    /** 
     * The title displayed in the {@link LoadingWindow} during the saving 
     * process.
     */
    public static final String      SAVING_TITLE = "Saving Data";
    
    /** 
     * The title displayed in the {@link LoadingWindow} during the saving 
     * process.
     */
    public static final String      LOADING_TITLE = "Loading...";
    
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
     * Sets the root of the retrieved hierarchies. 
     * 
     * @param rootID    	The Id of the root.
     * @param experimenter	The experimenter or <code>null</code> if 
     * 						the level is {@link #GROUP_ROOT}.
     */
    public void setHierarchyRoot(long rootID, ExperimenterData experimenter);
    
    /**
     * Returns <code>true</code> if the specified object is writable,
     * <code>false</code> otherwise, depending on the permission.
     * 
     * @param ho    The data object to check.
     * @return See above.
     */
    public boolean isObjectWritable(Object ho);
    
    /**
     * Returns <code>true</code> if the specified object is readable,
     * <code>false</code> otherwise.
     * 
     * @param hierarchyObject    The data object to check.
     * @return See above.
     */
    public boolean isReadable(DataObject hierarchyObject);
    
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

    /**
     * Sets the nodes to copy or cut depending on the passed index.
     * 
     * @param nodes The nodes to copy or paste.
     * @param index One of the following constants:
     *              {@link #CUT_AND_PASTE} or {@link #COPY_AND_PASTE}.
     */
    public void setNodesToCopy(TreeImageDisplay[] nodes, int index);
    
    /**
     * Pastes the nodes to copy into the specified parents.
     * 
     * @param parents The parents of the nodes to copy.
     * @see #setNodesToCopy(TreeImageDisplay[], int)
     */
    public void paste(TreeImageDisplay[] parents);
    
    /**
     * Sets the data displayed in the editor
     * 
     * @param b Pass <code>true</code> to save the data, <code>false</code> to
     *          remove the editor.
     */
    public void saveInEditor(boolean b);

    /**
     * Returns the {@link TreeViewer} view.
     * 
     * @return See above.
     */
    public JFrame getUI();
       
    /**
     * Sets the index of the selected pane when the editor is in the 
     * <code>Edit</code> mode.
     * 
     * @param index The value to set.
     */
    public void setEditorSelectedPane(int index);

    /**
     * Returns the type of the editor. One of the following constants 
     * {@link TreeViewer#CREATE_EDITOR}, {@link TreeViewer#PROPERTIES_EDITOR},
     * {@link TreeViewer#CLASSIFIER_EDITOR} or {@link TreeViewer#NO_EDITOR}.
     * 
     * @return See above.
     */
	public int getEditorType();

	/**
	 * Annotates the specified objects.
	 * 
	 * @param nodes The nodes to annotate.
	 */
	public void annotate(TreeImageDisplay[] nodes);

	/**
	 * Returns <code>true</code> if some data has to be saved before 
	 * selecting a new node, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasDataToSave();
	
	/**
	 * Brings up a dialog to save or not the data before switching to a 
	 * another object.
	 */
	public void showPreSavingDialog();

    /** 
     * Returns the id to the group selected for the current user.
     * 
     * @return See above.
     */
	public long getUserGroupID();

	/**
	 * Sets the available experimenter groups.
	 * 
	 * @param map The value to set.
	 */
	public void setAvailableGroups(Map map);

	/** Retrieves the user groups. */
	public void retrieveUserGroups();

    /**
     * Returns the first name and the last name of the currently 
     * selected experimenter as a String.
     * 
     * @return See above.
     */
	public String getExperimenterNames();
    
	/**
	 * Returns the currently selected experimenter.
	 * 
	 * @return See above.
	 */
	public ExperimenterData getSelectedExperimenter();

	/**
	 * Annotates the images contained in the specified folder.
	 * 
	 * @param node 	The node hosting the container.
	 * 				Can either be a <code>Dataset</code> or a 
	 * 				<code>Category</code>.
	 */
	public void annotateChildren(TreeImageDisplay node);
	
	/**
	 * Classifies the images contained in the specified folder.
	 * 
	 * @param node 	The node hosting the container.
	 * 				Can either be a <code>Dataset</code> or a 
	 * 				<code>Category</code>.
	 */
	public void classifyChildren(TreeImageDisplay node);
	
	/**
	 * Returns <code>true</code> if the viewer is recycled, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isRecycled();

	/**
	 * Returns <code>true</code> if the editor is updated when the user mouses
	 * over a node in the tree, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isRollOver();
	
	/**
	 * Sets to <code>true</code> if the editor is updated when the user mouses
	 * over a node in the tree, to <code>false</code> otherwise.
	 * 
	 * @param rollOver The value to set.
	 */
	public void setRollOver(boolean rollOver);

	/** Removes the experimenter from the display. */
	public void removeExperimenterData();
	
	/**
	 * Returns <code>true</code> if we can paste some rendering settings,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasRndSettings();

	/** Pastes the stored rendering settings across the selected images. */
	public void pasteRndSettings();
	
}
