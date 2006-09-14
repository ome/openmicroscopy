/*
 * org.openmicroscopy.shoola.agents.treemng.browser.Browser
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

package org.openmicroscopy.shoola.agents.treeviewer.browser;




//Java imports
import java.awt.Component;
import java.awt.Point;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;
import pojos.DataObject;
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
    public static final int     	NEW = 1;
    
    /** Flag to denote the <i>Loading Data</i> state. */
    public static final int     	LOADING_DATA = 2;
    
    /** Flag to denote the <i>Loading Data</i> state. */
    public static final int     	LOADING_LEAVES = 3;
    
    /** Flag to denote the <i>Counting items</i> state. */
    public static final int     	COUNTING_ITEMS = 4;
    
    /** Flag to denote the <i>Ready</i> state. */
    public static final int     	READY = 5;
    
    /** Flag to denote the <i>Discarded</i> state. */
    public static final int     	DISCARDED = 6;
   
    /** 
     * Indicates that the browser corresponds to an <code>Hierarchy</code>
     * explorer.
     */
    public static final int     	PROJECT_EXPLORER = 100;
    
    /** 
     * Indicates that the browser corresponds to an <code>Category</code>
     * explorer.
     */
    public static final int     	CATEGORY_EXPLORER = 101;
    
    /** 
     * Indicates that the browser corresponds to an <code>Images</code>
     * explorer.
     */
    public static final int     	IMAGES_EXPLORER = 102;
    
    /** Indicates to sort the nodes by date. */
    public static final int         SORT_NODES_BY_DATE = 300;
    
    /** Indicates to sort the nodes by name. */
    public static final int         SORT_NODES_BY_NAME = 301;
    
    /** 
     * Bound property name indicating the data retrieval is finished. 
     */
    public static final String  	ON_END_LOADING_PROPERTY = "onEndLoading";
    
    /** 
     * Bound property name indicating a new node is selected. 
     */
    public static final String  	SELECTED_DISPLAY_PROPERTY = 
        								"selectedDisplay";
    
    /** 
     * Bound property name indicating to remove the browser from the display. 
     */
    public static final String  	CLOSE_PROPERTY = "close";
    
    /** Bound property name indicating to bring up the popup menu.  */
    public static final String  	POPUP_MENU_PROPERTY = "popupMenu";
  
    /** 
     * The browser's title corresponding to {@link #PROJECT_EXPLORER} type.
     */
    public static final String     HIERARCHY_TITLE = "Projects";
    
    /** 
     * The browser's title corresponding to {@link #CATEGORY_EXPLORER} type.
     */
    public static final String     CATEGORY_TITLE = "Categories";
    
    /** 
     * The browser's title corresponding to {@link #IMAGES_EXPLORER} type.
     */
    public static final String     IMAGES_TITLE = "Images";
    
    
    /**
     * Sets the filter type.
     * 
     * @param type The value to set.
     */
    void setFilterType(int type);
    
    
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
     * Callback used by a data loader to set the nodes of the retrieved 
     * hierarchy.
     * 
     * @param nodes The root nodes.
     * @throws IllegalStateException If the current state is not 
     *                               {@link #LOADING_DATA}.
     */
    public void setNodes(Set nodes);
    
    /**
     * Callback used by a data loader to set the leaves contained in the 
     * currently selected node.
     * 
     * @param leaves The collection of leaves.
     * @throws IllegalStateException If the current state is not 
     *                               {@link #LOADING_LEAVES}.
     */
    public void setLeaves(Set leaves);
    
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
     * Loads the hierachy e.g. <code>Project/Dataset</code>,
     * <code>CategoryGroup/Category</code>.
     */
    public void loadData();
    
    /**
     * Loads the images contained in the selected container.
     * The user has to first select the container i.e. datasets or categories
     * then the images are retrieved. 
     */
    public void loadFilteredImagesForHierarchy();
    
    /**
     * Loads the children of the nodes specified by the collection 
     * of <code>CategoryData</code> or <code>DatasetData</code>.
     * 
     * @param nodeIDs The collection of <code>DataObject</code>.
     */
    public void loadFilteredImageData(Set nodeIDs);

    /**
     * Retrieves the images contained in a <code>Dataset</code> or 
     * <code>Category</code>.
     */
    public void loadLeaves();
    
    /** Brings up on screen the popup menu. */
    public void showPopupMenu();   
    
    /**
     * Sets the nodes retrieved using a filtering data loading.
     * 
     * @param nodes The collection of nodes to set.
     * @param type	The type of filter used.
     */
    public void setFilterNodes(Set nodes, int type);
    
    /** 
     * Brings up the <code>Filter menu</code> at the specified location 
     * and for the specified component.
     * 
     * @param c The invoking component. 
     * @param p The location.
     */
    public void showFilterMenu(Component c, Point p);
    
    /** 
     * Reloads the children of the currently selected node and rebuilds
     * the display.
     */
    public void refresh();
    
    /** 
     * Reloads children of the currently selected node and rebuilds
     * the display if the <code>Browser</code> is currently selected, 
     * if not, all the chidren are removed and there is no data loading.
     */
    public void refreshTree();
    
    /** 
     * Adds the specified nodes to the currently selected
     * {@link TreeImageDisplay}.
     * 
     * @param nodes Collection to set.
     * @param parent 	The parent hosting the nodes if <code>null</code> the 
     * 					nodes will be added to the root node.
     */
    public void setContainerNodes(Set nodes, TreeImageDisplay parent);
    
    /**
     * Returns the level of the root. One of the following constants:
     * {@link TreeViewer#GROUP_ROOT} and {@link TreeViewer#USER_ROOT}. 
     * 
     * @return See above.
     */
    public int getRootLevel();
    
    /**
     * The id of the root level.
     * 
     * @return See above.
     */
    public long getRootID();
    
    /**
     * Sets the number of items contained in the specified container.
     * 
     * @param containerID 	The id of the container.
     * @param value			The number of items contained in the container.
     */
    public void setContainerCountValue(long containerID, int value);
    
    /**
     * Returns a collection of containers which contain <code>Image</code>s
     * e.g. a <code>Dataset</code>.
     * 
     * @return See above.
     */
    public Set getContainersWithImagesNodes();
    
    /**
     * Returns a collection of containers which contain <code>Image</code>s
     * e.g. a <code>Dataset</code>.
     * 
     * @return See above.
     */
    public Set getContainersWithImages();
    
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
     * @param op        The type of operation.
     */
    public void refreshEdition(DataObject object, int op);
    
    /**
     *  
     * Refreshes the nodes hosting the specified <code>DataObject</code>.
     *
     * @param images        The image classified or declassified. Mustn't 
     *                      be <code>null</code>.
     * @param categories    The categories the image was added to or 
     *                      removed from. Mustn't be <code>null</code>.
     * @param op            The type of operation i.e. classification or 
     *                      declassification.
     */
    public void refreshClassification(ImageData[] images, Set categories,
                                        int op);

    /**
     * Returns the images objects. This method should be invoked
     * when the brower's type is {@link #IMAGES_EXPLORER}
     * 
     * @return See above.
     */
    public Set getLeaves();

    /**
     * Sets the nodes as the selected nodes. Should only be 
     * <code>image</code> nodes.
     * 
     * @param nodes             The nodes to set.
     */
    public void setSelectedDisplays(TreeImageDisplay[] nodes);
    
}
