/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserComponent
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
import java.awt.Cursor;
import java.awt.Point;
import java.util.ArrayList;
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

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDef;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.EditVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.RefreshVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
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
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.MultiImageData;
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
        	display.setChildrenLoaded(Boolean.TRUE);
        else if (ho instanceof TagAnnotationData) { 
        	TagAnnotationData tag = (TagAnnotationData) ho;
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
        		display.setChildrenLoaded(Boolean.TRUE);
        	else display.setChildrenLoaded(Boolean.FALSE);
        } else {
        	display.setChildrenLoaded(Boolean.FALSE);
        }
        view.createNodes(nodes, display, parentDisplay);
        //Object o = display.getUserObject();
        countItems(null);
        /*
        if (o instanceof DatasetData) {// || o instanceof TagAnnotationData) {
        	Set<DataObject> ids = new HashSet<DataObject>();
        	ids.add((DataObject) o);
        	//countItems(ids);
        	
        	//model.fireContainerCountLoading(ids);
        }*/
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
			if (node.getUserObject() instanceof ExperimenterData)
				countExperimenterImages(node);
		}
    }
    
	/** 
	 * Retrieves the nodes to count the value for.
	 *
	 * @param rootType The type of node to track.
	 */
	private void countItems(List<Class> rootType)
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
		accept(finder, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		Set<DataObject> items = finder.getContainers();
		Set<TreeImageSet> nodes = finder.getContainerNodes();
		if (items.size() == 0 && nodes.size() == 0) return;
		model.fireContainerCountLoading(items, nodes);
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
		accept(finder, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		Set<DataObject> items = finder.getContainers();
		Set<TreeImageSet> nodes = finder.getContainerNodes();
		if (items.size() == 0 && nodes.size() == 0) return;
		model.fireContainerCountLoading(items, nodes);
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
    	//if (hasDataToSave(display)) return;
    	TreeImageDisplay oldDisplay = model.getLastSelectedDisplay();
    	//if (oldDisplay != null && oldDisplay.equals(display)) return; 
    	TreeImageDisplay exp = null;
    	Object ho;
    	if (display != null) {
    		ho = display.getUserObject();
    		if (ho instanceof ExperimenterData) {
    			if (getBrowserType() != ADMIN_EXPLORER) {
    				exp = display;
        			display = null;
    			}
    		}
    	}
    	addComponent(null);
    	if (exp != null) model.setSelectedDisplay(exp, single);
    	else model.setSelectedDisplay(display, single);
    	//if (display == null) view.setNullSelectedNode();
    	if (oldDisplay != null && oldDisplay.equals(display)) {
    		ho = oldDisplay.getUserObject();
    		if (ho instanceof PlateData)
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
            	view.loadExperimenterData();
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
        /*
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_LEAVES "+
                    "state.");
        */
        if (leaves == null) throw new NullPointerException("No leaves.");
        Object ho = expNode.getUserObject();
        if (!(ho instanceof ExperimenterData))
        	throw new IllegalArgumentException("Experimenter not valid");
        if (model.getBrowserType() == FILE_SYSTEM_EXPLORER) {
        	model.getParentModel().setLeaves(parent, leaves);
        	model.setState(READY);
        	fireStateChange();
            return;
        }
        ExperimenterData exp = (ExperimenterData) ho;
        long userID = exp.getId();
        long groupID = exp.getDefaultGroup().getId();
        
        Set visLeaves = TreeViewerTranslator.transformHierarchy(leaves, userID, 
                                                                groupID);
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
        	countItems(types);
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
     * @see Browser#getRootID()
     */
    public long getRootID()
    {
        if (model.getState() == DISCARDED)
		    throw new IllegalStateException(
                    "This method can't only be invoked in the DISCARDED " +
                    "state.");
        return model.getRootID();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setContainerCountValue(int, long, Set)
     */
    public void setContainerCountValue(long containerID, long value, 
    		Set<TreeImageSet> nodes)
    {
        //int state = model.getState();
        boolean b = model.setContainerCountValue(view.getTreeDisplay(), 
									containerID, value, nodes);
        if (b) 
        	view.getTreeDisplay().repaint();
        
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
        TreeImageDisplay[] nodes = model.getSelectedDisplays();
        //setSelectedDisplay(null); 24/03
        if (nodes != null && nodes.length == 1) {
        	TreeImageDisplay n = nodes[0];
        	if (!(n.getUserObject() instanceof ExperimenterData)) {
        		setSelectedDisplays(nodes, false);
        	}
        } else {
        	 setSelectedDisplays(nodes, false);
        }
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
        TreeImageDisplay loggedUser = view.getLoggedExperimenterNode();
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
            long userID = model.getUserID();
            long groupID = model.getUserGroupID();
            //Get the user node.
            if (parentDisplay == null)
            	parentDisplay = getLastSelectedDisplay();
            TreeImageDisplay newNode = 
            		TreeViewerTranslator.transformDataObject(object, userID, 
            								groupID);
           
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
		TreeImageDisplay loggedUser = view.getLoggedExperimenterNode();
		List<TreeImageDisplay> nodes = new ArrayList<TreeImageDisplay>(1);
        nodes.add(loggedUser);
        long userID = model.getUserID();
        //long model.get
        long groupID = model.getUserGroupID();
        createNodes(nodes, 
                TreeViewerTranslator.transformDataObject(data, userID, 
                        groupID), loggedUser);
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
        //TreeImageDisplay[] oldNodes = model.getSelectedDisplays();
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
        countItems(null);
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
        if (state != LOADING_DATA)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_DATA "+
                    "state.");
        if (nodes == null) throw new NullPointerException("No nodes.");
      
        if (expNode == null)
        	throw new IllegalArgumentException("Experimenter node not valid.");
        Object uo = expNode.getUserObject();
        if (!(uo instanceof ExperimenterData))
        	throw new IllegalArgumentException("Experimenter node not valid.");
        ExperimenterData exp = (ExperimenterData) uo;
        Set convertedNodes = TreeViewerTranslator.transformHierarchy(nodes, 
					exp.getId(), -1);//exp.getDefaultGroup().getId());
        view.setExperimenterData(convertedNodes, expNode);
        model.setState(READY);
        
        countItems(null);
        countExperimenterDataInFolders();
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#addExperimenter(ExperimenterData, boolean)
     */
	public void addExperimenter(ExperimenterData experimenter, boolean load)
	{
		if (experimenter == null)
			throw new IllegalArgumentException("Experimenter cannot be null.");
		//Make sure the user is not already display
		List<TreeImageDisplay> nodes = new ArrayList<TreeImageDisplay>(1);
		nodes.add(new TreeImageSet(experimenter));
		SimilarNodesVisitor visitor = new SimilarNodesVisitor(nodes);
		accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		
		if (visitor.getFoundNodes().size() > 0) return;
		setSelectedDisplay(null);
		view.addExperimenter(experimenter, load);
	}

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#removeExperimenter(ExperimenterData)
     */
	public void removeExperimenter(ExperimenterData exp)
	{
		if (exp == null)
			throw new IllegalArgumentException("Experimenter cannot be null.");
		view.removeExperimenter(exp);
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
		long id;
		RefreshVisitor v = new RefreshVisitor(this);
		if (model.getBrowserType() == ADMIN_EXPLORER) {
			display = view.getTreeRoot();
			id = TreeViewerAgent.getUserDetails().getId();
		} else {
			display = model.getLastSelectedDisplay();
			if (display == null) return;
			Object ho = display.getUserObject();
			if (!(ho instanceof ExperimenterData)) return;
			id = display.getUserObjectId();
		}
		display.accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		RefreshExperimenterDef def = new RefreshExperimenterDef(
								(TreeImageSet) display, 
								v.getFoundNodes(), v.getExpandedTopNodes());
		Map<Long, RefreshExperimenterDef> 
			m = new HashMap<Long, RefreshExperimenterDef>(1);
		m.put(id, def);
		model.loadRefreshExperimenterData(m, null, -1, null, null);
		fireStateChange();
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getLoggedExperimenterNode()
     */
	public TreeImageDisplay getLoggedExperimenterNode()
	{
		return view.getLoggedExperimenterNode();
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
		TreeImageDisplay node = view.getLoggedExperimenterNode();
		if (node == null) return;
		Object ho = node.getUserObject();
		if (!(ho instanceof ExperimenterData)) return;
		//
		RefreshVisitor v = new RefreshVisitor(this);
		node.accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		RefreshExperimenterDef def = new RefreshExperimenterDef(
								(TreeImageSet) node, 
								v.getFoundNodes(), v.getExpandedTopNodes());
		Map<Long, RefreshExperimenterDef> 
			m = new HashMap<Long, RefreshExperimenterDef>(1);
		m.put(node.getUserObjectId(), def);
		model.loadRefreshExperimenterData(m, null, -1, null, null);
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

	    TreeImageDisplay root = view.getTreeRoot();
	    //root.setToRefresh(false);
	    TreeImageSet expNode;
	    RefreshExperimenterDef def;
	    RefreshVisitor v = new RefreshVisitor(this);
	    int n = root.getChildCount();
	    Map<Long, RefreshExperimenterDef> 
	    	m = new HashMap<Long, RefreshExperimenterDef>(n);
	    Collection foundNodes;
	    Map topNodes;
	    int type = model.getBrowserType();
	    Iterator j;
	    for (int i = 0; i < n; i++) {
	    	expNode = (TreeImageSet) root.getChildAt(i);
	    	expNode.accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
	    	foundNodes = v.getFoundNodes();
	    	topNodes = v.getExpandedTopNodes();
	    	//reset the flag 
	    	if (type == Browser.IMAGES_EXPLORER)
	    		countExperimenterImages(expNode);
	    	def = new RefreshExperimenterDef(expNode, v.getFoundNodes(), 
					v.getExpandedTopNodes());
    		m.put(expNode.getUserObjectId(), def);
		}
	    model.loadRefreshExperimenterData(m, null, -1, refNode, toBrowse);
		fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setRefreshExperimenterData(Map, Class, long)
     */
	public void setRefreshExperimenterData(Map<Long, RefreshExperimenterDef> 
		nodes, Class type, long id)
	{
		//TODO: Should reselect the node previously selected.
		if (nodes == null || nodes.size() == 0) {
			model.setSelectedDisplay(null, true);
			model.setState(READY);
			countItems(null);
			if (model.getBrowserType() == TAGS_EXPLORER)
				countExperimenterDataInFolders();
			model.getParentModel().setStatus(false, "", true);
			return;
		}
		Iterator i = nodes.keySet().iterator();
		RefreshExperimenterDef node;
		TreeImageSet expNode;
		ExperimenterData exp;
		Set convertedNodes;
		long userId;
		int browserType = model.getBrowserType();
		Map<Integer, Set> results;
		while (i.hasNext()) {
			userId = (Long) i.next();
			node = nodes.get(userId);
			expNode = node.getExperimenterNode();
			exp = (ExperimenterData) expNode.getUserObject();
			if (browserType == IMAGES_EXPLORER || browserType == FILES_EXPLORER)
			{
				results = TreeViewerTranslator.refreshFolderHierarchy(
							(Map) node.getResults(), exp.getId(), -1);
				view.refreshFolder(expNode, results);
			} else {
				convertedNodes = TreeViewerTranslator.refreshHierarchy(
						(Map) node.getResults(), node.getExpandedTopNodes(), 
						exp.getId(), -1);
				view.setExperimenterData(convertedNodes, expNode);
			}
		}
		//expand the nodes.
		i = nodes.keySet().iterator();
		Map m;
		Entry entry;
		Iterator j;
		NodesFinder finder;
		if (type == null) {
			List l;
			Iterator k;
			Set<TreeImageDisplay> found;
			while (i.hasNext()) {
				userId = (Long) i.next();
				node = nodes.get(userId);
				expNode = node.getExperimenterNode();
				if (expNode.isExpanded()) {
					m = node.getExpandedTopNodes();
					if (m != null && m.size() > 0 && 
							node.getExpandedNodes().size() == 0) {
						j = m.entrySet().iterator();
						while (j.hasNext()) {
							entry = (Entry) j.next();
							finder = new NodesFinder((Class) entry.getKey(), 
									(List) entry.getValue());
							accept(finder);
							found = finder.getNodes();
							if (found.size() > 0) {
								k = found.iterator();
								while (k.hasNext()) {
									view.expandNode((TreeImageDisplay) k.next());
								}
							}
						}
					} else view.expandNode(expNode);
				}
			}
		}
		
		model.setSelectedDisplay(null, true);
		model.setState(READY);
		
		if (model.getBrowserType() == TAGS_EXPLORER) {
			List<Class> types = new ArrayList<Class>();
			types.add(TagAnnotationData.class);
			types.add(DatasetData.class);
			countItems(types);
			countExperimenterDataInFolders();
		} else countItems(null);
			
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
		fireStateChange(); 
	}

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#countExperimenterImages(TreeImageDisplay)
     */
	public void countExperimenterImages(TreeImageDisplay expNode)
	{
		if (expNode == null || 
			!(expNode.getUserObject() instanceof ExperimenterData))
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
		if (expNode == null || 
				!(expNode.getUserObject() instanceof ExperimenterData))
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
		if (model.getBrowserType() == ADMIN_EXPLORER) {
			//visit the browser
			TreeImageDisplay node = model.getLastSelectedDisplay();
			refreshBrowser();
			setSelectedDisplay(node, true);
		} else {
			if (data instanceof ExperimenterData) view.refreshExperimenter();
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
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#removeTreeNodes(List)
	 */
	public void reActivate()
	{
		view.reActivate();
		if (!model.isSelected()) return;
		//Reload data.
		view.loadExperimenterData();
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
        countItems(null);
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
		countItems(null);
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
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getUserGroupID()
	 */
	public long getUserGroupID()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.getUserGroupID();
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#isObjectWritable(Object)
	 */
	public boolean isUserOwner(Object ho)
	{
		return model.getParentModel().isUserOwner(ho);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see Browser#canDeleteObject(Object)
	 */
	public boolean canDeleteObject(Object ho)
	{
		return model.getParentModel().canDeleteObject(ho);
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#expandUser()
	 */
	public void expandUser()
	{
		if (model.getState() == DISCARDED) return;
		view.expandUser();
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
			TreeImageDisplay root = view.getTreeRoot();
		    TreeImageSet expNode;
		    RefreshExperimenterDef def;
		    RefreshVisitor v = new RefreshVisitor(this);
		    int n = root.getChildCount();
		    Map<Long, RefreshExperimenterDef> 
		    	m = new HashMap<Long, RefreshExperimenterDef>(n);
		    Collection foundNodes;
		    Map topNodes;
		    int index = model.getBrowserType();
		    for (int i = 0; i < n; i++) {
		    	expNode = (TreeImageSet) root.getChildAt(i);
		    	expNode.accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		    	foundNodes = v.getFoundNodes();
		    	topNodes = v.getExpandedTopNodes();
		    	if (index == Browser.IMAGES_EXPLORER)
		    		countExperimenterImages(expNode);
		    	def = new RefreshExperimenterDef(expNode, v.getFoundNodes(), 
						v.getExpandedTopNodes());
	    		m.put(expNode.getUserObjectId(), def);
			}
		    model.loadRefreshExperimenterData(m, type, id, null, null);
			fireStateChange();
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
	 * @see Browser#getNodesForUser(long)
	 */
	public List<TreeImageDisplay> getNodesForUser(long userID)
	{
		if (model.getState() == DISCARDED) return null;
		if (userID < 0) return null;
		return view.getNodesForUser(userID);
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

}
