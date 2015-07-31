/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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

//Java imports
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

//Third-party libraries


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.treeviewer.ExperimenterLoadedDataEvent;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDef;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.EditVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ExperimenterVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ParentVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.RefreshVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.ContainerFinder;
import org.openmicroscopy.shoola.agents.util.browser.NodeSelectionVisitor;
import org.openmicroscopy.shoola.agents.util.browser.NodesFinder;
import org.openmicroscopy.shoola.agents.util.browser.PartialNameVisitor;
import org.openmicroscopy.shoola.agents.util.browser.SimilarNodesVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeFileSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.agents.util.browser.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.util.dnd.DnDTree;
import org.openmicroscopy.shoola.env.data.FSAccessException;
import org.openmicroscopy.shoola.env.data.FSFileSystemView;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import com.google.common.collect.Sets;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.MultiImageData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Implements the {@link Browser} interface to provide the functionality
 * required of the tree viewer component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BrowserComponent
    extends AbstractComponent
    implements Browser
{

	/** The Model sub-component. */
    private BrowserModel    	model;
    
    /** The View sub-component. */
    private BrowserUI       	view;
    
    /** The Controller sub-component. */
    private BrowserControl  	controller;
    
    /** The node to select after saving data. */
    private TreeImageDisplay	toSelectAfterSave;
  
    /**
	 * Reloads the data for the specified experimenters.
	 * 
	 * @param ids The id of the experimenters.
	 */
	private void refreshExperimenterData(List<Long> ids)
	{
		RefreshExperimenterDef def;
	    RefreshVisitor v = new RefreshVisitor(this);
	    TreeImageDisplay root = view.getTreeRoot();
	    //root.setToRefresh(false);
	    int n = root.getChildCount();
	    Map<SecurityContext, RefreshExperimenterDef> 
	    	m = new HashMap<SecurityContext, RefreshExperimenterDef>(n);
	    Collection foundNodes;
	    Map topNodes;
	    int type = model.getBrowserType();
	    Iterator j;
	    TreeImageSet expNode, groupNode;
	    List children;
	    long gid;
	    SecurityContext ctx;
	    if (model.isSingleGroup()) {
	    	gid = model.getUserDetails().getDefaultGroup().getId();
	    	for (int i = 0; i < n; i++) {
	    		expNode = (TreeImageSet) root.getChildAt(i);
		    	//if (expNode.isExpanded() && expNode.isChildrenLoaded()) {
	    		if (ids.contains(expNode.getUserObjectId())) {
		    		expNode.accept(v, 
							TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
			    	foundNodes = v.getFoundNodes();
			    	topNodes = v.getExpandedTopNodes();
			    	//reset the flag 
			    	if (type == Browser.IMAGES_EXPLORER)
			    		countExperimenterImages(expNode);
			    	def = new RefreshExperimenterDef(expNode, 
			    			v.getFoundNodes(),
							v.getExpandedTopNodes());
			    	ctx = new SecurityContext(gid);
			    	if (model.getDisplayMode() ==
			    			TreeViewer.EXPERIMENTER_DISPLAY)
			    		ctx.setExperimenter(
				    			(ExperimenterData) expNode.getUserObject());
					m.put(ctx, def);
		    	}
			}
	    } else {
	    	for (int i = 0; i < n; i++) {
		    	groupNode = (TreeImageSet) root.getChildAt(i);
		    	if (groupNode.isExpanded()) {
		    		gid = groupNode.getUserObjectId();
			    	children = groupNode.getChildrenDisplay();
			    	j = children.iterator();
			    	while (j.hasNext()) {
						expNode = (TreeImageSet) j.next();
						if (ids.contains(expNode.getUserObjectId())) {
							expNode.accept(v, 
									TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
					    	foundNodes = v.getFoundNodes();
					    	topNodes = v.getExpandedTopNodes();
					    	//reset the flag 
					    	if (type == Browser.IMAGES_EXPLORER)
					    		countExperimenterImages(expNode);
					    	def = new RefreshExperimenterDef(expNode, 
					    			v.getFoundNodes(),
									v.getExpandedTopNodes());
					    	ctx = new SecurityContext(gid);
					    	ctx.setExperimenter(
					    			(ExperimenterData) expNode.getUserObject());
							m.put(ctx, def);
						}
					}
		    	}
			}
	    }
	    
	    if(!MapUtils.isEmpty(m)) {
                model.loadRefreshExperimenterData(m, null, -1, null, null);
                fireStateChange();
	    }
	}
	
    /**
     * Helper method to remove the collection of the specified nodes.
     * 
     * @param nodes The collection of node to remove.
     */
    private void removeNodes(List nodes)
    {
        TreeImageDisplay parentDisplay;
        if (getLastSelectedDisplay() == null) 
            parentDisplay = view.getTreeRoot();
        else {
            parentDisplay = getLastSelectedDisplay().getParentDisplay();
        }   
        if (parentDisplay == null) parentDisplay = view.getTreeRoot();
        setSelectedDisplay(parentDisplay);
        view.removeNodes(nodes, parentDisplay);
    }
    
    /**
     * Helper method to create the specified nodes.
     * 
     * @param nodes         The list of nodes to add the specified 
     *                      <code>display</code> node to.
     * @param display       The node to add to.
     * @param parentDisplay The parent of the node.
     */
    private void createNodes(List nodes, TreeImageDisplay display, 
                            TreeImageDisplay parentDisplay)
    {
        setSelectedDisplay(display);
        Object ho = display.getUserObject();
        if ((ho instanceof ProjectData) || (ho instanceof ScreenData))
        	display.setChildrenLoaded(Boolean.valueOf(true));
        else if (ho instanceof TagAnnotationData) { 
        	TagAnnotationData tag = (TagAnnotationData) ho;
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
        		display.setChildrenLoaded(Boolean.valueOf(true));
        	else display.setChildrenLoaded(Boolean.valueOf(false));
        } else {
        	display.setChildrenLoaded(Boolean.valueOf(false));
        }
        view.createNodes(nodes, display, parentDisplay);
        //Object o = display.getUserObject();
        countItems(null, display);
    }
    
    /**
     * Handles the node selection when the user clicks on find next or find 
     * previous.
     * 
     * @param node The newly selected node.
     */ 
    private void handleNodeDisplay(TreeImageDisplay node)
    {
        view.selectFoundNode(node);
    }
    
    /**
     * Controls if the passed node has to be saved before selecting a new node.
     * 
     * @param node The node to check.
     * @return <code>true</code> if we need to save data, <code>false</code>
     * 			otherwise.
     */
    private boolean hasDataToSave(TreeImageDisplay node)
    {
        if (model.getParentModel().hasDataToSave()) {
        	//toSelectAfterSave = node;
        	model.getParentModel().showPreSavingDialog();
        	return false;
        }
        return false;
    }

    /** Counts the tags used but not owned by the user. */
    private void countExperimenterDataInFolders()
    {
    	ContainerFinder finder = new ContainerFinder(ExperimenterData.class);
		accept(finder, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		Set<TreeImageSet> nodes = finder.getContainerNodes();
		Iterator<TreeImageSet> i = nodes.iterator();
		TreeImageSet node;
		while (i.hasNext()) {
			node = i.next();
			if (node.getUserObject() instanceof ExperimenterData
			        && node.isExpanded())
				countExperimenterImages(node);
		}
    }
    
	/** 
	 * Retrieves the nodes to count the value for.
	 *
	 * @param rootType The type of node to track.
	 * @param node The node to visit.
	 */
	private void countItems(List<Class> rootType, TreeImageDisplay node)
	{
		if (rootType == null) {
			int type = model.getBrowserType();
			if (type == PROJECTS_EXPLORER) {
				rootType = new ArrayList<Class>();
				rootType.add(DatasetData.class);
			} else if (type == TAGS_EXPLORER) {
				rootType = new ArrayList<Class>();
				rootType.add(TagAnnotationData.class);
			} else if (type == ADMIN_EXPLORER) {
				rootType = new ArrayList<Class>();
				rootType.add(GroupData.class);
			}
		} 
		if (rootType == null) {
			countExperimenterDataInFolders();
			return;
		}
		ContainerFinder finder = new ContainerFinder(rootType);
		Set<DataObject> items;
		Set<TreeImageSet> nodes;
		if (node != null) {
			node.accept(finder, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
			items = finder.getContainers();
			nodes = finder.getContainerNodes();
			if (items.size() == 0 && nodes.size() == 0) return;
			model.fireContainerCountLoading(items, nodes, node);
		} else {
			//
			TreeImageDisplay root = view.getTreeRoot();
			List l = root.getChildrenDisplay();
			Iterator i = l.iterator();
			while (i.hasNext()) {
				countItems(rootType, (TreeImageDisplay) i.next());
			}
		}
	}
	
	/** 
	 * Counts the nodes linked to the specified annotation.
	 *
	 * @param node The node of reference.
	 */
	private void countItemsInAnnotation(TreeImageSet node)
	{
		if (node == null) return;
		Object ho = node.getUserObject();
		List<Class> types = new ArrayList<Class>();
		if (ho instanceof TagAnnotationData) {
			types.add(DatasetData.class);
		}
		if (types.size() == 0) return;
		ContainerFinder finder = new ContainerFinder(types);
		node.accept(finder, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		Set<DataObject> items = finder.getContainers();
		Set<TreeImageSet> nodes = finder.getContainerNodes();
		if (items.size() == 0 && nodes.size() == 0) return;
		model.fireContainerCountLoading(items, nodes, node);
	}

	/**
	 * Sets the selected node.
	 * 
	 * @param display The selected value.
	 * @param single  Pass <code>true</code> if the method is invoked for
	 *                single selection, <code>false</code> for multi-selection.
	 */
	private void setSelectedDisplay(TreeImageDisplay display, boolean single)
    {
		switch (model.getState()) {
	    	//case LOADING_DATA:
	    	//case LOADING_LEAVES:
	    	case DISCARDED:
	    		throw new IllegalStateException(
	    				"This method cannot be invoked in the "+
	    		"DISCARDED state.");
    	}
    	hasDataToSave(display);
    	TreeImageDisplay oldDisplay = model.getLastSelectedDisplay();
    	TreeImageDisplay exp = null;
    	Object ho = null;
    	if (display != null) {
    		ho = display.getUserObject();
    		if (ho instanceof ExperimenterData) {
    			if (getBrowserType() != ADMIN_EXPLORER) {
    				exp = display;
        			display = null;
    			}
    		}
    	}
    	if (exp != null) model.setSelectedDisplay(exp, single);
    	else model.setSelectedDisplay(display, single);
    	if (oldDisplay != null && oldDisplay.equals(display)) {
    		ho = oldDisplay.getUserObject();
    		if (ho instanceof PlateData || ho instanceof PlateAcquisitionData)
    			firePropertyChange(SELECTED_TREE_NODE_DISPLAY_PROPERTY, null, 
            			display);
    	} else {
    		firePropertyChange(SELECTED_TREE_NODE_DISPLAY_PROPERTY, oldDisplay, 
    				display);
    	}
    }
	
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straight 
     * after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    BrowserComponent(BrowserModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new BrowserControl(this);
        view = new BrowserUI();
    }
    
    /** 
     * Links up the MVC triad. 
     * 
     * @param exp The logged in experimenter.
     */
    void initialize(ExperimenterData exp)
    {
        model.initialize(this);
        controller.initialize(view);
        view.initialize(controller, model, exp);
    }
    
    /**
     * Returns the Model sub-component.
     * 
     * @return See above.
     */
    BrowserModel getModel() { return model; }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#activate()
     */
    public void activate()
    {
        int state = model.getState();
        switch (state) {
            case NEW:
            	if (model.getBrowserType() == ADMIN_EXPLORER) {
            		model.fireExperimenterDataLoading(null);
            		return;
            	}
            	TreeImageDisplay node;
            	switch (model.getDisplayMode()) {
					case TreeViewer.GROUP_DISPLAY:
						node = getDefaultGroupNode();
		            	if (node != null) {
		            		view.expandNode(node, true);
		            	}
						break;
					case TreeViewer.EXPERIMENTER_DISPLAY:
					default:
						node = getLoggedExperimenterNode();
		            	if (node != null) {
		            		if (!model.isSingleGroup()) {
		                		TreeImageDisplay p = node.getParentDisplay();
		                		if (p != null) p.setExpanded(true);
		                	}
		            		view.expandNode(node, true);
		            	}
				}
            	
                break;
            case READY:
            	refreshBrowser(); //do we want to automatically refresh?
            	break;
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
            default:
                break;
        }
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#discard()
     */
    public void discard()
    {
        if (model.getState() != DISCARDED) {
            model.discard();
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUI()
     */
    public JComponent getUI()
    { 
        if (model.getState() == DISCARDED)
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED state.");
        return view;
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getBrowserType()
     */
    public int getBrowserType() { return model.getBrowserType(); }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#cancel()
     */
    public void cancel()
    { 
        int state = model.getState();
        if ((state == LOADING_DATA) || (state == LOADING_LEAVES)) {
            model.cancel();
            view.cancel(model.getLastSelectedDisplay()); 
            fireStateChange();
        }
    }
   
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#loadFilteredImagesForHierarchy()
     */
    public void loadFilteredImagesForHierarchy()
    {
        int state = model.getState();
        if ((state == DISCARDED) || (state == LOADING_LEAVES))
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED or" +
                    "LOADING_LEAVES state.");
        //Check the filterType and editorType.
        if (model.getBrowserType() != Browser.IMAGES_EXPLORER)
            throw new IllegalArgumentException("Method should only be invoked" +
                    " by the Images Explorer.");
        //if (model.getFilterType() == NO_IMAGES_FILTER) 
        view.loadAction(view.getTreeRoot());
        //model.fireFilterDataLoading();
        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, false);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setLeaves(Set, TreeImageSet, TreeImageSet)
     */
    public void setLeaves(Collection leaves, TreeImageSet parent, 
    						TreeImageSet expNode)
    {
        if (model.getState() != LOADING_LEAVES) return;
        if (leaves == null) throw new NullPointerException("No leaves.");
        Object ho = expNode.getUserObject();
        if (!(ho instanceof ExperimenterData || ho instanceof GroupData))
        	throw new IllegalArgumentException("Node not valid");
        if (model.getBrowserType() == FILE_SYSTEM_EXPLORER) {
        	model.getParentModel().setLeaves(parent, leaves);
        	model.setState(READY);
        	fireStateChange();
            return;
        }
       
        Set visLeaves = TreeViewerTranslator.transformHierarchy(leaves);
        view.setLeavesViews(visLeaves, parent);
        
        model.setState(READY);
        if (parent != null && 
        		parent.getUserObject() instanceof TagAnnotationData)
        	//countItems(TagAnnotationData.class);
        	countItemsInAnnotation(parent);
        if (model.getBrowserType() == TAGS_EXPLORER && 
        		parent instanceof TreeFileSet) {
        	List<Class> types = new ArrayList<Class>();
        	types.add(TagAnnotationData.class);
        	countItems(types, expNode);
        }
        Object p = null;
        if (parent != null && 
        		parent.getUserObject() instanceof PlateData) {
        	p = parent.getUserObject();
        }
        if (!(p instanceof PlateData))
        	model.getParentModel().setLeaves(parent, leaves);
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelectedDisplay(TreeImageDisplay)
     */
    public void setSelectedDisplay(TreeImageDisplay display)
    {
        setSelectedDisplay(display, true);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#showPopupMenu(int)
     */
    public void showPopupMenu(int index)
    {
        switch (model.getState()) {
            case LOADING_DATA:
            case LOADING_LEAVES:
            case DISCARDED:
            	return;
            	/*
                throw new IllegalStateException(
                        "This method cannot be invoked in the LOADING_DATA, "+
                        " LOADING_LEAVES or DISCARDED state.");
                        */
        }
        switch (index) {
        	case TreeViewer.FULL_POP_UP_MENU:
        	case TreeViewer.PARTIAL_POP_UP_MENU:
        	case TreeViewer.ADMIN_MENU:
        	case TreeViewer.CREATE_MENU_ADMIN:
        		break;
        	default:
        		throw new IllegalArgumentException("Menu not supported:" +
        											" "+index);
		}
        firePropertyChange(POPUP_MENU_PROPERTY, Integer.valueOf(-1), 
        		Integer.valueOf(index));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getClickPoint()
     */
    public Point getClickPoint() { return model.getClickPoint(); }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getLastSelectedDisplay()
     */
    public TreeImageDisplay getLastSelectedDisplay()
    {
        return model.getLastSelectedDisplay();
    }
	
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#close()
     */
    public void close()
    {
        switch (model.getState()) {
            case LOADING_DATA:
            case LOADING_LEAVES:
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can only be invoked in the LOADING_DATA, "+
                        " LOADING_LEAVES or DISCARDED state.");
        }
        firePropertyChange(CLOSE_PROPERTY, null, this);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#collapse(TreeImageDisplay)
     */
    public void collapse(TreeImageDisplay node)
    {
        if (node == null) return;
        view.collapsePath(node);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#expand(TreeImageDisplay)
     */
    public void expand(TreeImageDisplay node)
    {
        if (node == null) return;
        view.expandNode(node);
    }

    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#accept(TreeImageDisplayVisitor)
     */
    public void accept(TreeImageDisplayVisitor visitor)
    {
        accept(visitor, TreeImageDisplayVisitor.ALL_NODES);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#accept(TreeImageDisplayVisitor, int)
     */
    public void accept(TreeImageDisplayVisitor visitor, int algoType)
    {
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        view.getTreeRoot().accept(visitor, algoType);
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getTitle()
     */
    public String getTitle() { return view.getBrowserTitle(); }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getIcon()
     */
    public Icon getIcon()
    {
        IconManager im = IconManager.getInstance();
        switch (model.getBrowserType()) {
            case PROJECTS_EXPLORER:
                return im.getIcon(IconManager.PROJECT);//HIERARCHY_EXPLORER);
            case TAGS_EXPLORER:
                return im.getIcon(IconManager.TAG);//TAGS_EXPLORER);
            case IMAGES_EXPLORER:
                return im.getIcon(IconManager.DATE);//IMAGES_EXPLORER);
            case SCREENS_EXPLORER:
            	return im.getIcon(IconManager.SCREEN);//SCREENS_EXPLORER);
            case FILES_EXPLORER:
                return im.getIcon(IconManager.FILES_EXPLORER);
            case FILE_SYSTEM_EXPLORER:
                return im.getIcon(IconManager.FILE_SYSTEM_EXPLORER);
            case ADMIN_EXPLORER:
                return im.getIcon(IconManager.ADMIN);
        }
        return null;
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#sortTreeNodes(int)
     */
    public void sortTreeNodes(int sortType)
    {
        switch (model.getState()) {
        	//case COUNTING_ITEMS:
            case LOADING_DATA:
            case LOADING_LEAVES:
            case DISCARDED:
                throw new IllegalStateException(
                        "This method cannot be invoked in the LOADING_DATA, "+
                        " LOADING_LEAVES or DISCARDED state.");
        }
        switch (sortType) {
            case SORT_NODES_BY_DATE:
            case SORT_NODES_BY_NAME:
                break;
            default:
                throw new IllegalArgumentException("SortType not supported.");
        }
        view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        view.sortNodes(sortType);
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setContainerCountValue(int, long, Set)
     */
    public void setContainerCountValue(long containerID, long value,
    		Set<TreeImageSet> nodes)
    {
        int state = model.getState();
        if (state == DISCARDED) return;
        boolean b = model.setContainerCountValue(view.getTreeDisplay(),
									containerID, value, nodes);
        if (b) {
        	view.getTreeDisplay().repaint();
        }
        model.getParentModel().setStatus(false, "", true);
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setFoundInBrowser(Set)
     */
    public void setFoundInBrowser(Set nodes)
    {
        if (nodes == null || nodes.size() == 0) {
            model.setFoundNodes(null); // reset default value.
            model.setFoundNodeIndex(-1); // reset default value.
            view.getTreeDisplay().repaint();
            return;
        }
        List<Object> list = new ArrayList<Object>(nodes.size());
        Iterator i = nodes.iterator();
        
        final JTree tree = view.getTreeDisplay();
        while (i.hasNext()) 
            list.add(i.next());
        Comparator c = new Comparator() {
            public int compare(Object o1, Object o2)
            {
                TreeImageDisplay node1 = (TreeImageDisplay) o1;
                TreeImageDisplay node2 = (TreeImageDisplay) o2;
                int i1 = tree.getRowForPath(new TreePath(node1.getPath()));
                int i2 = tree.getRowForPath(new TreePath(node2.getPath()));
                return (i1-i2);
            }
        };
        Collections.sort(list, c);
        model.setFoundNodes(list);
        model.setFoundNodeIndex(0);
        handleNodeDisplay((TreeImageDisplay) list.get(0));
        tree.repaint();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#findNext()
     */
    public void findNext()
    {
        List l = model.getFoundNodes();
        if (l == null || l.size() == 0) return;
        int index = model.getFoundNodeIndex();
        int n = l.size()-1;
        if (index < n) index++; //not last element
        else if (index == n) index = 0;
        model.setFoundNodeIndex(index);
        handleNodeDisplay((TreeImageDisplay) l.get(index));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#findPrevious()
     */
    public void findPrevious()
    {
    	List l = model.getFoundNodes();
        if (l == null || l.size() == 0) return;
        int index = model.getFoundNodeIndex();
        if (index > 0)  index--; //not last element
        else if (index == 0)  index = l.size()-1;
        model.setFoundNodeIndex(index);
        handleNodeDisplay((TreeImageDisplay) l.get(index));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelected(boolean)
     */
    public void setSelected(boolean b)
    {
        switch (model.getState()) {
	        //case LOADING_DATA:
	        //case LOADING_LEAVES:
	        //case COUNTING_ITEMS:
            //    return;
	        case DISCARDED:
	            throw new IllegalStateException(
	                    "This method can only be invoked in the " +
	                    "NEW or READY state.");
        }
        boolean old = model.isSelected();
        if (old == b) return;
        model.setSelected(b);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refreshEdition(DataObject, int)
     */
    public void refreshEdition(DataObject object, DataObject parent, int op)
    {
        switch (model.getState()) {
            case NEW:
            case READY:   
                break;
            default:
            	throw new IllegalStateException("This method can only " +
            			"be invoked in the NEW or READY state.");
        }
        Object o = object;
        List nodes = null;
        TreeImageDisplay parentDisplay = null;
        TreeImageDisplay loggedUser = getLoggedExperimenterNode();
        if (op == TreeViewer.CREATE_OBJECT) {
            TreeImageDisplay node = getLastSelectedDisplay();
            if ((object instanceof ProjectData) ||
                (object instanceof ScreenData) ||
                ((object instanceof DatasetData) && parent == null) ||
                ((object instanceof TagAnnotationData) && parent == null)) {
                nodes = new ArrayList(1);
                nodes.add(loggedUser);
                parentDisplay = loggedUser;
                //nodes.add(view.getTreeRoot());
                //parentDisplay = view.getTreeRoot();
            } else if (parent != null)
                o = node.getUserObject();
        }
        if (nodes == null && parent == null) {
            EditVisitor visitor = new EditVisitor(this, o, null);
            //accept(visitor, TreeImageDisplayVisitor.ALL_NODES);
            loggedUser.accept(visitor, TreeImageDisplayVisitor.ALL_NODES);
            nodes = visitor.getFoundNodes();
        }
        if (parent != null) {
        	EditVisitor visitor = new EditVisitor(this, null, parent);
            loggedUser.accept(visitor, TreeImageDisplayVisitor.ALL_NODES);
            nodes = visitor.getParentNodes();
        }
        
        if (op == TreeViewer.UPDATE_OBJECT) view.updateNodes(nodes, object);
        else if (op == TreeViewer.REMOVE_OBJECT) removeNodes(nodes);
        else if (op == TreeViewer.CREATE_OBJECT) {
            //Get the user node.
            if (parentDisplay == null)
            	parentDisplay = getLastSelectedDisplay();
            TreeImageDisplay newNode = 
            		TreeViewerTranslator.transformDataObject(object);
           
            createNodes(nodes, newNode, parentDisplay);
        }     
        setSelectedNode();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#onOrphanDataObjectCreated(DataObject)
     */
	public void onOrphanDataObjectCreated(DataObject data) 
	{
		int type = model.getBrowserType();
		if (data instanceof DatasetData) {
			if (type != PROJECTS_EXPLORER) return;
		} else if (data instanceof TagAnnotationData) {
			if (type != TAGS_EXPLORER) return;
		}
		TreeImageDisplay loggedUser = getLoggedExperimenterNode();
		List<TreeImageDisplay> nodes = new ArrayList<TreeImageDisplay>(1);
        nodes.add(loggedUser);
        //long model.get
        createNodes(nodes, 
                TreeViewerTranslator.transformDataObject(data), loggedUser);
        setSelectedNode();
	}
	
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getSelectedDisplays()
     */
    public TreeImageDisplay[] getSelectedDisplays()
    {
        return model.getSelectedDisplays();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getSelectedDataObjects()
     */
    public List getSelectedDataObjects()
    {
    	TreeImageDisplay[] nodes = getSelectedDisplays();
    	if (nodes == null || nodes.length == 0) return null;
    	List<DataObject> objects = new ArrayList<DataObject>();
    	Object uo;
    	for (int i = 0; i < nodes.length; i++) {
			uo = nodes[i].getUserObject();
			if (uo instanceof DataObject) {
				objects.add((DataObject) uo);
			}
		}
    	return objects;
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelectedDisplays(TreeImageDisplay[], boolean)
     */
    public void setSelectedDisplays(TreeImageDisplay[] nodes, 
    		boolean expandParent)
    {
        if (nodes.length == 0) return;
        TreeImageDisplay[] oldNodes = model.getSelectedDisplays();
        boolean b = true;
        if (oldNodes != null && oldNodes.length > 1) b = false;
        if (nodes.length == 1 && b) {
        	setSelectedDisplay(nodes[0], true);
        	if (expandParent) {
        		TreeImageDisplay parent = nodes[0].getParentDisplay();
        		if (parent.getUserObject() instanceof ExperimenterData)
        			parent = nodes[0];
        		view.expandNode(parent);
        		view.setFoundNode(nodes);
        	}
        	return;
        }
        boolean flush = false;
        if (oldNodes.length >= nodes.length) flush = true;
        int n = nodes.length;
        TreeImageDisplay parent = null;
        for (int i = 0; i < n; i++) {
        	if (nodes[i] != null) {
        		parent = nodes[i].getParentDisplay();
        		if (parent != null && 
        			parent.getUserObject() instanceof ExperimenterData)
        			parent = nodes[i];
            	if (i == 0) model.setSelectedDisplay(nodes[i], flush);
            	else model.setSelectedDisplay(nodes[i], false);
        	}
		}
        if (parent != null && expandParent) {
        	view.setFoundNode(nodes);
        	view.expandNode(parent);
        }
        firePropertyChange(SELECTED_TREE_NODE_DISPLAY_PROPERTY, null, 
        		nodes[n-1]);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#onComponentStateChange(boolean)
     */
    public void onComponentStateChange(boolean b)
    {
        view.onComponentStateChange(b);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#isDisplayed()
     */
    public boolean isDisplayed() { return model.isDisplayed(); }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setDisplayed(boolean)
     */
    public void setDisplayed(boolean displayed)
    {
        if (model.getState() == DISCARDED)
            throw new IllegalStateException("This method cannot be invoked "+
                    "in the DISCARDED state.");
        model.setDisplayed(displayed);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setRefreshedHierarchy(Map, Map)
     */
    public void setRefreshedHierarchy(Map nodes, Map expandedTopNodes)
    {
        if (model.getState() != LOADING_DATA)
            throw new IllegalStateException("This method cannot be invoked "+
                "in the LOADING_DATA state.");
        //long userID = model.getUserID();
        //long groupID = model.getUserGroupID();
        //view.setViews(TreeViewerTranslator.refreshHierarchy(nodes,
        //            expandedTopNodes, userID, groupID)); 
        countItems(null, null);
        model.getParentModel().setStatus(false, "", true);
        PartialNameVisitor v = new PartialNameVisitor(view.isPartialName());
		accept(v, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
        fireStateChange(); 
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#displaysImagesName()
     */
	public void displaysImagesName()
	{
		 if (model.getState() == DISCARDED)
			 throw new IllegalStateException("This method cannot be invoked "+
	                "in the DISCARDED state.");
		 PartialNameVisitor v = new PartialNameVisitor(view.isPartialName());
		 accept(v, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
		 view.repaint();
	}

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#loadExperimenterData(TreeImageDisplay, TreeImageDisplay)
     */
	public void loadExperimenterData(TreeImageDisplay exp, TreeImageDisplay n)
	{
		if (exp == null)
			throw new IllegalArgumentException("Node not valid.");
		Object uo = exp.getUserObject();
		if (!(uo instanceof ExperimenterData || uo instanceof GroupData))
			throw new IllegalArgumentException("Node not valid.");
		switch (model.getState()) {
			case DISCARDED:
			case LOADING_LEAVES:
				return;
		}
        if (n == null) model.fireExperimenterDataLoading((TreeImageSet) exp);
        else {
        	n.setToRefresh(false);
        	if (model.getBrowserType() == FILE_SYSTEM_EXPLORER) {
        		uo = n.getUserObject();
        		TreeImageDisplay expNode = BrowserFactory.getDataOwner(n);
				if (expNode == null) return;  
				Object ho = expNode.getUserObject();
				if (!(ho instanceof ExperimenterData)) return;
        		if (uo instanceof MultiImageData) {
        			MultiImageData mi = (MultiImageData) uo;
        			model.setState(LOADING_LEAVES);
					setLeaves(mi.getComponents(), (TreeImageSet) n, 
							(TreeImageSet) exp);
        		} else if (uo instanceof FileData) {
        			FileData dir = (FileData) uo;
        			if (dir.isHidden()) return;
        			if (dir.isDirectory()) {
        				//Check if data loaded
        				List<DataObject> list = new ArrayList<DataObject>();
        				if (n.isChildrenLoaded()) {
        					List children = n.getChildrenDisplay();
        					Iterator k = children.iterator();
        					TreeImageDisplay d;
        					Object o;
        					while (k.hasNext()) {
								d = (TreeImageDisplay) k.next();
								o = d.getUserObject();
								if (o instanceof DataObject) {
									list.add((DataObject) o);
								}
							}
        				} else {
        					long expID = ((ExperimenterData) ho).getId();
            				DataObject[] files = model.getFilesData(expID, dir);
            				if (files != null) {
            					for (int i = 0; i < files.length; i++) 
    								list.add(files[i]);
            				}
        				}
        				if (list.size() > 0) {
        					model.setState(LOADING_LEAVES);
        					setLeaves(list, (TreeImageSet) n, 
        							(TreeImageSet) exp);
        				}
        			}
        			return;
        		}
        	} else model.fireLeavesLoading(exp, n);
        }
        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, false);
        fireStateChange();
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setExperimenterData(TreeImageDisplay, Collection)
     */
	public void setExperimenterData(TreeImageDisplay expNode, Collection nodes)
	{
		int state = model.getState();
        if (state != LOADING_DATA) return;
        if (nodes == null) throw new NullPointerException("No nodes.");
      
        if (expNode == null)
        	throw new IllegalArgumentException("Node not valid.");
        Object uo = expNode.getUserObject();
        if (!(uo instanceof ExperimenterData || uo instanceof GroupData))
        	throw new IllegalArgumentException("Node not valid.");
        Set convertedNodes = TreeViewerTranslator.transformHierarchy(nodes);
        view.setExperimenterData(convertedNodes, expNode);
        model.setState(READY);
        
        countItems(null, expNode);
        countExperimenterDataInFolders();
        model.getParentModel().setStatus(false, "", true);
        //Visit the tree and
        switch(model.getBrowserType()) {
        	case Browser.PROJECTS_EXPLORER:
        	case Browser.SCREENS_EXPLORER:
        		//TODO: review that code to indicate the context.
        		ParentVisitor visitor = new ParentVisitor();
        		accept(visitor, ParentVisitor.TREEIMAGE_SET_ONLY);
        		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
        		bus.post(new ExperimenterLoadedDataEvent(visitor.getData()));
        }
        fireStateChange();
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#addExperimenter(ExperimenterData, long)
     */
	public void addExperimenter(ExperimenterData experimenter, long groupID)
	{
		if (experimenter == null)
			throw new IllegalArgumentException("Experimenter cannot be null.");
		if (model.getBrowserType() == ADMIN_EXPLORER) return;
		TreeImageDisplay node = model.getLastSelectedDisplay();
		boolean reload = false;
		if (model.isSingleGroup()) {
			node = view.getTreeRoot();
			reload = node.getChildCount() == 0;
		} else {
			//Find the group
			ExperimenterVisitor v = new ExperimenterVisitor(this, groupID);
			accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
			List<TreeImageDisplay> list = v.getNodes();
			if (list.size() == 0) return;
			node = list.get(0);
		}
		
		//Make sure the user is not already display
		List<TreeImageDisplay> nodes = new ArrayList<TreeImageDisplay>(1);
		nodes.add(new TreeImageSet(experimenter));
		SimilarNodesVisitor visitor = new SimilarNodesVisitor(nodes);
		node.accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		
		if (visitor.getFoundNodes().size() > 0) return;
		setSelectedDisplay(null);
		view.addExperimenter(experimenter, node);
		if (reload) view.reloadNode(node);
	}

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#removeExperimenter(ExperimenterData, GroupData)
     */
	public void removeExperimenter(ExperimenterData exp, long groupID)
	{
		if (exp == null)
			throw new IllegalArgumentException("Experimenter cannot be null.");
		TreeImageDisplay node = null;
		if (groupID >= 0) {
			ExperimenterVisitor v = new ExperimenterVisitor(this, groupID);
			accept(v);
			List<TreeImageDisplay> nodes = v.getNodes();
			if (nodes.size() == 1) node = nodes.get(0);
		}
		
		view.removeExperimenter(exp, node);
	}

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refreshExperimenterData()
     */
	public void refreshExperimenterData()
	{
		switch (model.getState()) {
	        case LOADING_DATA:
	        case LOADING_LEAVES:
	        	model.cancel();
	        	break;
	        case DISCARDED:
	        	//ignore
        	return;
		}
		TreeImageDisplay display;
		RefreshVisitor v = new RefreshVisitor(this);
		if (model.getBrowserType() == ADMIN_EXPLORER) {
			display = view.getTreeRoot();
			//review for admin.
			display.accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
			RefreshExperimenterDef def = new RefreshExperimenterDef(
				(TreeImageSet) display, v.getFoundNodes(), 
				v.getExpandedTopNodes());
			Map<SecurityContext, RefreshExperimenterDef> 
				m = new HashMap<SecurityContext, RefreshExperimenterDef>(1);
			SecurityContext ctx = TreeViewerAgent.getAdminContext();
			if (ctx == null) ctx = model.getSecurityContext(null);
			m.put(ctx, def);
			model.loadRefreshExperimenterData(m, null, -1, null, null);
			fireStateChange();
		} else {
			display = model.getLastSelectedDisplay();
			if (display == null) return;
			Object ho = display.getUserObject();
			if (!(ho instanceof ExperimenterData)) return;
			refreshExperimenterData(Arrays.asList(display.getUserObjectId()));
		}
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getLoggedExperimenterNode()
     */
	public TreeImageDisplay getLoggedExperimenterNode()
	{
		SecurityContext ctx = model.getSecurityContext(null);
		long id = ctx.getGroupID();
		if (model.isSingleGroup()) id = -1;
		ExperimenterVisitor visitor = new ExperimenterVisitor(this, 
				model.getUserID(), id);
		accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		List<TreeImageDisplay> nodes = visitor.getNodes();
		if (nodes.size() != 1) return null;
		return nodes.get(0);
	}
	
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refreshLoggedExperimenterData()
     */
	public void refreshLoggedExperimenterData()
	{
		switch (model.getState()) {
	        case LOADING_DATA:
	        case LOADING_LEAVES:
	        	model.cancel();
	        	break;
	        case DISCARDED:
	        	//ignore
	    	return;
		}
		TreeImageDisplay node = getLoggedExperimenterNode();
		if (node == null) return;
		Object ho = node.getUserObject();
		if (!(ho instanceof ExperimenterData)) return;
		refreshExperimenterData(Arrays.asList(node.getUserObjectId()));
		fireStateChange();
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refreshTree(Object, DataObject)
     */
	public void refreshTree(Object refNode, DataObject toBrowse)
	{ 
	    switch (model.getState()) {
	    case LOADING_DATA:
	    case LOADING_LEAVES:
	        model.cancel();
	        break;
	    case DISCARDED:
	        //ignore
	        return;
	    }

	    if (model.getBrowserType() == FILE_SYSTEM_EXPLORER) {
	        //view.loadFileSystem(true);
	        return;
	    }
	    if (model.getBrowserType() == ADMIN_EXPLORER) {
	        refreshExperimenterData();
	        return;
	    }
	    addComponent(null);
	    TreeImageDisplay root = view.getTreeRoot();
	    //root.setToRefresh(false);

	    RefreshExperimenterDef def;
	    RefreshVisitor v; 
	    int n = root.getChildCount();
	    Map<SecurityContext, RefreshExperimenterDef> 
	    m = new HashMap<SecurityContext, RefreshExperimenterDef>(n);
	    int type = model.getBrowserType();
	    Iterator j;
	    TreeImageSet expNode, groupNode;
	    List children;
	    long gid;
	    SecurityContext ctx;
	    switch (model.getDisplayMode()) {
	    case TreeViewer.EXPERIMENTER_DISPLAY:
	        if (model.isSingleGroup()) {
	            gid = model.getUserDetails().getDefaultGroup().getId();
	            for (int i = 0; i < n; i++) {
	                expNode = (TreeImageSet) root.getChildAt(i);
	                if (expNode.isExpanded() && expNode.isChildrenLoaded()) {
	                    v = new RefreshVisitor(this);
	                    expNode.accept(v);
	                    //reset the flag 
	                    if (type == Browser.IMAGES_EXPLORER)
	                        countExperimenterImages(expNode);
	                    def = new RefreshExperimenterDef(expNode,
	                            v.getFoundNodes(), v.getExpandedTopNodes());
	                    ctx = new SecurityContext(gid);
	                    if (model.getDisplayMode() ==
	                            TreeViewer.EXPERIMENTER_DISPLAY)
	                        ctx.setExperimenter(
	                                (ExperimenterData) expNode.getUserObject());
	                    m.put(ctx, def);
	                } else {
	                    expNode.setChildrenLoaded(Boolean.FALSE);
	                }
	            }
	        } else {
	            for (int i = 0; i < n; i++) {
	                groupNode = (TreeImageSet) root.getChildAt(i);
	                if (groupNode.isExpanded()) {
	                    v = new RefreshVisitor(this);
	                    gid = groupNode.getUserObjectId();
	                    children = groupNode.getChildrenDisplay();
	                    j = children.iterator();
	                    while (j.hasNext()) {
	                        expNode = (TreeImageSet) j.next();
	                        if (expNode.isChildrenLoaded() &&
	                                expNode.isExpanded()) {
	                            v = new RefreshVisitor(this);
	                            expNode.accept(v);
	                            //reset the flag 
	                            if (type == Browser.IMAGES_EXPLORER)
	                                countExperimenterImages(expNode);
	                            def = new RefreshExperimenterDef(expNode,
	                                    v.getFoundNodes(),
	                                    v.getExpandedTopNodes());
	                            ctx = new SecurityContext(gid);
	                            ctx.setExperimenter(
	                                    (ExperimenterData) expNode.getUserObject());
	                            m.put(ctx, def);
	                        } else {
	                            expNode.setChildrenLoaded(Boolean.FALSE);
	                        }
	                    }
	                }
	            }
	        }
	        break;
	    case TreeViewer.GROUP_DISPLAY:
	        for (int i = 0; i < n; i++) {
	            groupNode = (TreeImageSet) root.getChildAt(i);
	            if (groupNode.isExpanded() && groupNode.isChildrenLoaded()) {
	                v = new RefreshVisitor(this);
	                groupNode.accept(v);
	                gid = groupNode.getUserObjectId();
	                //reset the flag 
	                if (type == Browser.IMAGES_EXPLORER)
	                    countExperimenterImages(groupNode);
	                def = new RefreshExperimenterDef(groupNode,
	                        v.getFoundNodes(),
	                        v.getExpandedTopNodes());
	                m.put(new SecurityContext(gid), def);
	            } else {
	                groupNode.setChildrenLoaded(Boolean.FALSE);
                }
	        }
	    }

	    if (m.size() == 0) { //for new data the first time.
	        TreeImageDisplay node = null;
	        switch (model.getDisplayMode()) {
	        case TreeViewer.GROUP_DISPLAY:
	            node = getDefaultGroupNode();
	        case TreeViewer.EXPERIMENTER_DISPLAY:
	        default:
	            node = getLoggedExperimenterNode();
	        }
	        if (node != null)
	            loadExperimenterData(node, null);
	        return;
	    }
	    model.loadRefreshExperimenterData(m, null, -1, refNode, toBrowse);
	    fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setRefreshExperimenterData(Map, Class, long)
     */
	public void setRefreshExperimenterData(
			Map<SecurityContext, RefreshExperimenterDef> 
		nodes, Class type, long id)
	{
		//TODO: Should reselect the node previously selected.
		if (nodes == null || nodes.size() == 0) {
			model.setSelectedDisplay(null, true);
			model.setState(READY);
			countItems(null, null);
			if (model.getBrowserType() == TAGS_EXPLORER)
				countExperimenterDataInFolders();
			model.getParentModel().setStatus(false, "", true);
			return;
		}
		Iterator<Entry<SecurityContext, RefreshExperimenterDef>>
		i = nodes.entrySet().iterator();
		RefreshExperimenterDef node;
		TreeImageSet expNode;
		Set convertedNodes;
		Entry<SecurityContext, RefreshExperimenterDef> entry;
		int browserType = model.getBrowserType();
		Map<Integer, Set> results;
		while (i.hasNext()) {
			entry = i.next();
			//userId = (Long)
			node = (RefreshExperimenterDef) entry.getValue();
			expNode = node.getExperimenterNode();
			if (browserType == IMAGES_EXPLORER || browserType == FILES_EXPLORER)
			{
				results = TreeViewerTranslator.refreshFolderHierarchy(
							(Map) node.getResults());
				view.refreshFolder(expNode, results);
			} else {
				convertedNodes = TreeViewerTranslator.refreshHierarchy(
						(Map) node.getResults(), node.getExpandedTopNodes());
				view.setExperimenterData(convertedNodes, expNode);
			}
		}
		
		
		
		
		//expand the nodes.
		i = nodes.entrySet().iterator();
		Map m;
		Iterator j;
		Entry e;
		NodesFinder finder;
		if (type == null) {
			List l;
			Iterator<TreeImageDisplay> k;
			Set<TreeImageDisplay> found;
			while (i.hasNext()) {
				entry = i.next();
				node = (RefreshExperimenterDef) entry.getValue();
				expNode = node.getExperimenterNode();
				if (expNode.isExpanded()) {
					m = node.getExpandedTopNodes();
					List expandedNodes = node.getExpandedNodes();
					boolean b = expandedNodes.size() == 0;
					if (model.getBrowserType() == TAGS_EXPLORER)
						b = expandedNodes.size() > 0;
					if (m != null && m.size() > 0 && b) {
						j = m.entrySet().iterator();
						while (j.hasNext()) {
							e = (Entry) j.next();
							finder = new NodesFinder((Class) e.getKey(),
									(List) e.getValue());
							accept(finder);
							found = finder.getNodes();
							if (found.size() > 0) {
								k = found.iterator();
								TreeImageDisplay n, c;
								while (k.hasNext()) {
								    n = k.next();
								    if (n.isExpanded()) view.expandNode(n);
								    if (n.getUserObject() instanceof
								            ProjectData) {
								        List ll = n.getChildrenDisplay();
								        if (!CollectionUtils.isEmpty(ll)) {
								            Iterator w = ll.iterator();
								            while (w.hasNext()) {
								                c = (TreeImageDisplay) w.next();
								                if (c.isExpanded())
								                    view.expandNode(c);
								            }
								        }

								    }
								}
							}
						}
					}
				}
			}
		}
		
		model.setSelectedDisplay(null, true);
		model.setState(READY);
		switch (model.getBrowserType()) {
			case TAGS_EXPLORER:
				List<Class> types = new ArrayList<Class>();
				types.add(TagAnnotationData.class);
				types.add(DatasetData.class);
				countItems(types, null);
				countExperimenterDataInFolders();
				break;
			case ADMIN_EXPLORER:
				countItems(null, null);
				break;
			default:
				TreeImageDisplay root = view.getTreeRoot();
				TreeImageSet groupNode;
				for (int k = 0; k < root.getChildCount(); k++) {
					groupNode = (TreeImageSet) root.getChildAt(k);
					if (groupNode.isExpanded()) {
						countItems(null, groupNode);
					}
				}
		}
		model.getParentModel().setStatus(false, "", true);
		PartialNameVisitor v = new PartialNameVisitor(view.isPartialName());
		accept(v, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
		if (ProjectData.class.equals(type) || DatasetData.class.equals(type) ||
        		ScreenData.class.equals(type)) {
        	finder = new NodesFinder(type, id);
			accept(finder);
			Set<TreeImageDisplay> found = finder.getNodes();
			if (found.size() > 0) {
				Iterator<TreeImageDisplay> n = found.iterator();
				TreeImageDisplay display;
				if (DatasetData.class.equals(type)) {
					while (n.hasNext()) {
						display = n.next();
						setSelectedDisplay(display);
						view.expandNode(display, true);
					}
				} else {
					while (n.hasNext()) {
						setSelectedDisplay(n.next());
					}
				}	
			}
        }
		switch(model.getBrowserType()) {
	    	case Browser.PROJECTS_EXPLORER:
	    	case Browser.SCREENS_EXPLORER:
	    		ParentVisitor visitor = new ParentVisitor();
	    		accept(visitor, ParentVisitor.TREEIMAGE_SET_ONLY);
	    		EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
	    		bus.post(new ExperimenterLoadedDataEvent(visitor.getData()));
    	}
		fireStateChange(); 
	}

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#countExperimenterImages(TreeImageDisplay)
     */
	public void countExperimenterImages(TreeImageDisplay expNode)
	{
		if (expNode == null)
			throw new IllegalArgumentException("Node not valid.");
		Object ho = expNode.getUserObject();
		if (!(ho instanceof ExperimenterData || ho instanceof GroupData))
			throw new IllegalArgumentException("Node not valid.");
		switch (model.getState()) {
			case DISCARDED:
			case LOADING_LEAVES:
				throw new IllegalStateException(
	                    "This method cannot be invoked in the DISCARDED or" +
	                    "LOADING_LEAVES state.");
		}  
		int browserType = model.getBrowserType();
		if (!(browserType == IMAGES_EXPLORER || browserType == FILES_EXPLORER
			|| browserType == TAGS_EXPLORER))
			return;
        model.fireCountExperimenterImages((TreeImageSet) expNode);
        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, false);
        fireStateChange();
	}

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setExperimenterCount(TreeImageSet, int, int)
     */
	public void setExperimenterCount(TreeImageSet expNode, int index, Object v)
	{
		if (expNode == null)
			throw new IllegalArgumentException("Node not valid.");
		Object ho = expNode.getUserObject();
		if (!(ho instanceof ExperimenterData || ho instanceof GroupData))
			throw new IllegalArgumentException("Node not valid.");
		int browserType = model.getBrowserType();
		if (!(browserType == IMAGES_EXPLORER || browserType == FILES_EXPLORER
				|| browserType == TAGS_EXPLORER))
			return;
		boolean b = model.setExperimenterCount(expNode, index);
		if (index != -1 && v != null) {
			view.setCountValues(expNode, index, v);
		}
		if (b) view.getTreeDisplay().repaint();
	    model.getParentModel().setStatus(false, "", true);
	    fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getNodeOwner(TreeImageDisplay)
	 */
	public ExperimenterData getNodeOwner(TreeImageDisplay node)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node specified.");
		TreeImageDisplay n = BrowserFactory.getDataOwner(node);
		if (n == null) return model.getUserDetails();
		return (ExperimenterData) n.getUserObject();
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getNodeGroup(TreeImageDisplay)
	 */
	public GroupData getNodeGroup(TreeImageDisplay node)
	{
		if (node == null) 
			throw new IllegalArgumentException("No node specified.");
		TreeImageDisplay n = EditorUtil.getDataGroup(node);
		if (n == null) return model.getUserDetails().getDefaultGroup();
		return (GroupData) n.getUserObject();
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getSecurityContext(TreeImageDisplay)
	 */
	public SecurityContext getSecurityContext(TreeImageDisplay node)
	{
		return model.getSecurityContext(node);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getClickComponent()
	 */
	public JComponent getClickComponent()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
					"in the DISCARDED state.");
		return view.getTreeDisplay();
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setSelected(boolean)
	 */
    public void setSelectedNode()
    {
    	if (toSelectAfterSave == null) return;
    	setSelectedDisplay(toSelectAfterSave);
    	toSelectAfterSave = null;
    }

    /**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#refreshAdmin(Object)
	 */
	public void refreshAdmin(Object data)
	{
		if (model.getState() == DISCARDED) return;
		if (model.getBrowserType() == ADMIN_EXPLORER && model.isSelected()) {
			//visit the browser
			TreeImageDisplay node = model.getLastSelectedDisplay();
			refreshBrowser();
			setSelectedDisplay(node, true);
			return;
		}
		if (data instanceof ExperimenterData || data instanceof GroupData) {
            ExperimenterVisitor v;
            GroupData g = null;
            if (data instanceof ExperimenterData) {
                ExperimenterData exp = (ExperimenterData) data;
                v = new ExperimenterVisitor(this, exp.getId(), -1);
            } else {
                g = (GroupData) data;
                v = new ExperimenterVisitor(this, g.getId());
            }
            accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
            List<TreeImageDisplay> l = v.getNodes();
            Iterator<TreeImageDisplay> i = l.iterator();
            TreeImageDisplay n;
            if (data instanceof ExperimenterData) {
                while (i.hasNext()) {
                    n = i.next();
                    n.setUserObject(model.getUserDetails());
                    view.reloadNode(n);
                }
            } else {
                while (i.hasNext()) {
                    n = i.next();
                    n.setUserObject(g);
                    view.reloadNode(n);
                    l = n.getChildrenDisplay();
                    i = l.iterator();
                    while (i.hasNext()) {
                        n = i.next();
                        if (n.isExpanded()) view.expandNode(n);
                    }
                }
            }
        }
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#browse(TreeImageDisplay, DataObject, boolean)
	 */
	public void browse(TreeImageDisplay node, DataObject data, 
			boolean withThumbnails)
	{
		//if (node == null) return;
		model.getParentModel().browse(node, data, withThumbnails);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#onSelectedNode(Object, Object, Boolean)
	 */
	public void onSelectedNode(Object parent, Object selected, 
					Boolean multiSelection)
	{
		TreeImageDisplay foundNode;
		if (selected instanceof DataObject) {
			NodeSelectionVisitor visitor = new NodeSelectionVisitor(parent, 
													(DataObject) selected);
			accept(visitor);
			foundNode = visitor.getSelectedNode();
			if (foundNode != null) {		
				if (multiSelection) model.addFoundNode(foundNode);
				else model.setSelectedDisplay(foundNode, true);
				view.setFoundNode(model.getSelectedDisplays());
			} else 
				view.setFoundNode(null);
		} else if (selected instanceof TreeImageDisplay) {
			foundNode = (TreeImageDisplay) selected;
			if (multiSelection) model.addFoundNode(foundNode);
			else model.setSelectedDisplay(foundNode, true);
			view.setFoundNode(model.getSelectedDisplays());
		} else if (selected instanceof List) {
			NodeSelectionVisitor visitor = new NodeSelectionVisitor(parent, 
					(List<DataObject>) selected);
			accept(visitor);
			List<TreeImageDisplay> nodes = visitor.getSelectedNodes();
			if (nodes.size() == 0) {
				view.setFoundNode(null);
			} else if (nodes.size() == 1) {
				model.setSelectedDisplay(nodes.get(0), true);
				view.setFoundNode(model.getSelectedDisplays());
			} else {
				model.setSelectedDisplay(null, true);
				Iterator<TreeImageDisplay> i = nodes.iterator();
				while (i.hasNext())
					model.addFoundNode(i.next());
				view.setFoundNode(model.getSelectedDisplays());
			}
        } else if (selected instanceof String) {
            // this is the case if the 'orphaned images' folder
            // is selected
            model.setSelectedDisplay(null, true);
            view.setFoundNode(null);
        }
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#onDeselectedNode(Object, Object, Boolean)
	 */
	public void onDeselectedNode(Object parent, Object selected, 
			Boolean multiSelection)
	{
		if (selected instanceof DataObject) {
			NodeSelectionVisitor visitor = new NodeSelectionVisitor(parent, 
													(DataObject) selected);
			accept(visitor);
			TreeImageDisplay foundNode = visitor.getSelectedNode();
			if (foundNode != null) {
				model.removeDisplay(foundNode);
				view.setFoundNode(model.getSelectedDisplays());
			} else 
				view.setFoundNode(null);
		}
		
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setTimeIntervalImages(Set, TreeImageTimeSet)
	 */
	public void setTimeIntervalImages(Set set, TreeImageTimeSet node)
	{
		model.getParentModel().browseTimeInterval(node, set);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#onImportFinished(List)
	 */
	public void onImportFinished(List<TreeImageDisplay> nodes)
	{
		if (model.getState() == DISCARDED) return;
		if (nodes == null) return;
		//reload the node.
		
		Iterator<TreeImageDisplay> i;
		TreeImageDisplay n;
		Object ho;
		
		switch (model.getBrowserType()) {
			case PROJECTS_EXPLORER:
				SimilarNodesVisitor v = new SimilarNodesVisitor(nodes);
				accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
				nodes = v.getFoundNodes();
				i = nodes.iterator();
				while (i.hasNext()) {
					n = i.next();
					ho = n.getUserObject();
					if (ho instanceof DatasetData) {
						view.reloadContainer(n);
					}
				}
				break;
		}
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setImportedFile(ImageData)
	 */
	public void setImportedFile(ImageData image)
	{
		if (model.getState() == DISCARDED) return;
		if (image == null) return;
		model.addImportedImage(image);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#isFileImported(String)
	 */
	public boolean isFileImported(String path)
	{
		if (model.getState() == DISCARDED) return false;
		return model.isFileImported(path);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#deleteObjects(List)
	 */
	public void deleteObjects(List nodes)
	{
		model.getParentModel().deleteObjects(nodes);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#showMenu(int, Component, Point)
	 */
	public void showMenu(int index, Component invoker, Point loc)
	{
		model.getParentModel().showMenu(index, invoker, loc);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#refreshBrowser()
	 */
	public void refreshBrowser()
	{
		model.getParentModel().refreshTree();
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#removeTreeNodes(Collection)
	 */
	public void removeTreeNodes(Collection<TreeImageDisplay> nodes)
	{
		if (nodes == null) return;
		Iterator<TreeImageDisplay> i = nodes.iterator();
		TreeImageDisplay node;
		TreeImageDisplay parent;
		while (i.hasNext()) {
			node = i.next();
			parent = node.getParentDisplay();
			if (parent != null) {
				parent.removeChildDisplay(node);
				node.removeFromParent();
				view.reloadNode(parent);
			}
		}
		/* ensure that the selected displays do not include any removed nodes */
		final Set<TreeImageDisplay> oldSelected = Sets.newHashSet(getSelectedDisplays());
		final Set<TreeImageDisplay> newSelected = Sets.newHashSet(oldSelected);
		newSelected.removeAll(nodes);
		if (!newSelected.equals(oldSelected)) {
		    setSelectedDisplay(null);
		    setSelectedDisplays(newSelected.toArray(new TreeImageDisplay[newSelected.size()]), false);
		}
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#reActivate()
	 */
	public void reActivate()
	{
		model.setSelectedDisplay(null, true);
		view.reActivate();
		if (model.isSelected() &&
			model.getBrowserType() == Browser.ADMIN_EXPLORER) {
			model.setState(NEW);
			activate();
		}
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setRepositories(TreeImageDisplay, FSFileSystemView)
	 */
	public void setRepositories(TreeImageDisplay expNode,
			FSFileSystemView systemView)
	{
		int state = model.getState();
        if (state != LOADING_DATA)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_DATA "+
                    "state.");
        if (model.getBrowserType() != FILE_SYSTEM_EXPLORER) return;
        if (systemView == null) 
        	throw new NullPointerException("No File System.");
      
        if (expNode == null)
        	throw new IllegalArgumentException("Experimenter node not valid.");
        Object uo = expNode.getUserObject();
        if (!(uo instanceof ExperimenterData))
        	throw new IllegalArgumentException("Experimenter node not valid.");
        model.setRepositories(systemView);
    	view.loadFileSystem(expNode);
        countItems(null, expNode);
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setGroups(Set, List)
	 */
	public void setGroups(Collection groups, List expanded)
	{
		int state = model.getState();
        if (state != LOADING_DATA) return;
        /*
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_DATA "+
                    "state.");
                    */
        if (model.getBrowserType() != ADMIN_EXPLORER) return;
		Set nodes = TreeViewerTranslator.transformGroups(groups);
		view.setGroups(nodes, expanded);
		model.setState(READY);
		countItems(null, null);
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#register(DataObject)
	 */
	public boolean register(DataObject file)
	{
		if (file == null)
			throw new IllegalArgumentException("No file to register.");
		if (model.getBrowserType() != FILE_SYSTEM_EXPLORER) return false;
		if (!(file instanceof FileData || file instanceof ImageData))
			return false;
		if (file.getId() > 0) return false;
		try {
			TreeImageDisplay d = model.getLastSelectedDisplay();
			TreeImageDisplay exp = BrowserFactory.getDataOwner(d);
			if (exp == null) return false;
			Object ho  = exp.getUserObject();
			if (!(ho instanceof ExperimenterData)) return false;
			long id = ((ExperimenterData) ho).getId();
			if (file instanceof ImageData) {
				ImageData img = (ImageData) file;
				if (img.getIndex() >= 0) {
					TreeImageDisplay pd = d.getParentDisplay();
					if (pd == null) return false;
					if (!(pd.getUserObject() instanceof MultiImageData))
						return false;
					file = (DataObject) pd.getUserObject();
				}
			}
			DataObject o = model.getRepositories(id).register(file);
			if (o == null) return false;
		} catch (FSAccessException e) {
			LogMessage msg = new LogMessage();
			msg.print("Cannot register the object.");
			msg.print(e);
			TreeViewerAgent.getRegistry().getLogger().error(this, msg);
			return false;
		}
		view.repaint();
		return true;
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setExperimenters(TreeImageSet, Collection)
	 */
	public void setExperimenters(TreeImageSet node, List result)
	{
		if (result.size() != 1) return;
		Object ho = result.get(0);
		node.setUserObject(ho);
		GroupData g = (GroupData) ho;
		Set nodes = TreeViewerTranslator.transformExperimenters(
				g.getExperimenters());
		view.setLeavesViews(nodes, node);
		model.setState(READY);
		model.getParentModel().setLeaves(node, g.getExperimenters());
		model.getParentModel().setStatus(false, "", true);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see Browser#canEdit(Object)
	 */
	public boolean canEdit(Object ho)
	{
		return model.getParentModel().canEdit(ho);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see Browser#canAnnotate(Object)
	 */
	public boolean canAnnotate(Object ho)
	{
		return model.getParentModel().canAnnotate(ho);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#expandUser()
	 */
	public void expandUser()
	{
		if (model.getState() == DISCARDED) return;
		SecurityContext ctx = model.getSecurityContext(
				model.getLastSelectedDisplay());
		long id = model.getUserID();
		ExperimenterVisitor v = new ExperimenterVisitor(this, id,
				ctx.getGroupID());
		accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		List<TreeImageDisplay> values = v.getNodes();
		if (values.size() != 1) return;
		TreeImageDisplay n = values.get(0);
		if (!n.isExpanded()) view.expandNode(n);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#expandUser()
	 */
	public void resetPassword(String value)
	{
		if (model.getState() == DISCARDED) return;
		model.getParentModel().resetPassword(value);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#refreshBrowser(Class, long)
	 */
	public void refreshBrowser(Class type, long id)
	{
		if (ProjectData.class.equals(type) || 
				DatasetData.class.equals(type) || 
				ScreenData.class.equals(type)) {
		    ExperimenterVisitor visitor = new ExperimenterVisitor(this, -1, -1);
		    accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		    List<TreeImageDisplay> nodes = visitor.getNodes();
		    if (nodes.size() == 0) return;
		    Iterator<TreeImageDisplay> i = nodes.iterator();
		    List<Long> ids = new ArrayList<Long>(nodes.size());
		    while (i.hasNext()) {
		    	ids.add(i.next().getUserObjectId());
			}
		    refreshExperimenterData(ids);
		}
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#addComponent(JComponent)
	 */
	public void addComponent(JComponent component)
	{
		if (getBrowserType() != SCREENS_EXPLORER) return;
		view.addComponent(component);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#loadDirectory(TreeImageDisplay)
	 */
	public void loadDirectory(TreeImageDisplay display)
	{
		if (display == null) return;
		Object ho = display.getUserObject();
		if (!(ho instanceof FileData)) return;
		FileData f = (FileData) ho;
		if (f.isDirectory()) {
			if (!display.isChildrenLoaded())
				view.loadFile(display);
			//model.getParentModel().browse(display, true);
		}
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getNodesForUser(long, TreeImageDisplay)
	 */
	public List<TreeImageDisplay> getNodesForUser(long userID, TreeImageDisplay
			node)
	{
		if (model.getState() == DISCARDED) return null;
		if (userID < 0) return null;
		SecurityContext context = model.getSecurityContext(node);
		ExperimenterVisitor v = new ExperimenterVisitor(this, userID, 
				context.getGroupID());
		accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		List<TreeImageDisplay> nodes = v.getNodes();
		if (nodes .size() != 1) return new ArrayList<TreeImageDisplay>();
		TreeImageDisplay n = nodes.get(0);
		return n.getChildrenDisplay();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#rejectTransfer()
	 */
	public void rejectTransfer()
	{
		JTree tree = view.getTreeDisplay();
		if (tree instanceof DnDTree) {
			((DnDTree) tree).reset();
		}
	}

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setUserGroup(GroupData)
     */
	public void setUserGroup(GroupData group)
	{
		if (group == null)
			throw new IllegalArgumentException("Group cannot be null.");
		view.setUserGroup(Arrays.asList(group));
	}

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#removeGroup(GroupData)
     */
	public void removeGroup(GroupData group)
	{
		if (group == null) return;
		view.removeGroup(group);
	}
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#canLink(Object)
	 */
	public boolean canLink(Object ho)
	{
		return model.getParentModel().canLink(ho);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getDataToCopy()
	 */
	public List<DataObject> getDataToCopy()
	{ 
		return model.getParentModel().getDataToCopy();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#paste(TreeImageDisplay[])
	 */
	public void paste(TreeImageDisplay[] parents)
	{
		model.getParentModel().paste(parents);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#setNodesToCopy(TreeImageDisplay[], int)
	 */
	public void setNodesToCopy(TreeImageDisplay[] nodes, int index)
	{
		model.getParentModel().setNodesToCopy(nodes, index);
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getLoggedExperimenterNode()
     */
	public TreeImageDisplay getDefaultGroupNode()
	{
		SecurityContext ctx = model.getSecurityContext(null);
		long id = ctx.getGroupID();
		ExperimenterVisitor visitor = new ExperimenterVisitor(this, id);
		accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		List<TreeImageDisplay> nodes = visitor.getNodes();
		if (nodes.size() != 1) return null;
		return nodes.get(0);
	}

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getDisplayMode()
     */
	public int getDisplayMode() { return model.getDisplayMode(); }

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#changeDisplayMode()
	 */
	public void changeDisplayMode()
	{
		if (model.getBrowserType() == Browser.ADMIN_EXPLORER)
			return;
		model.setSelectedDisplay(null, true);
		//view.changeDisplayMode();
		ExperimenterVisitor v = new ExperimenterVisitor(this, -1);
		accept(v, ExperimenterVisitor.TREEIMAGE_SET_ONLY);
		List<TreeImageDisplay> nodes = v.getNodes();
		
		//Check the group already display
		//Was in group mode
		view.clear();
		List<GroupData> groups = new ArrayList<GroupData>(nodes.size());
		Iterator<TreeImageDisplay> i = nodes.iterator();
		while (i.hasNext()) {
			groups.add((GroupData) i.next().getUserObject());
		}
		if (model.isSingleGroup()) view.reActivate();
        else view.setUserGroup(groups);
	}
	
}