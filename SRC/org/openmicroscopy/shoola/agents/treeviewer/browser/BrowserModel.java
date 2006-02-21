/*
 * org.openmicroscopy.shoola.agents.treemng.browser.BrowserModel
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
import java.awt.Point;
import java.util.List;
import java.util.Set;
import javax.swing.JTree;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.ContainerCounterLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ContainerLoader;
import org.openmicroscopy.shoola.agents.treeviewer.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.treeviewer.HierarchyLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ImagesInContainerLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ImagesLoader;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ProjectData;

/** 
 * The Model component in the <code>Browser</code> MVC triad.
 * This class tracks the <code>Browser</code>'s state and knows how to
 * initiate data retrievals. It also knows how to store and manipulate
 * the results. However, this class doesn't know the actual hierarchy
 * the <code>Browser</code> is for.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BrowserModel
{
    
    /** The type of Browser. */
    private int                 browserType;
    
    /** The currently selected node in the visualization tree. */
    private TreeImageDisplay    selectedDisplay;
    
    /** Holds one of the state flags defined by {@link Browser}. */
    private int                 state;
     
    /** The point where the mouse clicked event occured. */
    private Point               clickPoint;
    
    /** 
     * Will either be a hierarchy loader or 
     * <code>null</code> depending on the current state. 
     */
    private DataBrowserLoader	currentLoader;
    
    /** The type of filter. */
    private int                 filterType;
    
    /** 
     * The level of the hierarchy root. One of the following constants:
     * {@link TreeViewer#WORLD_ROOT}, {@link TreeViewer#GROUP_ROOT} and
     * {@link TreeViewer#USER_ROOT}.
     */
    private int 				rootLevel;
    
    /** 
     * The ID of the root. This parameter will be used only when the 
     * {@link #rootLevel} is {@link TreeViewer#GROUP_ROOT}.
     */
    private int					rootID;
    
    /** List of founds nodes. */
    private List				foundNodes;
    
    /** The index of the currently selected found node. */
    private int					foundNodeIndex;
    
    /** 
     * Maps an container id to the list of number of items providers for that 
     * container.
     */
    private ContainersManager	containersManager;
    
    /** Indicates if the browser is currently selected. */
    private boolean				selected;
    
    /** Reference to the component that embeds this model. */
    protected Browser           component; 
    
    /** 
     * Checks if the specified browser is valid.
     * 
     * @param type The type to check.
     */
    private void checkBrowserType(int type)
    {
        switch (type) {
            case Browser.HIERARCHY_EXPLORER:
            case Browser.CATEGORY_EXPLORER:
            case Browser.IMAGES_EXPLORER:    
                break;
            default:
                throw new IllegalArgumentException("Browser type not valid.");
        }
    }
    
    /**
     * Creates a new object and sets its state to {@link Browser#NEW}.
     * 
     * @param browserType   The browser's type. One of the type defined by
     *                      the {@link Browser}.
     */
    protected BrowserModel(int browserType)
    { 
        state = Browser.NEW;
        checkBrowserType(browserType);
        this.browserType = browserType;
        clickPoint = null;
        filterType = -1;
        foundNodeIndex = -1;
    }

    /**
     * Called by the <code>Browser</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Browser component) { this.component = component; }
    
    /**
     * Returns the current state.
     * 
     * @return One of the state constants defined by the {@link Browser}.  
     */
    int getState() { return state; }
    
    /**
     * Sets the current state.
     * 
     * @param state The current state.
     *              One of the state constants defined by the {@link Browser}.
     */
    void setState(int state) { this.state = state; }
    
    /**
     * Returns the currently selected node.
     * 
     * @return See above.
     */
    TreeImageDisplay getSelectedDisplay() { return selectedDisplay; }
    
    /**
     * Sets the currently selected node.
     * 
     * @param selectedDisplay The selected node.
     */
    void setSelectedDisplay(TreeImageDisplay selectedDisplay)
    {
        this.selectedDisplay = selectedDisplay;
    }
    
    /**
     * Returns the location of the mouse click.
     * 
     * @return See above.
     */
    Point getClickPoint() { return clickPoint; }
    
    /**
     * Sets the location of the mouse click.
     * 
     * @param p The location to set.
     */
    void setClickPoint(Point p) { clickPoint = p; }
    
    /**
     * Returns the type of the browser.
     * 
     * @return See above.
     */
    int getBrowserType() { return browserType; }
    
    /**
     * Starts the asynchronous retrieval of the hierarchy objects needed
     * by this model and sets the state to {@link Browser#LOADING_DATA}. 
     */
    void fireDataLoading()
    {
        if (browserType == Browser.HIERARCHY_EXPLORER) 
            currentLoader = new HierarchyLoader(component, 
                                    HierarchyLoader.PROJECT);
        else if (browserType == Browser.CATEGORY_EXPLORER)
            currentLoader = new HierarchyLoader(component,
                                HierarchyLoader.CATEGORY_GROUP);
        else if (browserType == Browser.IMAGES_EXPLORER)
            currentLoader = new ImagesLoader(component);
        else throw new IllegalArgumentException("BrowserType not valid.");
        currentLoader.load();
        state = Browser.LOADING_DATA;
    }
    
    /**
     * Starts the asynchronous retrieval of the hierarchy objects needed
     * by this model and sets the state to {@link Browser#LOADING_DATA}. 
     * 
     * @param nodeIDs Collection of containers' IDs.
     */
    void fireDataLoading(Set nodeIDs)
    {
        if (browserType != Browser.IMAGES_EXPLORER)
            throw new IllegalArgumentException("BrowserType not valid.");
        
        if (filterType == -1) 
            throw new IllegalArgumentException("Filter type not valid.");
        Class nodeType = null;
        if (filterType == HierarchyLoader.DATASET) nodeType = DatasetData.class;
        else if (filterType == HierarchyLoader.CATEGORY)
            nodeType = CategoryData.class;
        currentLoader = new ImagesInContainerLoader(component, nodeType, 
                                                    nodeIDs, true);
        currentLoader.load();
        filterType = -1;
        state = Browser.LOADING_DATA;
    }
    
    /**
     * Starts the asynchronous retrieval of the leaves contained in the 
     * currently selected <code>TreeImageDisplay</code> objects needed
     * by this model and sets the state to {@link Browser#LOADING_LEAVES}. 
     */
    void fireLeavesLoading()
    {
        Object ho = selectedDisplay.getUserObject();
        int id = 0;
        Class nodeType = null;
        if (ho instanceof DatasetData) {
            nodeType = DatasetData.class;
            id = ((DatasetData) ho).getId();
        } else if (ho instanceof CategoryData) {
            nodeType = CategoryData.class;
            id = ((CategoryData) ho).getId();
        } else 
            throw new IllegalArgumentException("Not valid selected display");
        state = Browser.LOADING_LEAVES;
        currentLoader = new ImagesInContainerLoader(component, nodeType, id);
        currentLoader.load();
    }

    /**
     * Starts the asynchronous retrieval of the hierarchy objects needed
     * by this model and sets the state to {@link Browser#LOADING_DATA}. 
     * 
     * @param type The type of filter.
     */
    void fireFilterDataLoading(int type)
    {
        if (type == Browser.DATASET_CONTAINER)
            currentLoader = new HierarchyLoader(component,
                                    HierarchyLoader.DATASET, false, true);
        else if (type == Browser.CATEGORY_CONTAINER)
            currentLoader = new HierarchyLoader(component,
                    HierarchyLoader.CATEGORY, false, true);
        currentLoader.load();
        state = Browser.LOADING_DATA;
    }
    
    /**
     * Starts the asynchronous retrieval of the data 
     * and sets the state to {@link Browser#LOADING_DATA}.
     */
    void fireContainerLoading()
    {
        if (selectedDisplay == null) return;
        Object ho = selectedDisplay.getUserObject();
        int id = -1;
        Class nodeType = null;
        if (ho instanceof ProjectData) {
            id = ((ProjectData) ho).getId();
            nodeType = ProjectData.class;
        } else if (ho instanceof CategoryGroupData) {
            id = ((CategoryGroupData) ho).getId();
            nodeType = CategoryGroupData.class;
        }
        if (nodeType != null) {
            currentLoader = new ContainerLoader(component, nodeType, id);
            currentLoader.load();
            state = Browser.LOADING_DATA;
        }
    }
    
    /**
     * Starts the asynchronous retrieval of the number of items contained 
     * in the <code>TreeImageSet</code> containing images e.g. a 
     * <code>Dataset</code> and sets the state to {@link Browser#COUNTING_ITEMS}
     */
    void fireContainerCountLoading()
    {
        Set containers = component.getContainersWithImages();
        if (containers.size() == 0) {
            state = Browser.READY;
            return;
        }
        state = Browser.COUNTING_ITEMS;
        currentLoader = new ContainerCounterLoader(component, containers);
        currentLoader.load();
    }
    
    /**
     * Sets the object in the {@link Browser#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
        cancel();
        state = Browser.DISCARDED;
    }
    
    /** 
     * Cancels any ongoing data loading and sets the state to 
     * {@link Browser#READY}.
     */
    void cancel()
    {
        if (currentLoader != null) {
            currentLoader.cancel();
            currentLoader = null;
        }
        state = Browser.READY;
    }
    
    /**
     * Sets the filter used.
     * 
     * @param type The type of filter.
     */
    void setFilterType(int type) { filterType = type; }
    
    /**
     * Returns the type of filter currently used.
     * 
     * @return See above.
     */
    int getFilterType() { return filterType; }
    
    /**
     * Starts the asynchronous retrieval of the data 
     * according to the <code>UserObject</code> type.
     */
    void refreshSelectedDisplay()
    {
        if (selectedDisplay == null) return;
        Object ho = selectedDisplay.getUserObject();
        if ((ho instanceof DatasetData) || (ho instanceof CategoryData))
            fireLeavesLoading();
        else fireContainerLoading();
    }
    
    /**
     * Sets the root of the retrieved hierarchies. 
     * The rootID is taken into account if and only if 
     * the passed <code>rootLevel</code> is {@link TreeViewer#GROUP_ROOT}.
     * 
     * @param rootLevel The level of the root. One of the following constants:
     * 					{@link TreeViewer#WORLD_ROOT}, 
     * 					{@link TreeViewer#GROUP_ROOT} and
     * 					{@link TreeViewer#USER_ROOT}.
     * @param rootID	The Id of the root.
     */
    void setHierarchyRoot(int rootLevel, int rootID)
    {
    	this.rootLevel = rootLevel;
    	this.rootID = rootID;
    }
    
    /**
     * Returns the level of the root. 
     * One of the following constants: {@link TreeViewer#WORLD_ROOT},
     * {@link TreeViewer#GROUP_ROOT} and {@link TreeViewer#USER_ROOT}.
     * 
     * @return See above.
     */
    int getRootLevel() { return rootLevel; }
    
    /** 
     * Returns the root ID.
     * 
     * @return See above.
     */
    int getRootID() { return rootID; }

    /**
     * Sets the number of items contained in the specified container.
     *  
     * @param tree The component hosting the node.
     * @param containerID The ID of the container.
     * @param value	The number of items.
     */
    void setContainerCountValue(JTree tree, int containerID, int value)
    {
        if (containersManager == null)
            containersManager = new ContainersManager(tree, 
                    			component.getContainersWithImagesNodes());
        containersManager.setNumberItems(containerID, value);
        if (containersManager.isDone()) {
            state = Browser.READY;
            containersManager = null;
        }
    }
    
    /**
     * Sets the value of the {@link #selected} field.
     * 
     * @param selected The value to set.
     */
    void setSelected(boolean selected) { this.selected = selected; }
    
    /**
     * Returns <code>true</code> if the {@link Browser} is selected, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isSelected() { return selected; }
    
    
    /**
     * Sets the list of found nodes.
     * 
     * @param nodes The collection of found nodes.
     */
    void setFoundNodes(List nodes) { foundNodes = nodes; }
    
    /**
     * Sets the index of the found node.
     * 
     * @param i The index of the node.
     */
    void setFoundNodeIndex(int i) { foundNodeIndex = i; }
    
    /**
     * Returns the index of the node found.
     * 
     * @return See above.
     */
    int getFoundNodeIndex() { return foundNodeIndex; }
    
    /**
     * Returns a collection of found nodes.
     * 
     * @return See above.
     */
    List getFoundNodes() { return foundNodes; }
    
}
