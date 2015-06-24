/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;

//Third-party libraries

import org.openmicroscopy.shoola.agents.treeviewer.ImageChecker.ImageCheckerType;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.DataObjectRegistration;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.env.data.model.ImageCheckerResult;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.ui.ActivityComponent;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;


/** 
* Defines the interface provided by the tree viewer component.
* The tree viewer provides a top-level window to host different types of
* hierarchy display and let the user interact with it.
* A display is a view with a visualization tree. A visualization tree
* is a graphical tree that represents objects in a given  <i>OME</i> hierarchy,
* like Project/Dataset/Image, Screen/Plate/Wells or simply Images.
* The component follows the <code>Lazy loading rule</code> i.e. the leaves 
* of a given hierarchy are only retrieved if the parent is selected.
* In practice, this means that we only display a Project/Dataset hierarchy
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
	
	/** Indicates to display the data per experimenter.*/
	public static final int EXPERIMENTER_DISPLAY = 
			LookupNames.EXPERIMENTER_DISPLAY;
	
	/** Indicates to display the data per group.*/
	public static final int GROUP_DISPLAY = LookupNames.GROUP_DISPLAY;

	/** 
	 * Indicates that the {@link TreeViewer} displayed the various explorers.
	 */
	public static final int			EXPLORER_MODE = 100;
	
	/** 
	 * Indicates that the {@link TreeViewer} displayed the various explorers.
	 */
	public static final int			SEARCH_MODE = 101;
	
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

	/** Flag to denote the <i>Settings rendering</i> state. */
	public static final int         SETTINGS_RND = 8;
	
	/** Flag to denote the <i>Settings rendering</i> state. */
	public static final int         RND_SET = 9;
	
	/** Identifies the <code>Create</code> type for the editor. */
	public static final int         CREATE_EDITOR = 100;

	/** Identifies the <code>Edit</code> type for the editor. */
	public static final int         PROPERTIES_EDITOR = 101;

	/** Identifies the <code>No Editor</code> type for the editor. */
	public static final int         NO_EDITOR = 102;

	/** Identifies the <code>Delete Object</code> operation. */
	public static final int         REMOVE_OBJECT = 302;

	/** Identifies the <code>Manager</code> menu. */
	public static final int         MANAGER_MENU = 0;

	/** Identifies the <code>Full popUp menu</code> menu. */
	public static final int         FULL_POP_UP_MENU = 1;

	/** Identifies the <code>Partial popUp menu</code> menu. */
	public static final int         PARTIAL_POP_UP_MENU = 2;

	/** Identifies the <code>Create popUp menu</code> menu. */
	public static final int         CREATE_MENU_CONTAINERS = 3;

	/** Identifies the <code>Create popUp menu</code> menu. */
	public static final int         CREATE_MENU_TAGS = 4;

	/** Identifies the <code>Personal</code> menu. */
	public static final int         PERSONAL_MENU = 5;
	
	/** Identifies the <code>Create popUp menu</code> menu. */
	public static final int         CREATE_MENU_ADMIN = 6;
	
	/** Identifies the <code>Create popUp menu</code> menu. */
	public static final int         ADMIN_MENU = 7;
	
	/** Identifies the <code>Create popUp menu</code> menu. */
	public static final int         CREATE_MENU_SCREENS = 8;

	/** Identifies the <code>View pop-up menu</code> menu. */
	public static final int         VIEW_MENU = 9;
	
	/** Identifies the <code>Available Scripts</code> menu. */
	public static final int         AVAILABLE_SCRIPTS_MENU = 10;
	
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
	
	/** Bound property indicating to the group has been modified. */
	public static final String      GROUP_CHANGED_PROPERTY = "groupChanged";
	
	/** Bound property indicating that the mode. */
	public static final String		DISPLAY_MODE_PROPERTY = "searchMode";
	
	/** Bound property indicating to import data. */
	public static final String		IMPORT_PROPERTY = "importImages";
	
	/** Bound property indicating that the images have been imported. */
	public static final String		IMPORTED_PROPERTY = "imported";
	
	/** 
	 * Bound property indicating that the nodes have been selected
	 * automatically.
	 */
	public static final String      SELECTION_PROPERTY = "selection";
	
	/** 
	 * Bound property indicating the start of the available scripts loading.
	 */
	public static final String      SCRIPTS_LOADING_PROPERTY = "scriptsLoading";
	
	/** 
	 * Bound property indicating the start of the available scripts are loaded.
	 */
	public static final String      SCRIPTS_LOADED_PROPERTY = "scriptsLoaded";
	
	/** 
	 * The title displayed in the {@link LoadingWindow} during the saving 
	 * process.
	 */
	public static final String      SAVING_TITLE = "Saving Data";

	/** 
	 * The title displayed in the {@link LoadingWindow} during the saving 
	 * process.
	 */
	public static final String      LOADING_TITLE = "Loading Data...";

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
	 * Returns the {@link Browser} displayed when the application is first
	 * launched. This should not be <code>null</code>.
	 * 
	 * @return See above.
	 */
	Browser getDefaultBrowser();
	
	/**
	 * Sets the currently selected {@link Browser} or <code>null</code>
	 * if no {@link Browser} is selected.
	 * 
	 * @param browser 	The selected {@link Browser}.
	 * @param activate 	Passed <code>true</code> to activate the browser,
	 * 					<code>false</code> otherwise.
	 */
	void setSelectedBrowser(Browser browser, boolean activate);

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
	public Map<Integer, Browser> getBrowsers();

	/**
	 * Transitions the viewer to the {@link #DISCARDED} state.
	 * Any ongoing data loading is cancelled.
	 */
	public void discard();

	/** 
	 * Adds or removes the {@link Browser} corresponding to the specified type
	 * to the display depending on the actual status of the browser.
	 * 
	 * @param browserType The browser's type.
	 */
	public void displayBrowser(int browserType);

	/**
	 * Brings up the editor to create a data object.
	 * 
	 * @param object 		The {@link DataObject} to create.
	 * @param withParent	Sets to <code>true</code> if the object will 
	 * 						have a parent, <code>false</code> otherwise.
	 */
	public void createDataObject(DataObject object, boolean withParent);

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
	 * @param parent	The parent of the <code>DataObject</code>.
	 * @param op 		The type of operation.
	 */
	public void onDataObjectSave(DataObject data, DataObject parent, int op);
	
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
	public void onDataObjectSave(List data, int operation);

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
	 * @param experimenters	The experimenters or <code>null</code> if 
	 * 						the level is {@link #GROUP_ROOT}.
	 */
	public void setHierarchyRoot(long rootID, 
			List<ExperimenterData> experimenters);

	/**
	 * Returns <code>true</code> if the specified object can be moved to another
	 * group, <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canChgrp(Object ho);
	
	/**
	 * Returns <code>true</code> if the specified object can be deleted.
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canDelete(Object ho);
	
	/**
	 * Returns <code>true</code> if the specified object can be edited.
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho    The data object to check.
	 * @return See above.
	 */
	public boolean canEdit(Object ho);
	
	/**
	 * Returns <code>true</code> if the specified object can be annotated,
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho    The data object to check.
	 * @return See above.
	 */
	public boolean canAnnotate(Object ho);
	
	/**
	 * Returns <code>true</code> if the specified object can have hard links
	 * i.e. image added to dataset, <code>false</code> otherwise,
	 * depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canLink(Object ho);
	
	/** 
	 * Adds existing objects to the currently selected node. 
	 * 
	 * @param ho The node the objects are added to.
	 */
	public void addExistingObjects(DataObject ho);

	/**
	 * Displays the collection of existing o.
	 * 
	 * @param nodes The nodes to display.
	 */
	public void setExistingObjects(List nodes);

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
	 * @param menuID    The id of the menu.
	 * @param invoker   The component that requested the pop-up menu.
	 * @param loc       The point at which to display the menu, relative to the
	 *                  <code>component</code>'s coordinates.
	 */
	public void showMenu(int menuID, Component invoker, Point loc);

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
	 * Returns the {@link TreeViewer} view.
	 * 
	 * @return See above.
	 */
	public JFrame getUI();

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
	 * Retrieves the user groups. 
	 * 
	 * @param location The location of the mouse pressed.
	 * @param group The group to handle.
	 */
	public void retrieveUserGroups(Point location, GroupData group);

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

	/** 
	 * Pastes the stored rendering settings across the selected images. 
	 * 
	 * @param ids Collection of node ids.
	 * @param klass Either dataset, image or category.
	 */
	public void pasteRndSettings(List<Long> ids, Class klass);

	/** 
	 * Pastes the stored rendering settings across the selected images. 
	 * 
	 * @param ref The time reference object.
	 */
	public void pasteRndSettings(TimeRefObject ref);
	
	/** 
	 * Resets the rendering settings across the selected images. 
	 * 
	 * @param ids Collection of node ids.
	 * @param klass Either dataset, image or category.
	 */
	public void resetRndSettings(List<Long> ids, Class klass);

	/** 
	 * Resets the rendering settings across the selected images. 
	 * 
	 * @param ref The time reference object.
	 */
	public void resetRndSettings(TimeRefObject ref);

	/** 
	 * Notifies the user that the save is done.
	 * 
	 * @param map The value to set.
	 */
	public void rndSettingsPasted(Map map);

	/**
	 * Creates the passed object.
	 * 
	 * @param object 		The object to create.
	 * @param withParent	Sets to <code>true</code> if the object will 
	 * 						have a parent, <code>false</code> otherwise.
	 */
	public void createObject(DataObject object, boolean withParent);

	/**
	 * Sets the leaves linked to the specified parent.
	 * 
	 * @param parent 	The node the leaves are related to.
	 * @param leaves	The leaves to convert and add to the node.
	 */
	public void setLeaves(TreeImageSet parent, Collection leaves);
	
	/**
	 * Sets the selected node.
	 * 
	 * @param node The value to set.
	 */
	public void setSelectedNode(Object node);
	
	/**
	 * Browses the passed hierarchy.
	 * 
	 * @param parent The parent of the node.
	 * @param nodes  The value to set.
	 */
    public void browseHierarchyRoots(Object parent, Collection nodes);
    
    /** 
     * Adds a dataset without project.
     * 
     * @param data The object to add.
     */
    public void onOrphanDataObjectCreated(DataObject data);

    /**
     * Unselects the passed node.
     * 
     * @param node The node to deselect.
     */
	public void setUnselectedNode(Object node);

	/** 
	 * Copies the rendering settings. 
	 * 
	 * @param image The image to copy the rendering settings from 
	 * 				or <code>null</code>.
	 */
	public void copyRndSettings(ImageData image);

	/**
	 * Sets the original rendering settings for the images identified by 
	 * the specified parameters.
	 * 
	 * @param ids		The collection of objects id.
	 * @param klass		The class identifying the object.
	 */
	public void setMinMax(List<Long> ids, Class klass);
	
	/**
	 * Sets the original rendering settings for images 
	 * imported during the specified period of time.
	 * 
	 * @param ref The object storing the time interval information.
	 */
	public void setOriginalRndSettings(TimeRefObject ref);

	/**
	 * Sets the rendering settings used by the owner, 
	 * for the images identified by the specified parameters.
	 * 
	 * @param ids		The collection of objects id.
	 * @param klass		The class identifying the object.
	 */
	public void setOwnerRndSettings(List<Long> ids, Class klass);

	/**
	 * Sets the original rendering settings used by the owner, for images 
	 * imported during the specified period of time.
	 * 
	 * @param ref The object storing the time interval information.
	 */
	public void setOwnerRndSettings(TimeRefObject ref);
	
	/** Shows or hides the searching component. */
	public void showSearch();
    
        /** Handles a search event */
	public void handleSearchEvent(SearchEvent evt);
	
	/** Handles a search result selection event */
	public void handleSearchSelectionEvent(SearchSelectionEvent evt);
	
	/**
	 * Sets the result of the search.
	 * 
	 * @param result 	Either a collection of objects or
	 * 					or an integer with the maximum number of results
	 */
	public void setSearchResult(Object result);

	/** 
	 * Brings up the dialog used to add metadata to a collection of 
	 * images selected either by the user or as linked to a dataset
	 * or tag.
	 */
	public void addMetadata();

	/** Refreshes the selected tree. */
	public void refreshTree();

	/**
	 * Browses the images acquired during the passed time interval.
	 * 
	 * @param node The node holding the time information.
	 * @param set  The elements to add.
	 */
	public void browseTimeInterval(TreeImageTimeSet node, Set set);

	/**
	 * Sets the wells linked to the specified plates.
	 * 
	 * @param plates The parents to handle.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
	 */
	public void setPlates(Map<TreeImageSet, Set> plates, boolean withThumbnails);

	/**
	 * Browses the passed node.
	 * 
	 * @param node The reference node to browse.
	 * @param data The object to browse.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
	 */
	public void browse(TreeImageDisplay node, DataObject data, 
			boolean withThumbnails);
	
	/**
	 * Deletes the {@link DataObject}s hosted by the passed nodes.
	 * 
	 * @param nodes The nodes hosting the {@link DataObject}s to delete.
	 */
	public void deleteObjects(List nodes);

	/**
	 * Returns the objects to copy or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public List<DataObject> getDataToCopy();

	/** 
	 * Refreshes the view when nodes have been deleted. A collection of nodes
	 * that could not be deleted is passed. If the collection is 
	 * <code>null</code> or of size <code>0</code> all the nodes have been
	 * deleted.
	 * 
	 * @param nodes The collection of nodes that couldn't be deleted.
	 */
	public void onNodesDeleted(Collection<DataObject> notDeleted);
	
	/** 
	 * Refreshes the view when nodes have been <code>Cut/Paste</code> or
	 * <code>Copy/Paste</code>. 
	 */
	public void onNodesMoved();

	/** Displays the tag wizard. */
	public void showTagWizard();
	
	/** 
	 * Sets the selected field of a well.
	 * 
	 * @param node The selected field.
	 */
	public void setSelectedField(Object node);

	/** Shows or hides the Tree Viewer. */
	public void setInspectorVisibility();
	
	/**
	 * Returns all the images currently displayed in the 
	 * <code>data browser</code>.
	 * 
	 * @return See above.
	 */
	public Collection getDisplayedImages();

	/**
	 * Indicates that an activity has just terminated.
	 * 
	 * @param activity The activity to handle.
	 * @param finished Pass <code>true</code> to indicate the activity is 
	 * 				   finished, <code>false</code> otherwise.
	 */
	void onActivityProcessed(ActivityComponent activity, boolean finished);

	/**
	 * Downloads the currently selected files to the specified folder.
	 * 
	 * @param folder The folder where to download the files.
	 * @param override Flag indicating to override the existing file if it
     *                 exists, <code>false</code> otherwise.
	 */
	void download(File folder, boolean override);

	/**
	 * Sets the collection of archived files.
	 * 
	 * @param folder The folder where to save the files.
	 * @param files  The collection of files to handle.
	 * @param data	 The third party application or <code>null</code>.
	 */
	void setDownloadedFiles(File folder, ApplicationData data, Collection o);

	/**
	 * Indicates to view the image with another application. 
	 * This will only possible if the image has been archived.
	 * 
	 * @param data The application to use to open the file.
	 */
	void openWith(ApplicationData data);

	/**
	 * Adds/Removes the groups to/from the display.
	 * 
	 * @param groups The groups to set.
	 */
	void setUserGroup(List<GroupData> groups);

	/** Opens the image in a separate window or in the main viewer. */
	void setFullScreen();

	/** Shows or hides the Metadata View. */
	void setMetadataVisibility();
	
	/**
	 * Returns all the scripts currently stored into the system.
	 * 
	 * @return See above.
	 */
	public Map<Long, String> getScriptsAsString();

	/**
	 * Returns <code>true</code> if the currently logged in user is 
	 * a leader of the specified group, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isLeaderOfGroup(GroupData group);
	
	/** 
	 * Manages the passed object.
	 * 
	 * @param object The object to handle.
	 */
	void administrate(AdminObject object);

	/** 
	 * Registers the passed file and updates the annotation.
	 * 
	 * @param file The file to register..
	 */
	void register(DataObjectRegistration file);
	
	/**
	 * Returns the permission level of the selected group. One of the constants
	 * defined by the <code>AdminObject</code> class.
	 * 
	 * @param group The group to handle.
	 * @return See above.
	 */
	int getGroupPermissions(GroupData group);

	/**
	 * Resets the password of the selected experimenters.
	 * 
	 * @param value The new password.
	 */
	void resetPassword(String value);

	/** 
	 * Indicates that the group has been successfully switched if 
	 * <code>true</code>, unsuccessfully if <code>false</code>.
	 * 
	 * @param success 	Pass <code>true</code> if successful, 
	 * 					<code>false</code> otherwise.
	 */
	void onGroupSwitched(boolean success);
	
	/**
	 * Finds the specified object.
	 * 
	 * @param type The type of data object to find.
	 * @param id   The identifier of the data object.
	 * @param selectTab Pass <code>true</code> to select the tab corresponding 
	 * to the passed type, <code>false</code> otherwise.
	 */
	void findDataObject(Class type, long id, boolean selectTab);

	/**
	 * Returns <code>true</code> if there is an on-going import,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImporting();
	
	/**
	 * Sets the flag indicating of any on-going imports.
	 * 
	 * @param importing The value to set.
	 * @param containers The containers that need to be refreshed.
	 * @param refresh Pass <code>true</code> to mark the node to refresh,
	 * <code>false</code> otherwise.
	 * @param importedObject The result of the import.
	 */
	void setImporting(boolean importing, List<DataObject> containers, boolean
			refresh, Object importedObject);

	/**
	 * Marks the passed nodes.
	 * 
	 * @param containers The containers that need to be refreshed.
	 * @param refresh Pass <code>true</code> to mark the node to refresh,
	 * <code>false</code> otherwise.
	 */
	void indicateToRefresh(List<DataObject> containers, boolean refresh);
	
	/**
	 * Browses the specified container.
	 * 
	 * @param data The data object to browse.
	 * @param node The node hosting the object to browse or <code>null</code>.
	 */
	void browseContainer(Object data, Object node);

	/**
	 * Activates the user or de-activates the user.
	 * 
	 * @param exp The experimenter to handle.
	 */
	void activateUser(ExperimenterData exp);

	/** 
	 * Creates the specified data object and links the selected children
	 * to it.
	 * 
	 * @param data The object to create.
	 */
	void createDataObjectWithChildren(DataObject data);

	/**
	 * Sets the collection of available scripts.
	 * 
	 * @param scripts The available scripts.
	 * @param location The location of the mouse click.
	 */
	void setAvailableScripts(List result, Point location);

	/**
	 * Loads the specified script.
	 * 
	 * @param scriptID The identifier of the script to load.
	 */
	void loadScript(long scriptID);

	/**
	 * Sets the script.
	 * 
	 * @param object The object hosting the script.
	 */
	void setScript(ScriptObject object);

	/** Transfers the nodes.
	 * 
	 * @param target The target.
	 * @param nodes The nodes to transfer.
	 * @param transferAction The transfer action.
	 */
	void transfer(TreeImageDisplay target, List<TreeImageDisplay> nodes, int
			transferAction);
	
	/**
	 * Sets the selected nodes, the nodes selected from middle panel.
	 * 
	 * @param nodes The value to set.
	 */
	void setSelectedNodes(Object nodes);

	/**
	 * Returns the selected group.
	 * 
	 * @return See above.
	 */
	GroupData getSelectedGroup();
	
	/**
	 * Returns the only group displayed if any.
	 * 
	 * @return See above.
	 */
	GroupData getSingleGroupDisplayed();

	/** 
	 * Removes group.
	 * 
	 * @param groupID The id of the group.
	 */
	void removeGroup(long groupID);

	/** 
	 * Moves the selected data to the specified group.
	 * 
	 * @param group The data to move.
	 */
	void moveTo(GroupData group, List<DataObject> nodes);

	/**
	 * Displays the groups to select.
	 * 
	 * @param point The location of the mouse pressed.
	 */
	void displayUserGroups(Point point);

	/** Returns the security context.
	 * 
	 * @return See above.
	 */
	SecurityContext getSecurityContext();

	/**
	 * Returns <code>true</code> if the image to copy the rendering settings
	 * from is in the specified group, <code>false</code> otherwise.
	 * 
	 * @param groupID The group to handle.
	 * @return See above.
	 */
	boolean areSettingsCompatible(long groupID);
	
	/**
	 * Returns the groups the user is a member of.
	 * 
	 * @return See above.
	 */
	Collection getGroups();
	
	/**
	 * Returns the display mode. One of the constants defined by 
	 * {@link TreeViewer}.
	 * 
	 * @return See above.
	 */
	int getDisplayMode();

	/**
	 * Sets the display mode.
	 * 
	 * @param index The mode to set.
	 */
	void setDisplayMode(int index);

	/**
	 * Handles the result checking if images to delete/move are split or not
	 * 
	 * @param result The result to handle.
	 * @param action The action to do after the check.
	 * @param index The type of action.
	 */
	void handleSplitImage(ImageCheckerResult result, Object action,
			ImageCheckerType index);

    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param userID The identifier of the user.
     * @return See above.
     */
    boolean isSystemUser(long userID);

    /**
     * Returns <code>true</code> if the user is a system user e.g. root
     * <code>false</code> otherwise.
     *
     * @param userID The identifier of the user.
     * @param key One of the constants defined by GroupData.
     * @return See above.
     */
    boolean isSystemUser(long userID, String key);

    /**
     * Returns <code>true</code> if the group is a system group,
     * <code>false</code> otherwise.
     *
     * @param groupID The identifier of the group.
     * @param key One of the constants defined by GroupData.
     * @return See above.
     */
    boolean isSystemGroup(long groupID, String key);

}
