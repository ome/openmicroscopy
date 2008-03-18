/*
 * org.openmicroscopy.shoola.agents.treemng.browser.BrowserModel
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
import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.metadata.ViewMetadata;
import org.openmicroscopy.shoola.agents.treeviewer.ContainerCounterLoader;
import org.openmicroscopy.shoola.agents.treeviewer.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExperimenterDataLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExperimenterImageLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExperimenterImagesCounter;
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDataLoader;
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDef;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.event.EventBus;

import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
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
    
    /** 
	 * Will either be a data loader or
	 * <code>null</code> depending on the current state. 
	 */
	private DataBrowserLoader	numberLoader;
	
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
    
    /** Indicates if the browser is visible or not. */
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
     * @param experimenter  The experimenter this browser is for.                  
     */
    protected BrowserModel(int browserType, TreeViewer parent)
    { 
        state = Browser.NEW;
        this.parent = parent;
        checkBrowserType(browserType);
        this.browserType = browserType;
        clickPoint = null;
        foundNodeIndex = -1;
        selectedNodes = new HashSet();
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
     * Returns the root ID.
     * 
     * @return See above.
     */
    long getRootID() { return parent.getUserDetails().getId(); }
    
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
        TreeImageDisplay node;
        for (int i = 0; i < nodes.length; i++) {
            node = nodes[i];
            if (node != null)
                selectedNodes.add(node);
        }    
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
        //if (selectedDisplay.getUserObject() instanceof String) return;
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
     * Starts the asynchronous retrieval of the leaves contained in the 
     * currently selected <code>TreeImageDisplay</code> objects needed
     * by this model and sets the state to {@link Browser#LOADING_LEAVES}. 
     */
    void fireLeavesLoading()
    {
    	/*
    	TreeImageDisplay n = getLastSelectedDisplay();
        if (n instanceof TreeImageNode) return;
        
        Object ho = n.getUserObject();
        int type = -1;
        if (ho instanceof DatasetData) type = HierarchyLoader.DATASET;
        else if (ho instanceof CategoryData) type = HierarchyLoader.CATEGORY;
        if (type == -1) return;
        state = Browser.LOADING_LEAVES;
        currentLoader = new HierarchyLoader(component, type, (TreeImageSet) n);
        currentLoader.load();
        */
    }

    /**
     * Starts the asynchronous retrieval of the leaves contained in the 
     * currently selected <code>TreeImageDisplay</code> objects needed
     * by this model and sets the state to {@link Browser#LOADING_LEAVES}.
     * 
	 * @param expNode 	The node hosting the experimenter.
	 * @param node		The parent of the data. Pass <code>null</code>
	 * 					to retrieve all data.	
     */
    void fireLeavesLoading(TreeImageDisplay expNode, TreeImageDisplay node)
    {
    	state = Browser.LOADING_LEAVES;
    	if (node instanceof TreeImageTimeSet) {
    		currentLoader = new ExperimenterImageLoader(component, 
					(TreeImageSet) expNode, (TreeImageTimeSet) node);
    	} else {
    		Object ho = node.getUserObject();
            int type = -1;
            if (ho instanceof DatasetData) 
            	type = ExperimenterDataLoader.DATASET;
            else if (ho instanceof CategoryData) 
            	type = ExperimenterDataLoader.CATEGORY;
            currentLoader = new ExperimenterDataLoader(component, type, 
            					(TreeImageSet) expNode, (TreeImageSet) node);
    	}
        currentLoader.load();
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
        //state = Browser.COUNTING_ITEMS;
        //currentLoader = new ContainerCounterLoader(component, containers);
        //currentLoader.load();
        numberLoader = new ContainerCounterLoader(component, containers);
        numberLoader.load();
    }
    
    /**
     * Sets the object in the {@link Browser#DISCARDED} state.
     * Any ongoing data loading will be cancelled.
     */
    void discard()
    {
        cancel();
        if (numberLoader != null) {
        	numberLoader.cancel();
        	numberLoader = null;
        }
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
     * Sets the number of items contained in the specified container.
     * Returns <code>true</code> if all the nodes have been visited,
     * <code>false</code> otherwise.
     * 
     * @param tree The component hosting the node.
     * @param containerID The ID of the container.
     * @param value	The number of items.
     * @return See above.
     */
    boolean setContainerCountValue(JTree tree, long containerID, int value)
    {
        if (containersManager == null)
            containersManager = new ContainersManager(tree, 
                    			component.getContainersWithImagesNodes());
        containersManager.setNumberItems(containerID, value);
        if (containersManager.isDone()) {
            //state = Browser.READY;
            containersManager = null;
            numberLoader = null;
            return true;
        }
        return false;
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
     * Returns the id to the group selected for the current user.
     * 
     * @return See above.
     */
    long getUserGroupID() { return parent.getUserGroupID(); }
    
    /**
     * Returns the details of the user currently logged in.
     * 
     * @return See above.
     */
    ExperimenterData getUserDetails()
    { 
    	return (ExperimenterData) TreeViewerAgent.getRegistry().lookup(
				LookupNames.CURRENT_USER_DETAILS);
    }
    
    /**
     * Brings up the viewer if the last selected data object 
     * is an <code>Image</code>.
     */
    void viewDataObject()
    {
        TreeImageDisplay d  = getLastSelectedDisplay();
        if (d == null) return;
        Object o = d.getUserObject();
        if (o instanceof ImageData) {
            ViewCmd cmd = new ViewCmd(parent, (DataObject) o);
            cmd.execute();
        }
    }
    
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

    /**
     * Returns the first name and the last name of the currently 
     * selected experimenter as a String.
     * 
     * @return See above.
     */
	String getExperimenterNames() { return parent.getExperimenterNames(); }

	 /**
     * Starts the asynchronous retrieval of the hierarchy objects needed
     * by this model and sets the state to {@link Browser#LOADING_DATA}
     * depending on the value of the {@link #filterType}. 
     * 
     * @param expNode 	The node hosting the experimenter.
     */
	void fireExperimenterDataLoading(TreeImageSet expNode)
	{
		int index = -1;
		switch (browserType) {
			case Browser.PROJECT_EXPLORER:
				index = ExperimenterDataLoader.PROJECT;
				break;
			case Browser.CATEGORY_EXPLORER:
				index = ExperimenterDataLoader.CATEGORY_GROUP;
				break;
			case Browser.IMAGES_EXPLORER:
				index = ExperimenterDataLoader.IMAGE;
				break;
		}
		currentLoader = new ExperimenterDataLoader(component, index, expNode);
        currentLoader.load();
        state = Browser.LOADING_DATA;
	}
	
	/** 
     * Reloads the experimenter data.
     * 
     * @param nodes
     */
    void loadRefreshExperimenterData(Map<Long, RefreshExperimenterDef> nodes)
    {
        Class klass = null;
        switch (browserType) {
			case Browser.PROJECT_EXPLORER:
				klass = ProjectData.class;
				break;
			case Browser.CATEGORY_EXPLORER:
				klass = CategoryGroupData.class;
				break;
			case Browser.IMAGES_EXPLORER:
				klass =  ImageData.class;
				break;
		}
        state = Browser.LOADING_DATA;
        currentLoader = new RefreshExperimenterDataLoader(component, klass,
        												nodes);
        currentLoader.load();   
    }

    /**
     * Fires an asynchronous call to retrieve the number of images
     * imported by the experimenter.
     * 
     * @param expNode The node hosting the experimenter.
     */
	void fireCountExperimenterImages(TreeImageSet expNode)
	{
		List<TreeImageTimeSet> n = expNode.getChildrenDisplay();
		Iterator i = n.iterator();
		Set indexes = new HashSet(n.size());
		TreeImageTimeSet node;
		while (i.hasNext()) {
			node = (TreeImageTimeSet) i.next();
			indexes.add(node.getType());
		}
		if (containersManager == null)
            containersManager = new ContainersManager(indexes);
		//state = Browser.COUNTING_ITEMS;
        numberLoader = new ExperimenterImagesCounter(component, expNode, n);
        numberLoader.load();  
	}
	
	/**
	 * Indicates that the node with specified index is done.
	 * Returns <code>true</code> if all the nodes have been visited,
     * <code>false</code> otherwise.
     * 
	 * @param expNode	The node hosting the experimenter.
	 * @param index		The index of the node.
	 * @return See above.
	 */
	boolean setExperimenterCount(TreeImageSet expNode, int index) 
	{
		if (containersManager == null) return true;
		containersManager.setItem(index);
		if (containersManager.isDone()) {
			//state = Browser.READY;
			containersManager = null;
			numberLoader = null;
			return true;
		}
		return false;
	}
	
}
