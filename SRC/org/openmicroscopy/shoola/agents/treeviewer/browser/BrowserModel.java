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
import java.util.HashSet;
import java.util.Iterator;
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
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
import pojos.ImageData;
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
    
    /** The collection of selected nodes in the visualization tree. */
    private Set                selectedNodes;
    
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
    
    /** Indicates if it's the main which is visible. */
    private boolean             mainTree;
    
    /** Flag to indicate if the browser is visible or not. */
    private boolean             displayed;
    
    /** Reference to the parent. */
    private TreeViewer          parent;
    
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
            case Browser.PROJECT_EXPLORER:
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
     * @param parent        Reference to the parent.                  
     */
    protected BrowserModel(int browserType, TreeViewer parent)
    { 
        state = Browser.NEW;
        this.parent = parent;
        checkBrowserType(browserType);
        this.browserType = browserType;
        clickPoint = null;
        filterType = Browser.IN_DATASET_FILTER;
        foundNodeIndex = -1;
        selectedNodes = new HashSet();
        mainTree = true;
        displayed = true;
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
     * Returns the level of the root. 
     * One of the following constants: 
     * {@link TreeViewer#GROUP_ROOT} or {@link TreeViewer#USER_ROOT}.
     * 
     * @return See above.
     */
    int getRootLevel() { return parent.getRootLevel(); }
    
    /** 
     * Returns the root ID.
     * 
     * @return See above.
     */
    long getRootGroupID() { return parent.getRootGroupID(); }
    
    /**
     * Returns the currently selected node.
     * 
     * @return See above.
     */
    TreeImageDisplay getLastSelectedDisplay()
    { 
        int n = selectedNodes.size();
        if (n == 0) return null;
        Iterator i = selectedNodes.iterator();
        int index = 0;
        while (i.hasNext()) {
            if (index == (n-1)) return (TreeImageDisplay) i.next();
            index++;
        }
        return null;
    }
    
    /**
     * Returns an array with all the selected nodes.
     * 
     * @return See above.
     */
    TreeImageDisplay[] getSelectedDisplays()
    {
        if (selectedNodes.size() == 0) return new TreeImageDisplay[0];
        return (TreeImageDisplay[]) selectedNodes.toArray(
                new TreeImageDisplay[selectedNodes.size()]);
    }
    
    /**
     * Sets the selected nodes.
     * 
     * @param nodes The nodes to set.
     */
    void setSelectedDisplays(TreeImageDisplay[] nodes) 
    {
        selectedNodes.removeAll(selectedNodes);
        for (int i = 0; i < nodes.length; i++)
            selectedNodes.add(nodes[i]);
    }
    
    /**
     * Sets the currently selected node.
     * 
     * @param selectedDisplay The selected node.
     */
    void setSelectedDisplay(TreeImageDisplay selectedDisplay)
    {
        selectedNodes.removeAll(selectedNodes);
        if (selectedDisplay == null) return;
        selectedNodes.add(selectedDisplay);
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
        if (browserType == Browser.PROJECT_EXPLORER) 
            currentLoader = new HierarchyLoader(component, 
                                    HierarchyLoader.PROJECT);
        else if (browserType == Browser.CATEGORY_EXPLORER)
            currentLoader = new HierarchyLoader(component,
                                HierarchyLoader.CATEGORY_GROUP);
        else throw new IllegalArgumentException("BrowserType not valid.");
        currentLoader.load();
        state = Browser.LOADING_DATA;
    }
    
    /**
     * Starts the asynchronous retrieval of the hierarchy objects needed
     * by this model and sets the state to {@link Browser#LOADING_DATA}. 
     * 
     * @param nodes The Collection of <code>DataObject</code> nodes.
     */
    void fireFilteredImageDataLoading(Set nodes)
    {
        Set ids = new HashSet(nodes.size());
        Iterator i = nodes.iterator();
        if (filterType == Browser.IN_DATASET_FILTER) {
            while (i.hasNext())
                ids.add(new Long(((DatasetData) i.next()).getId()));
            currentLoader = new ImagesInContainerLoader(component, 
                                            DatasetData.class, ids, true);
        } else if (filterType == Browser.IN_CATEGORY_FILTER) {
            while (i.hasNext())
                ids.add(new Long(((CategoryData) i.next()).getId()));
            currentLoader = new ImagesInContainerLoader(component, 
                                                CategoryData.class, ids, true);
        }
        currentLoader.load();
        state = Browser.LOADING_DATA;
    }
    
    /**
     * Starts the asynchronous retrieval of the leaves contained in the 
     * currently selected <code>TreeImageDisplay</code> objects needed
     * by this model and sets the state to {@link Browser#LOADING_LEAVES}. 
     */
    void fireLeavesLoading()
    {
        Object ho = getLastSelectedDisplay().getUserObject();
        long id = 0;
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
     * by this model and sets the state to {@link Browser#LOADING_DATA}
     * depending on the value of the {@link #filterType}. 
     */
    void fireFilterDataLoading()
    {
        if (filterType == Browser.IN_DATASET_FILTER)
            currentLoader = new HierarchyLoader(component,
                                    HierarchyLoader.DATASET, false, true);
        else if (filterType == Browser.IN_CATEGORY_FILTER)
            currentLoader = new HierarchyLoader(component,
                                    HierarchyLoader.CATEGORY, false, true);
        else currentLoader = new ImagesLoader(component);
        currentLoader.load();
        state = Browser.LOADING_DATA;
    }
    
    /**
     * Starts the asynchronous retrieval of the data 
     * and sets the state to {@link Browser#LOADING_DATA}.
     */
    void fireContainerLoading()
    {
        TreeImageDisplay selectedDisplay = getLastSelectedDisplay();
        if (selectedDisplay == null) return;
        Object ho = selectedDisplay.getUserObject();
        long id = -1;
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
        TreeImageDisplay selectedDisplay = getLastSelectedDisplay();
        if (selectedDisplay == null) return;
        Object ho = selectedDisplay.getUserObject();
        if ((ho instanceof DatasetData) || (ho instanceof CategoryData))
            fireLeavesLoading();
        else fireContainerLoading();
    }

    /**
     * Sets the number of items contained in the specified container.
     *  
     * @param tree The component hosting the node.
     * @param containerID The ID of the container.
     * @param value	The number of items.
     */
    void setContainerCountValue(JTree tree, long containerID, int value)
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
    
    /**
     * Returns the user's id. Helper method
     * 
     * @return See above.
     */
    long getUserID() { return parent.getUserDetails().getId(); }

    /**
     * Brings up the viewer if the last selected data object 
     * is an <code>Image</code>.
     */
    void viewDataObject()
    {
        TreeImageDisplay d  = getLastSelectedDisplay();
        if (d == null) return;
        if (d.getUserObject() instanceof ImageData) {
            ViewCmd cmd = new ViewCmd(parent);
            cmd.execute();
        }
    }
    
    /**
     * Flag to indicate if the main is displayed on screen.
     * 
     * @return See above.
     */
    boolean isMainTree() { return mainTree; }
    
    /**
     * Indicates which tree is currently displayed in the browser.
     * 
     * @param b Pass <code>true</code> for the main tree, <code>false</code>
     *          otherwise.
     */
    void setMainTree(boolean b) { mainTree = b; }
    
    /**
     * Returns the parent of the component.
     * 
     * @return See above.
     */
    TreeViewer getParentModel() { return parent; }
    
    /**
     * Returns <code>true</code> if the browser is displayed on screen,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isDisplayed() { return displayed; }
    
    /**
     * Sets the {@link #displayed} flag. 
     * 
     * @param displayed Pass <code>true</code> to indicate the browser is on 
     *                  screen, <code>false</code> otherwise.
     */
    void setDisplayed(boolean displayed) { this.displayed = displayed; }
    
}
