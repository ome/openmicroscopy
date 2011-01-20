/*
 * org.openmicroscopy.shoola.agents.treemng.browser.Browser
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;




//Java imports
import java.awt.Component;
import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDef;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

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
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
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
    
    /**
     * Indicates that the images contained in the selected categories are 
     * retrieved if the view is {@link #IMAGES_EXPLORER}.
     */
    static final int                IN_CATEGORY_FILTER = 202;
    
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
    public static final int     	PROJECT_EXPLORER = 100;
    
    /** 
     * Indicates that the browser corresponds to an <code>Images</code>
     * explorer.
     */
    public static final int     	IMAGES_EXPLORER = 101;
    
    /** 
     * Indicates that the browser corresponds to a <code>Tags</code>
     * explorer.
     */
    public static final int     	TAGS_EXPLORER = 102;
   
    /** 
     * Indicates that the browser corresponds to a <code>Screen</code>
     * explorer.
     */
    public static final int     	SCREENS_EXPLORER = 103;
    
    /** 
     * Indicates that the browser corresponds to a <code>Files</code>
     * (saved on server) explorer.
     */
    public static final int     	FILES_EXPLORER = 104;
    
    /** 
     * Indicates that the browser corresponds to a <code>Files</code>
     * explorer.
     */
    public static final int     	FILE_SYSTEM_EXPLORER = 105;
    
    
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
    
    /** Bound property name indicating to bring up the popup menu.  */
    public static final String  	POPUP_MENU_PROPERTY = "popupMenu";
    
    /** 
     * Bound property name indicating to display the list of supported file
     * formats.  
     */
    public static final String  	FILE_FORMATS_PROPERTY = "fileFormats";
  
    /** 
     * The browser's title corresponding to {@link #PROJECT_EXPLORER} type.
     */
    public static final String     HIERARCHY_TITLE = "Hierarchies";//"Projects";

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
     * Callback used by a data loader to set the leaves contained in the 
     * currently selected node.
     * 
     * @param leaves    The collection of leaves.
     * @param parent    The parent of the leaves.
     * @param expNode	The experimenter the data belonged to.
     * @throws IllegalStateException If the current state is not 
     *                               {@link #LOADING_LEAVES}.
     */
    public void setLeaves(Set leaves, TreeImageSet parent, 
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
     * Returns the location of the mouse click event occured.
     * 
     * @return See above.
     */
    public Point getClickPoint();
    
    /** 
     * Returns the UI component where the mouse click event occured.
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
     * Collapses the specified node. 
     * 
     * @param node The node to collapse.
     */
    public void collapse(TreeImageDisplay node);
    
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
     */
    public void refreshTree();
    
    /**
     * The id of the root level.
     * 
     * @return See above.
     */
    public long getRootID();
    
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
     */
    public void setSelectedDisplays(TreeImageDisplay[] nodes);
    
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
	 * @param experimenter 	The experimenter to add. 
	 * 						Mustn't be <code>null</code>.
	 * @param load			Pass <code>true</code> to load the data,
	 * 						<code>false</code> otherwise.
	 */
	public void addExperimenter(ExperimenterData experimenter, boolean load);

	/**
	 * Removes the experimenter's data from the display.
	 * 
	 * @param exp The experimenter to remove. Mustn't be <code>null</code>.
	 */
	public void removeExperimenter(ExperimenterData exp);

	/** Refreshes the experimenter node. */
	public void refreshExperimenterData();
	
	/**
	 * Sets the data for the selected experimenter. 
	 * 
	 * @param def The data to set. Mustn't be <code>null</code>.
	 */
	public void setRefreshExperimenterData(
					Map<Long, RefreshExperimenterDef> def);

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
	 * Sets the node the user wished to save before being prompt with
	 * the Save data message box.
	 */
    public void setSelectedNode();
    
    /** Refreshes the experimenter data. */
    public void refreshExperimenter();

    /**
     * Browses the specified node.
     * 
     * @param node The node to browse.
	 * @param withThumbnails Pass <code>true</code> to load the thumbnails,
     * 						 <code>false</code> otherwise.
     */
	public void browse(TreeImageDisplay node, boolean withThumbnails);
	
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

	/** Displays the list of supported file formats. */
	void showSupportedFiles();

	/**
	 * Returns <code>true</code> if the specified object is writable,
	 * <code>false</code> otherwise, depending on the permission.
	 * 
	 * @param ho    The data object to check.
	 * @return See above.
	 */
	public boolean isObjectWritable(Object ho);
	
	/**
	 * Deletes the {@link DataObject}s hosted by the passed nodes.
	 * 
	 * @param nodes The nodes hosting the {@link DataObject}s to delete.
	 */
	public void deleteObjects(List nodes);

	/**
	 * Brings up the menu to manage the data.
	 * 
	 * @param index		The index of the menu.
	 * @param invoker   The component that requested the pop-up menu.
	 * @param loc       The point at which to display the menu, relative to the
	 *                  <code>component</code>'s coordinates.
	 */
	void showMenu(int index, Component source, Point point);

	/**
	 * Returns <code>true</code> if there is an on-going import.
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isImporting();

	/** Brings up the importer. */
	void showImporter();

	/** Refreshes the specified browser. */
	void refreshBrowser();
	
	/** Removes the passed nodes from the display. */
	void removeTreeNodes(List<TreeImageDisplay> nodes);

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
}
