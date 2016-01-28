/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;

import org.openmicroscopy.shoola.agents.treeviewer.AdminLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ContainerCounterLoader;
import org.openmicroscopy.shoola.agents.treeviewer.DataBrowserLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExperimenterDataLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExperimenterImageLoader;
import org.openmicroscopy.shoola.agents.treeviewer.ExperimenterImagesCounter;
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDataLoader;
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDef;
import org.openmicroscopy.shoola.agents.treeviewer.ScreenPlateLoader;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.FSAccessException;
import org.openmicroscopy.shoola.env.data.FSFileSystemView;
import omero.gateway.SecurityContext;
import omero.log.LogMessage;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FileData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

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
 * @since OME2.2
 */
class BrowserModel
{
    
    /** The type of Browser. */
    private int                 	browserType;
    
    /** The collection of selected nodes in the visualization tree. */
    private List<TreeImageDisplay>	selectedNodes;
    
    /** Holds one of the state flags defined by {@link Browser}. */
    private int                 	state;
     
    /** The point where the mouse clicked event occurred. */
    private Point               	clickPoint;
    
    /** 
     * Will either be a hierarchy loader or 
     * <code>null</code> depending on the current state. 
     */
    private DataBrowserLoader		currentLoader;
    
    /** 
	 * Will either be a data loader or
	 * <code>null</code> depending on the current state. 
	 */
	private DataBrowserLoader		numberLoader;
	
    /** List of founds nodes. */
    private List					foundNodes;
    
    /** The index of the currently selected found node. */
    private int						foundNodeIndex;
    
    /** 
     * Maps an container id to the list of number of items providers for that 
     * container.
     */
    private ContainersManager		containersManager;
    
    /** 
     * Maps an container id to the list of number of items providers for that 
     * container.
     */
    private ContainersManager		containersManagerWithIndexes;
    
    /** Indicates if the browser is currently selected. */
    private boolean					selected;
    
    /** Indicates if the browser is visible or not. */
    private boolean             	displayed;
    
    /** The collection of previously imported images. */
    private Map<String, Object>		importedImages;
    
    /** The repositories to set. */
    private Map<Long, FSFileSystemView>		views;
    
    /** Reference to the parent. */
    private TreeViewer          	parent;
    
    /** Reference to the component that embeds this model. */
    protected Browser           	component; 
    
    /** The security context for the administrator.*/
    //private SecurityContext adminContext;
    
