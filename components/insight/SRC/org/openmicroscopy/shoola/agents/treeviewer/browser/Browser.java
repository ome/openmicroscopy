/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.treeviewer.browser;

import java.awt.Component;
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDef;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.FSFileSystemView;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import omero.gateway.model.DataObject;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;

/** 
 * Defines the interface provided by the browser component.
 * The browser provides a <code>JComponent</code> to host and display one
 * visualization tree. That is, one {@link TreeImageDisplay} top node.
 * Use the {@link BrowserFactory} to create an object implementing this 
 * interface.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public interface Browser
    extends ObservableComponent
{

    /**
     * Indicates that all images owned by the current user are retrieved if the
     * view is {@link #IMAGES_EXPLORER}.
     */
    static final int                NO_IMAGES_FILTER = 200;
    
    /**
     * Indicates that the images contained in the selected datasets are 
     * retrieved if the view is {@link #IMAGES_EXPLORER}.
     */
    static final int                IN_DATASET_FILTER = 201;
    
    /** Flag to denote the <i>New</i> state. */
    public static final int     	NEW = 10;
    
    /** Flag to denote the <i>Loading Data</i> state. */
    public static final int     	LOADING_DATA = 11;
    
    /** Flag to denote the <i>Loading Data</i> state. */
    public static final int     	LOADING_LEAVES = 12;
    
    /** Flag to denote the <i>Counting items</i> state. */
    public static final int     	COUNTING_ITEMS = 13;
    
    /** Flag to denote the <i>Browsing</i> state. */
    public static final int     	BROWSING_DATA = 14;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     	READY = 15;

    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     	DISCARDED = 16;

    /** 
     * Indicates that the browser corresponds to an <code>Hierarchy</code>
     * explorer.
     */
    public static final int     	PROJECTS_EXPLORER =
    	BrowserSelectionEvent.PROJECT_TYPE;
    
    /** 
     * Indicates that the browser corresponds to an <code>Images</code>
     * explorer.
     */
    public static final int     	IMAGES_EXPLORER =
    	BrowserSelectionEvent.IMAGE_TYPE;
    
    /** 
     * Indicates that the browser corresponds to a <code>Tags</code>
     * explorer.
     */
    public static final int     	TAGS_EXPLORER =
    	BrowserSelectionEvent.TAG_TYPE;
   
    /** 
     * Indicates that the browser corresponds to a <code>Screen</code>
     * explorer.
     */
    public static final int     	SCREENS_EXPLORER =
    	BrowserSelectionEvent.SCREEN_TYPE;
    
    /** 
     * Indicates that the browser corresponds to a <code>Files</code>
     * (saved on server) explorer.
     */
    public static final int     	FILES_EXPLORER =
    	BrowserSelectionEvent.FILE_TYPE;
    
    /** 
     * Indicates that the browser corresponds to a <code>Files</code>
     * explorer.
     */
    public static final int     	FILE_SYSTEM_EXPLORER =
    	BrowserSelectionEvent.FILE_SYSTEM_TYPE;
    
    /** 
     * Indicates that the browser corresponds to a <code>Shares</code>
     * explorer.
     */
    public static final int     	SHARES_EXPLORER = 106;
    
    /** 
     * Indicates that the browser corresponds to an Administration
     * explorer.
     */
    public static final int     	ADMIN_EXPLORER =
    	BrowserSelectionEvent.ADMIN_TYPE;
    
    /** Indicates to sort the nodes by date. */
    public static final int         SORT_NODES_BY_DATE = 300;
    
    /** Indicates to sort the nodes by name. */
    public static final int         SORT_NODES_BY_NAME = 301;
    
    /** Bound property name indicating the data retrieval is finished. */
    public static final String  	ON_END_LOADING_PROPERTY = "onEndLoading";
    
    /** Bound property indicating that the data have been refreshed. */
    public static final String		DATA_REFRESHED_PROPERTY = "dataRefreshed";
    
    /** Bound property name indicating a new node is selected. */
    public static final String  	SELECTED_TREE_NODE_DISPLAY_PROPERTY = 
        								"selectedTreeNodeDisplay";
    
    /** 
     * Bound property name indicating to remove the browser from the display. 
     */
    public static final String  	CLOSE_PROPERTY = "close";
    
    /** Bound property name indicating to bring up the pop-up menu.  */
    public static final String  	POPUP_MENU_PROPERTY = "popupMenu";
  
    /** 
     * The browser's title corresponding to {@link #PROJECTS_EXPLORER} type.
     */
    public static final String     HIERARCHY_TITLE = "Projects";

    /** 
     * The browser's title corresponding to {@link #IMAGES_EXPLORER} type.
     */
    public static final String     IMAGES_TITLE = "Images"; 
    
    /** 
     * The browser's title corresponding to {@link #TAGS_EXPLORER} type.
     */
    public static final String     TAGS_TITLE = "Tags";
    
    /** 
     * The browser's title corresponding to {@link #SCREENS_EXPLORER} type.
     */
    public static final String     SCREENS_TITLE = "Screens";
    
    /** 
     * The browser's title corresponding to {@link #FILES_EXPLORER} type.
     */
    public static final String     FILES_TITLE = "Attachments";
    
    /** 
     * The browser's title corresponding to {@link #FILE_SYSTEM_EXPLORER} type.
     */
    public static final String     FILE_SYSTEM_TITLE = "File System";
    
    /** 
     * The browser's title corresponding to {@link #ADMIN_EXPLORER} type.
     */
    public static final String     ADMIN_TITLE = "Administration";
    
    /** The text of the dummy default node. */
    public static final String     LOADING_MSG = "Loading...";
    
    /** 
     * The text of the node added to a {@link TreeImageSet} node
     * containing no element.
     */
    public static final String     EMPTY_MSG = "Empty";
    
    /**
     * Sets the selected {@link TreeImageDisplay node}.
     * 
     * @param display           The selected node.
     */
    void setSelectedDisplay(TreeImageDisplay display);
    
    /**
     * Queries the current state.
     * 
     * @return One of the state flags defined by this interface.
     */
    public int getState();
    
    /**
     * Starts the data loading process when the current state is {@link #NEW} 
     * and puts the window on screen.
     * If the state is not {@link #NEW}, then this method simply moves the
     * window to front.
     * 
     * @throws IllegalStateException If the current state is {@link #DISCARDED}.  
     */
    public void activate();
    
    /**
     * Transitions the viewer to the {@link #DISCARDED} state.
     * Any ongoing data loading is cancelled.
     */
    public void discard();
    
    /** 
     * Returns the UI component. 
     * 
     * @return See above.
     * @throws IllegalStateException If the current state is
     *                               {@link #DISCARDED}.
     */
    public JComponent getUI();
    
    /**
     * Call-back used by a data loader to set the leaves contained in the 
     * currently selected node.
     * 
     * @param leaves    The collection of leaves.
     * @param parent    The parent of the leaves.
     * @param expNode	The experimenter the data belonged to.
     * @throws IllegalStateException If the current state is not 
     *                               {@link #LOADING_LEAVES}.
     */
    public void setLeaves(Collection leaves, TreeImageSet parent, 
    					TreeImageSet expNode);
    
    /** 
     * Returns the type of this browser.
     * 
     * @return See above.
     */
    public int getBrowserType();
    
    /** Cancels any ongoing data loading. */
    public void cancel();   
    
    /**
     * Returns the location of the mouse click event occurred.
     * 
     * @return See above.
     */
    public Point getClickPoint();
    
    /** 
     * Returns the UI component where the mouse click event occurred.
     * 
     * @return See above.
     */
    public JComponent getClickComponent();
    
    /**
     * Returns the currently selected {@link TreeImageDisplay node}.
     * 
     * @return See above.
     */
    public TreeImageDisplay getLastSelectedDisplay();
    
    /**
     * Returns an array containing the selected nodes. If there isn't a selected
     * node. The array returned is of size <code>0</code>.
     * 
     * @return See above.
     */
    public TreeImageDisplay[] getSelectedDisplays();
    
    /**
     * Returns the nodes linked to the specified user.
     * 
     * @param userID The identifier of the user.
     * @param node The selected node.
     * @return See above.
     */
    public List<TreeImageDisplay> getNodesForUser(long userID, TreeImageDisplay
			node);
    
    /** 
     * Collapses the specified node. 
     * 
     * @param node The node to collapse.
     */
    public void collapse(TreeImageDisplay node);
    
    /** 
     * Expands the specified node. 
     * 
     * @param node The node to expand.
     */
    public void expand(TreeImageDisplay node);
    
    /** Removes the {@link Browser} from the display. */
    public void close();
    
    /** 
     * Returns the title of this browser.
     * 
     * @return The browser's title.
     */
    public String getTitle();
    
    /** 
     * Returns the icon of this browser. 
     * 
     * @return The browser's icon.
     */
    public Icon getIcon();
    
    /**
     * Has the specified object visit all the visualization trees hosted by
     * the browser.
     * 
     * @param visitor The visitor.  Mustn't be <code>null</code>.
     * @see TreeImageDisplayVisitor
     */
    public void accept(TreeImageDisplayVisitor visitor);
    
    /**
     * Has the specified object visit all the visualization trees hosted by
     * the browser.
     * 
     * @param visitor   The visitor.  Mustn't be <code>null</code>.
     * @param algoType  The algorithm selected to visit the visualization trees.
     *                  One of the constants defined by
     *                  {@link TreeImageDisplayVisitor}.
     * @see TreeImageDisplayVisitor
     */
    public void accept(TreeImageDisplayVisitor visitor, int algoType);
    
    /**
     * Sorts the nodes displayed in the browser. 
     * 
     * @param sortType One of the following constants:
     * {@link #SORT_NODES_BY_DATE} or {@link #SORT_NODES_BY_NAME}.
     */
    public void sortTreeNodes(int sortType);
    
    /** 
     * Brings up on screen the pop-up menu. 
     * 
     * @param menuIndex The index of the menu. One of the following constants
     * 					{@link TreeViewer#FULL_POP_UP_MENU} or 
     * 					{@link TreeViewer#PARTIAL_POP_UP_MENU}
     */
    public void showPopupMenu(int menuIndex);   
    
    /** 
     * Reloads children of the currently selected node and rebuilds
     * the display if the <code>Browser</code> is currently selected, 
     * if not, all the children are removed and there is no data loading.
     * 
     * @param refNode  The node to hosting the data object to browse.
     * @param toBrowse The data object to browse
     */
    public void refreshTree(Object refNode, DataObject toBrowse);
    
    /**
     * Sets the number of items contained in the specified container.
     * 
     * @param containerID The id of the container.
     * @param value		  The number of items contained in the container.
     * @param nodes       The collection of nodes.
     */
    public void setContainerCountValue(long containerID, long value, 
    		Set<TreeImageSet> nodes);

    /**
     * Sets the nodes found corresponding to a pattern.
     * 
     * @param nodes The collection of found nodes.
     */
    public void setFoundInBrowser(Set nodes);

    /** Finds the next occurence of the phrase. */
    public void findNext();
    
    /** Finds the previous occurence of the phrase. */
    public void findPrevious();
    
    /**
     * Sets the value to <code>true</code> if currently selected.
     * <code>false</code> otherwise.
     * 
     * @param b <code>true</code> if the browser is selected.
     * 			<code>false</code> otherwise.
     */
    public void setSelected(boolean b);
    
    /**
     * Refreshes the nodes hosting the specified <code>DataObject</code>.
     * 
     * @param object    The <code>DataObject</code> to handle.
     * @param parent	The parent of the <code>DataObject</code>.
     * 					This will be taken only when creating a new object.
     * @param op        The type of operation.
     */
    public void refreshEdition(DataObject object, DataObject parent, int op);

    /**
     * Sets the nodes as the selected nodes. Should only be 
     * <code>image</code> nodes.
     * 
     * @param nodes The nodes to set.
     * @param expandParent Pass <code>true</code> to expand the parent, 
     * 					  <code>false</code> otherwise.
     */
    public void setSelectedDisplays(TreeImageDisplay[] nodes, 
    		boolean expandParent);
    
    /**
     * Enables the components composing the display depending on the specified
     * parameter.
     * 
     * @param b Pass <code>true</code> to enable the component, 
     *          <code>false</code> otherwise.
     */
    public void onComponentStateChange(boolean b);

    /**
     * Returns <code>true</code> if the browser is displayed on screen,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isDisplayed();
    
    /**
     * Sets the <code>Displayed</code> flag. 
     * 
     * @param displayed Pass <code>true</code> to indicate the browser is on 
     *                  screen, <code>false</code> otherwise.
     */
    public void setDisplayed(boolean displayed);

    /**
     * Rebuilds the hierarchy tree.
     * 
     * @param set               The nodes to set.
     * @param expandedTopNodes  The expanded top nodes IDs.
     */
    public void setRefreshedHierarchy(Map set, Map expandedTopNodes);

    /** Shows the truncated name of the images or the full path. */
	public void displaysImagesName();
	
	/**
	 * Returns a collection of <code>DataObject</code> hosted by the
	 * selected nodes or <code>null</code> if no node selected.
	 * 
	 * @return See above.
	 */ 
	public List getSelectedDataObjects();

	/**
	 * Loads the data for the experimenter hosted by the specified node.
	 * 
	 * @param expNode 	The node hosting the experimenter. 
	 * 					Mustn't be <code>null</code>.
	 * @param node		The parent of the data. Pass <code>null</code>
	 * 					to retrieve all data.
	 */
	public void loadExperimenterData(TreeImageDisplay expNode, 
										TreeImageDisplay node);

	/**
	 * Adds the data to the passed experimenter node.
	 * 
	 * @param expNode	The experimenter node. Mustn't be <code>null</code>.
	 * @param nodes		The nodes to add.
	 */
	public void setExperimenterData(TreeImageDisplay expNode, Collection nodes);

	/** 
	 * Adds the passed experimenter to the display.
	 * 
	 * @param experimenter The experimenter to add. Mustn't be <code>null</code>.
	 * @param groupID The identifier of the group the experimenter has to be
	 * added.
	 */
	public void addExperimenter(ExperimenterData experimenter, long groupID);

	/**
	 * Removes the experimenter's data from the display.
	 * 
	 * @param exp The experimenter to remove. Mustn't be <code>null</code>.
	 * @param groupID The group's id the experimenter is member of.
	 */
	public void removeExperimenter(ExperimenterData exp, long groupID);

	/** Refreshes the experimenter node. */
	public void refreshExperimenterData();
	
	/**
	 * Sets the data for the selected experimenter. 
	 * 
	 * @param def The data to set. Mustn't be <code>null</code>.
	 * @param type The type of data object to select or <code>null</code>.
	 * @param id   The identifier of the data object.
	 */
	public void setRefreshExperimenterData(
			Map<SecurityContext, RefreshExperimenterDef> def, Class type,
			long id);

	/** Refreshes the data used by the currently logged in user. */
	public void refreshLoggedExperimenterData();
	
	/**
	 * Counts the images imported by the current user during certain periods 
	 * of time.
	 * 
	 * @param expNode 	The node hosting the experimenter. 
	 * 					Mustn't be <code>null</code>.
	 */
	void countExperimenterImages(TreeImageDisplay expNode);

	/** 
	 * Sets the result of the count.
	 * 
	 * @param expNode 	The node hosting the experimenter. 
	 * 					Mustn't be <code>null</code>.
	 * @param index		The index of the node.
	 * @param value		The value to set.
	 */
	void setExperimenterCount(TreeImageSet expNode, int index, Object value);
	
	/**
	 * Returns the owner of the node.
	 * 
	 * @param node The node to handle.
	 * @return See above.
	 */
	ExperimenterData getNodeOwner(TreeImageDisplay node);
	
	/**
	 * Returns the group the node is representing.
	 * 
	 * @param node The node to handle.
	 * @return See above.
	 */
	GroupData getNodeGroup(TreeImageDisplay node);

	/** 
	 * Sets the node the user wished to save before being prompt with
	 * the Save data message box.
	 */
    public void setSelectedNode();
    
    /** 
     * Refreshes the experimenter or the group.
     * 
     * @param data Pass either an experimenter or a group.
     */
    public void refreshAdmin(Object data);

    /**
     * Browses the specified node.
     * 
     * @param node The node of reference. 
     * @param data The object of reference.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
     */
	public void browse(TreeImageDisplay node, DataObject data, 
			boolean withThumbnails);
	
	/**
	 * Updates the view when a node is selected in the thumbnail view
	 * or table view.
	 * 
	 * @param parent			The parent of the selected node.
	 * @param selected			The selected node.
	 * @param multiSelection	Pass <code>true</code> if multi selection is
	 * 							<code>false</code> otherwise.
	 */
	public void onSelectedNode(Object parent, Object selected, 
							Boolean multiSelection); 
	
	/**
	 * Updates the view when a node is deselected in the thumbnail view
	 * or table view.
	 * 
	 * @param parent			The parent of the selected node.
	 * @param selected			The node to deselect.
	 * @param multiSelection	Pass <code>true</code> if multi selection is
	 * 							<code>false</code> otherwise.
	 */
	public void onDeselectedNode(Object parent, Object selected, 
							Boolean multiSelection); 
	
	/**
	 * Updates the view when a dataset not linked to a project is created.
	 * 
	 * @param data The newly created dataset.
	 */
	public void onOrphanDataObjectCreated(DataObject data);

	/**
	 * Sets the images imported during a given period of time.
	 * 
	 * @param set	The collection of images.
	 * @param node	The node hosting the time information.
	 */
	public void setTimeIntervalImages(Set set, TreeImageTimeSet node);

    /** 
	 * Notifies the users that the imports is finished. 
	 * 
	 * @param nodes The collection of nodes to reload.
	 */
	void onImportFinished(List<TreeImageDisplay> nodes);
	
	/**
	 * Sets the imported image.
	 * 
	 * @param image The imported image.
	 */
	void setImportedFile(ImageData image);
	
	/**
	 * Returns <code>true</code> if the file has already been imported,
	 * <code>false</code> otherwise.
	 * 
	 * @param path The path to the file.
	 * @return See above.
	 */
	boolean isFileImported(String path);

	/**
	 * Deletes the {@link DataObject}s hosted by the passed nodes.
	 * 
	 * @param nodes The nodes hosting the {@link DataObject}s to delete.
	 */
	public void deleteObjects(List nodes);

	/**
	 * Brings up the menu to manage the data.
	 * 
	 * @param index The index of the menu.
	 * @param source The component that requested the pop-up menu.
	 * @param point The point at which to display the menu, relative to the
	 *            <code>component</code>'s coordinates.
	 */
	void showMenu(int index, Component source, Point point);

	/** Refreshes the browser. */
	void refreshBrowser();
	
	/** Removes the passed nodes from the display. */
	void removeTreeNodes(Collection<TreeImageDisplay> nodes);
	
	/** 
	 * Removes all the data from the display
	 * and reloads the data for the currently logged experimenter
	 * only for the active browser.
	 */
	void reActivate();
	
	/**
	 * Sets the repositories.
	 * 
	 * @param expNode		The experimenter node. Mustn't be <code>null</code>.
	 * @param systemView 	The file system hosting the repositories. 
	 * 						Mustn't be <code>null</code>.
	 */
	void setRepositories(TreeImageDisplay expNode, FSFileSystemView systemView);
	
	/** 
	 * Sets the groups to manage.
	 * 
	 * @param groups   The groups to set.
	 * @param expanded Collection of expanded group's identifiers.
	 */
	void setGroups(Collection groups, List expanded);

	/** 
	 * Registers the specified object. Returns <code>true</code>
	 * if the file has been registered, <code>false</code> otherwise.
	 * 
	 * @param file The file to register.
	 */
	boolean register(DataObject file);

	/**
	 * Sets the experimenters counted in the specified group.
	 * 
	 * @param node
	 * @param result
	 */
	void setExperimenters(TreeImageSet node, List result);

	/** Expands the node corresponding to the user currently logged in. */
	void expandUser();

	/** 
	 * Sets the new password.
	 * 
	 * @param value The value to set.
	 */
	void resetPassword(String value);
	
	/**
	 * Refreshes the browser and selects the nodes identified by the type
	 * and the identifier.
	 * 
	 * @param type The type of object to handle.
	 * @param id   The identifier of the object.
	 */
	void refreshBrowser(Class type, long id);
	
    /**
     * Adds the component under the tree. This method should only be invoked
     * if the browser is displayed Screening data.
     * 
     * @param component The component to add.
     */
	void addComponent(JComponent component);
	
	/**
	 * Loads the files contained in the passed folder.
	 * 
	 * @param display The directory.
	 */
	void loadDirectory(TreeImageDisplay display);
	
	/**
	 * Returns <code>true</code> if the user currently logged in can
	 * edit the passed object, <code>false</code> otherwise.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canEdit(Object ho);
	
	/**
	 * Returns <code>true</code> if the user currently logged in can
	 * annotate the passed object, <code>false</code> otherwise.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	public boolean canAnnotate(Object ho);
	
	/**
	 * Returns the node corresponding to the experimenter currently logged in.
	 * 
	 * @return See above.
	 */
	public TreeImageDisplay getLoggedExperimenterNode();

	/** Indicates that the transfer has been rejected.*/
	void rejectTransfer();

	/**
	 * Returns the security context.
	 * 
	 * @param node The node to handle
	 * @return See above.
	 */
	SecurityContext getSecurityContext(TreeImageDisplay node);
	
	/**
	 * Adds the specified group to the tree.
	 * 
	 * @param group The selected group.
	 */
	void setUserGroup(GroupData group);

	/**
	 * Removes the specified group from the display
	 * 
	 * @param group The group to remove.
	 */
	void removeGroup(GroupData group);

	/**
	 * Returns <code>true</code> if the specified object can have hard links
	 * i.e. image added to dataset, <code>false</code> otherwise,
	 * depending on the permission.
	 * 
	 * @param ho The data object to check.
	 * @return See above.
	 */
	boolean canLink(Object ho);
	
	/**
	 * Returns the objects to copy or <code>null</code>.
	 * 
	 * @return See above.
	 */
	List<DataObject> getDataToCopy();

	/**
	 * Sets the nodes to copy or cut depending on the passed index.
	 * 
	 * @param nodes The nodes to copy or paste.
	 * @param index One of the constants defined by this class.
	 */
	void setNodesToCopy(TreeImageDisplay[] nodes, int index);

	/**
	 * Pastes the nodes to copy into the specified parents.
	 * 
	 * @param parents The parents of the nodes to copy.
	 * @see #setNodesToCopy(TreeImageDisplay[], int)
	 */
	void paste(TreeImageDisplay[] parents);
	
	/**
	 * Returns the node hosting the default group when in group display mode.
	 * 
	 * @return See above.
	 */
	TreeImageDisplay getDefaultGroupNode();

	/**
	 * Returns the display mode. One of the constants defined by 
	 * {@link TreeViewer}.
	 * 
	 * @return See above.
	 */
	int getDisplayMode();
	
	/** Rebuilds the tree when the display mode is modified.*/
	void changeDisplayMode();

}