    /** 
     * Checks if the specified browser is valid.
     * 
     * @param type The type to check.
     */
    private void checkBrowserType(int type)
    {
        switch (type) {
            case Browser.PROJECTS_EXPLORER:
            case Browser.IMAGES_EXPLORER:
            case Browser.TAGS_EXPLORER:
            case Browser.SCREENS_EXPLORER:
            case Browser.FILES_EXPLORER:
            case Browser.FILE_SYSTEM_EXPLORER:
            case Browser.ADMIN_EXPLORER:
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
        selectedNodes = new ArrayList<TreeImageDisplay>();
        displayed = true;
        //adminContext = TreeViewerAgent.getAdminContext();
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
    TreeImageDisplay getLastSelectedDisplay()
    { 
        int n = selectedNodes.size();
        if (n == 0) return null; 
        return selectedNodes.get(n-1);
    }
    
    /**
     * Returns an array with all the selected nodes.
     * 
     * @return See above.
     */
    TreeImageDisplay[] getSelectedDisplays()
    {
        if (selectedNodes.size() == 0) return new TreeImageDisplay[0];
        TreeImageDisplay[] nodes = new TreeImageDisplay[selectedNodes.size()];
        Iterator<TreeImageDisplay> i = selectedNodes.iterator();
        int index = 0;
        while (i.hasNext()) {
        	nodes[index] = i.next();
        	index++;
		}
        return nodes;
    }
    
    /**
     * Sets the selected node.
     * 
     * @param display The selected value.
	 * @param single  Pass <code>true</code> if the method is invoked for
	 *                single selection, <code>false</code> for multi-selection.
     */
    void setSelectedDisplay(TreeImageDisplay display, boolean single)
    {
    	if (single) {
    		selectedNodes.clear();
            if (display != null) selectedNodes.add(display);
    	} else {
    		if (!selectedNodes.contains(display) && display != null)
    			selectedNodes.add(display);
    	}
    }
    
    /**
     * Adds the passed node to the collection of selected nodes.
     * 
     * @param selectedDisplay The node to add.
     */
    void addFoundNode(TreeImageDisplay selectedDisplay)
    {
    	if (selectedDisplay == null) return;
    	//if (!selectedNodes.contains(selectedDisplay))
    	TreeImageDisplay display = getLastSelectedDisplay();
    	if (display != null) {
    		if (!display.getUserObject().getClass().equals(
    				selectedDisplay.getUserObject().getClass()))
    			selectedNodes.clear();
    	}
    	if (!selectedNodes.contains(selectedDisplay))
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
     * 
	 * @param expNode 	The node hosting the experimenter.
	 * @param node		The parent of the data. Pass <code>null</code>
	 * 					to retrieve all data.	
     */
    void fireLeavesLoading(TreeImageDisplay expNode, TreeImageDisplay node)
    {
    	state = Browser.LOADING_LEAVES;
    	SecurityContext ctx = getSecurityContext(expNode);
    	if (node instanceof TreeImageTimeSet || node instanceof TreeFileSet) {
    		currentLoader = new ExperimenterImageLoader(component, ctx,
					(TreeImageSet) expNode, (TreeImageSet) node);
    		 currentLoader.load();
    	} else {
    		Object ho = node.getUserObject();
            if (ho instanceof DatasetData)  {
        		currentLoader = new ExperimenterDataLoader(component, ctx,
        				ExperimenterDataLoader.DATASET, 
        				(TreeImageSet) expNode, (TreeImageSet) node);
        		 currentLoader.load();
        	} else if (ho instanceof TagAnnotationData) {
        		currentLoader = new ExperimenterDataLoader(component, ctx,
        				ExperimenterDataLoader.TAG, 
        				(TreeImageSet) expNode, (TreeImageSet) node);
        		currentLoader.load();
        	} else if (ho instanceof GroupData) {
        		if (TreeViewerAgent.isAdministrator()) 
        			ctx = TreeViewerAgent.getAdminContext();
        		currentLoader = new AdminLoader(component, ctx,
        				(TreeImageSet) expNode);
        		currentLoader.load();
            } else if (ho instanceof FileData) {
            	FileData fa = (FileData) ho;
            	if (fa.isDirectory() && !fa.isHidden()) {
            		
            	}
            }
    	}
    }

    /**
     * Starts the asynchronous retrieval of the number of items contained 
     * in the <code>TreeImageSet</code> containing images e.g. a 
     * <code>Dataset</code> and sets the state to 
     * {@link Browser#COUNTING_ITEMS}.
     * 
     * @param containers The collection of <code>DataObject</code>s.
     * @param nodes      The corresponding nodes.
     */
    void fireContainerCountLoading(Set containers, Set<TreeImageSet> nodes,
    		TreeImageDisplay refNode)
    {
        if (containers == null || containers.size() == 0) {
            state = Browser.READY;
            return;
        }
        //state = Browser.COUNTING_ITEMS;
        SecurityContext ctx = getSecurityContext(refNode);
        if (TreeViewerAgent.isAdministrator() &&
        		getBrowserType() == Browser.ADMIN_EXPLORER)
        	ctx = TreeViewerAgent.getAdminContext();;
        ContainerCounterLoader loader = new ContainerCounterLoader(component,
        		ctx, containers, nodes);
        loader.load();
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
     * @param value The number of items.
     * @param nodes The collection of nodes.
     * @return See above.
     */
    boolean setContainerCountValue(JTree tree, long containerID, long value,
    		Set<TreeImageSet> nodes)
    {
        if (containersManager == null)
            containersManager = new ContainersManager(tree, nodes);
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
    long getUserID() { return getUserDetails().getId(); }
    
    /**
     * Returns the details of the user currently logged in.
     * 
     * @return See above.
     */
    ExperimenterData getUserDetails()
    { 
    	return parent.getUserDetails();
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
		if (browserType == Browser.ADMIN_EXPLORER) {
			state = Browser.LOADING_DATA;
			//Depending on user roles.
			if (TreeViewerAgent.isAdministrator()) {
				currentLoader = new AdminLoader(component,
						TreeViewerAgent.getAdminContext(), null);
				currentLoader.load();
			} else {
				component.setGroups(TreeViewerAgent.getGroupsLeaderOf(), null);
			}
			return;
		}
		SecurityContext ctx = getSecurityContext(expNode);
		if (browserType == Browser.SCREENS_EXPLORER) {
			currentLoader = new ScreenPlateLoader(component, ctx, expNode,
												ScreenPlateLoader.SCREEN);
	        currentLoader.load();
			state = Browser.LOADING_DATA;
			return;
		}
		switch (browserType) {
			case Browser.PROJECTS_EXPLORER:
				index = ExperimenterDataLoader.PROJECT;
				break;
			case Browser.IMAGES_EXPLORER:
				index = ExperimenterDataLoader.IMAGE;
				break;
			case Browser.TAGS_EXPLORER:
				index = ExperimenterDataLoader.TAG_SET;
				break;
			case Browser.FILE_SYSTEM_EXPLORER:
				index = ExperimenterDataLoader.FILE_DATA;
				break;
			case Browser.FILES_EXPLORER:
				//index = ExperimenterDataLoader.FILE;
		}
		if (index == -1) return;
		currentLoader = new ExperimenterDataLoader(component, ctx, index,
				expNode);
        currentLoader.load();
        state = Browser.LOADING_DATA;
	}
	
	/** 
     * Reloads the experimenter data.
     * 
     * @param nodes The objects to refresh.
     * @param type  The type of node to select when refreshed
     * @param id    The identifier of the node.
     * @param refNode  The node to hosting the data object to browse.
     * @param toBrowse The data object to browse.
     */
    void loadRefreshExperimenterData(
    		Map<SecurityContext, RefreshExperimenterDef> nodes, 
    		Class<?> type, long id, Object refNode, DataObject toBrowse)
    {
        Class<?> klass = null;
        switch (browserType) {
			case Browser.PROJECTS_EXPLORER:
				klass = ProjectData.class;
				break;
			case Browser.IMAGES_EXPLORER:
				klass =  ImageData.class;
				break;
			case Browser.TAGS_EXPLORER:
				klass = TagAnnotationData.class;
				break;
			case Browser.SCREENS_EXPLORER:
				klass = ScreenData.class;
				break;
			case Browser.FILES_EXPLORER:
				klass = FileAnnotationData.class;
				break;
			case Browser.ADMIN_EXPLORER:
				klass = GroupData.class;
		}
        state = Browser.LOADING_DATA;
        if (klass == null) return;
        currentLoader = new RefreshExperimenterDataLoader(component,
        		getSecurityContext(null), klass,
        					nodes, type, id, refNode, toBrowse);
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
		SecurityContext ctx = getSecurityContext(expNode);
		List<TreeImageSet> n = expNode.getChildrenDisplay();
		Iterator<TreeImageSet> i = n.iterator();
		Set<Integer> indexes = new HashSet<Integer>();
		switch (getBrowserType()) {
			case Browser.IMAGES_EXPLORER:
				TreeImageTimeSet node;
				TreeImageSet no;
				while (i.hasNext()) {
					no = i.next();
					if (no instanceof TreeImageTimeSet) {
						node = (TreeImageTimeSet) no;
						indexes.add(node.getType());
					}
				}
				break;
			case Browser.FILES_EXPLORER:
			case Browser.TAGS_EXPLORER:
				TreeFileSet file;
				Object o;
				while (i.hasNext()) {
					o = i.next();
					if (o instanceof TreeFileSet) {
						file = (TreeFileSet) o;
						indexes.add(file.getType());
					}
				}
		}

		if (indexes.size() == 0) return;
		if (containersManagerWithIndexes == null)
			containersManagerWithIndexes = new ContainersManager(indexes);
		state = Browser.COUNTING_ITEMS;
        numberLoader = new ExperimenterImagesCounter(component, ctx,
        		expNode, n);
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
		if (containersManagerWithIndexes == null) return true;
		containersManagerWithIndexes.setItem(index);
		if (containersManagerWithIndexes.isDone()) {
			if (state == Browser.COUNTING_ITEMS) {
				if (containersManager == null)
					state = Browser.READY;
			}
			containersManagerWithIndexes = null;
			numberLoader = null;
			return true;
		}
		return false;
	}

	/**
	 * Removes the passed node.
	 * 
	 * @param foundNode The node to remove.
	 */
	void removeDisplay(TreeImageDisplay foundNode)
	{
		if (foundNode != null) selectedNodes.remove(foundNode);
	}

	/**
	 * Views the passed image.
	 * 
	 * @param node The node to handle
	 */
	void browse(TreeImageDisplay node)
	{ 
		if (node == null) return;
		Object object = node.getUserObject();
		if (object instanceof ImageData) parent.browse(node, null, true);
		else if (object instanceof PlateData) {
			if (!node.hasChildrenDisplay() || 
				node.getChildrenDisplay().size() == 1)
				parent.browse(node, null, true);
		} else if (object instanceof PlateAcquisitionData)
			parent.browse(node, null, true);
	}

	/**
	 * Sets the collection of images already imported.
	 * 
	 * @param nodes The collection to handle.
	 */
	void setImportedImages(Collection nodes)
	{
		if (nodes == null) return;
		state = Browser.READY;
		Iterator i = nodes.iterator();
		ImageData img;
		importedImages = new HashMap<String, Object>();
		while (i.hasNext()) {
			img = (ImageData) i.next();
			importedImages.put(EditorUtil.getObjectName(img.getName()), img);
		}
	}
	
	/**
	 * Sets the file system hosting the repositories.
	 * 
	 * @param systemView The value to set.
	 */
	void setRepositories(FSFileSystemView systemView)
	{
		if (views == null) views = new HashMap<Long, FSFileSystemView>();
		views.put(systemView.getUserID(), systemView);
		state = Browser.READY;
	}
	
	/**
	 * Returns the repositories.
	 * 
	 * @param userID The id of the user.
	 * @return See above.
	 */
	FSFileSystemView getRepositories(long userID) { return views.get(userID); }
	
	/**
	 * Adds the passed images to the collection of imported images.
	 * 
	 * @param nodes The collection to handle.
	 */
	void addImportedImages(List nodes)
	{
		Iterator i = nodes.iterator();
		ImageData img;
		while (i.hasNext()) {
			img = (ImageData) i.next();
			importedImages.put(EditorUtil.getObjectName(img.getName()), img);
		}
	}
	
	/**
	 * Adds the passed image to the collection of imported images.
	 * 
	 * @param image The collection to handle.
	 */
	void addImportedImage(ImageData image)
	{
		if (importedImages != null)
			importedImages.put(EditorUtil.getObjectName(image.getName()), 
					image);
	}
	
	/**
	 * Returns the image corresponding to the passed file name.
	 * 
	 * @param fileName The name of the file.
	 * @return See above.
	 */
	ImageData getImportedImage(String fileName)
	{
		if (importedImages == null) return null;
		String name = EditorUtil.getObjectName(fileName);
		Object ho = importedImages.get(name);
		if (ho instanceof ImageData)
			return (ImageData) ho;
		return null;
	}
	
	/**
	 * Returns <code>true</code> if the file has already been imported,
	 * <code>false</code> otherwise.
	 * 
	 * @param path The path to the file.
	 * @return See above.
	 */
	boolean isFileImported(String path)
	{
		if (importedImages == null) return false;
		String name = EditorUtil.getObjectName(path);
		return importedImages.get(name) != null;
	}

	/** Creates a {@link DeleteCmd} command to execute the action. */
	void delete()
	{
		TreeImageDisplay[] selected = getSelectedDisplays();
    	int count = 0;
    	boolean b = false;
		for (int i = 0; i < selected.length; i++) {
			b = parent.canDelete(selected[i].getUserObject());
			if (b) count++;
		}
		if (count == selected.length) {
			DeleteCmd c = new DeleteCmd(component);
			c.execute();
		}
	}
	
	/**
	 * Returns the object within the specified directory.
	 * 
	 * @param userID The id of the user the directory structure is for.
	 * @param dir The directory to handle.
	 * @return See above.
	 */
	DataObject[] getFilesData(long userID, FileData dir)
	{
		FSFileSystemView fs = getRepositories(userID);
		DataObject[] files = null;
		try {
			files = fs.getFiles(dir, false);
		} catch (FSAccessException e) {

			LogMessage msg = new LogMessage();
			msg.print("Cannot retrieve the files.");
			msg.print(e);
			TreeViewerAgent.getRegistry().getLogger().error(this, msg);
		}
		return files;
	}

	/** 
	 * Returns <code>true</code> if several nodes are selected,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isMultiSelection()
	{
		TreeImageDisplay[] nodes = getSelectedDisplays();
		if (nodes == null || nodes.length <= 1) return false;
		return true;
	}

	/**
	 * Transfers the nodes.
	 * 
	 * @param target The target.
	 * @param nodes The nodes to transfer.
	 * @param transferAction The transfer action.
	 */
	void transfer(TreeImageDisplay target, List<TreeImageDisplay> nodes,
			int transferAction)
	{
		parent.transfer(target, nodes, transferAction);
	}
	
	/**
	 * Returns the security context.
	 * 
	 * @param node The node to handle.
	 * @return See above
	 */
	SecurityContext getSecurityContext(TreeImageDisplay node)
	{
		if (node == null || isSingleGroup()) {
		    return new SecurityContext(
					TreeViewerAgent.getUserDetails().getDefaultGroup().getId());
		}
		GroupData group = parent.getSingleGroupDisplayed();
		if (node == null && group != null)
		    return new SecurityContext(group.getId());
		if (node.getUserObject() instanceof ExperimenterData) {
			TreeImageDisplay parent = node.getParentDisplay();
			Object p = parent.getUserObject();
			if (p instanceof GroupData) {
				group = (GroupData) p;
				return new SecurityContext(group.getId());
			} else {
				return new SecurityContext(
				getUserDetails().getDefaultGroup().getId());
			}
		}
		if (node.getUserObject() instanceof GroupData) {
			group = (GroupData) node.getUserObject();
			return new SecurityContext(group.getId());
		}
		TreeImageDisplay n = null;
		switch (getDisplayMode()) {
			case TreeViewer.GROUP_DISPLAY:
				n = EditorUtil.getDataGroup(node);
				if (n != null)
					return new SecurityContext(n.getUserObjectId());
				break;
			case TreeViewer.EXPERIMENTER_DISPLAY:
			default:
				n = BrowserFactory.getDataOwner(node);
		}
		if (n == null || isSingleGroup()) {
			return new SecurityContext(
					getUserDetails().getDefaultGroup().getId());
		}
		TreeImageDisplay parent = n.getParentDisplay();
		if (parent == null) {
			return new SecurityContext(
					getUserDetails().getDefaultGroup().getId());
		}
		Object p = parent.getUserObject();
		if (p instanceof GroupData) {
			group = (GroupData) p;
			return new SecurityContext(group.getId());
		}
		return new SecurityContext(
				getUserDetails().getDefaultGroup().getId());
	}
	
	
	/**
	 * Returns the selected group.
	 * 
	 * @return See above.
	 */
	GroupData getSelectedGroup() { return parent.getSelectedGroup(); }
	
	/**
	 * Returns <code>true</code> if the user belongs to one group only,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above
	 */
	boolean isSingleGroup() { return getGroups().size() <= 1; }
	
	/**
	 * Returns the display mode. One of the constants defined by 
	 * {@link TreeViewer}.
	 * 
	 * @return See above.
	 */
	int getDisplayMode() { return parent.getDisplayMode(); }
	
	/**
	 * Returns the groups the user currently logged in is a member of.
	 * 
	 * @return See above.
	 */
	Collection getGroups()
	{
		return TreeViewerAgent.getAvailableUserGroups();
	}

}