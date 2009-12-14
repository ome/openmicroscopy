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
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.RefreshExperimenterDef;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.EditVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.RefreshVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
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

	/** 
	 * Retrieves the nodes to count the value for.
	 *
	 * @param rootType The type of node to track.
	 */
	private void countItems(Class rootType)
	{
		if (rootType == null) {
			int type = model.getBrowserType();
			if (type == PROJECT_EXPLORER) 
				rootType = DatasetData.class;
			else if (type == TAGS_EXPLORER)
				rootType = TagAnnotationData.class;
		}
		ContainerFinder finder = new ContainerFinder(rootType);
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
    			exp = display;
    			display = null;
    		}
    	}
    	if (exp != null) model.setSelectedDisplay(exp, single);
    	else model.setSelectedDisplay(display, single);
    	if (display == null) view.setNullSelectedNode();
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
                //view.loadRoot();
            	view.loadExperimenterData();
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
    public void setLeaves(Set leaves, TreeImageSet parent, 
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
        ExperimenterData exp = (ExperimenterData) ho;
        long userID = exp.getId();
        long groupID = -1;//exp.getDefaultGroup().getId();
        
        Set visLeaves = TreeViewerTranslator.transformHierarchy(leaves, userID, 
                                                                groupID);
        view.setLeavesViews(visLeaves, parent);
        
        model.setState(READY);
        if (parent != null && 
        		parent.getUserObject() instanceof TagAnnotationData)
        	countItems(DatasetData.class);
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
            case PROJECT_EXPLORER:
                return im.getIcon(IconManager.HIERARCHY_EXPLORER);
            case TAGS_EXPLORER:
                return im.getIcon(IconManager.TAGS_EXPLORER);
            case IMAGES_EXPLORER:
                return im.getIcon(IconManager.IMAGES_EXPLORER);
            case SCREENS_EXPLORER:
            	return im.getIcon(IconManager.SCREENS_EXPLORER);
            case FILES_EXPLORER:
                return im.getIcon(IconManager.FILES_EXPLORER);
            case FILE_SYSTEM_EXPLORER:
                return im.getIcon(IconManager.FILE_SYSTEM_EXPLORER);
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
        		setSelectedDisplays(nodes);
        	}
        } else {
        	 setSelectedDisplays(nodes);
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
		if ((data instanceof DatasetData) || (data instanceof DatasetData)) {
			if (type != PROJECT_EXPLORER) return;
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
			if (uo instanceof DataObject)
				objects.add((DataObject) uo);
		}
    	return objects;
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelectedDisplays(TreeImageDisplay[])
     */
    public void setSelectedDisplays(TreeImageDisplay[] nodes)
    {
    	/*
        if (nodes.length == 0) return;
        boolean b = nodes.length == 1;
        for (int i = 0; i < nodes.length; i++) {
        	setSelectedDisplay(nodes[i], b);
		}
		*/
        if (nodes.length == 0) return;
        if (nodes.length == 1) setSelectedDisplay(nodes[0], true);
        TreeImageDisplay[] oldNodes = model.getSelectedDisplays();
        boolean flush = false;
        if (oldNodes.length >= nodes.length) flush = true;
        int n = nodes.length;
        for (int i = 0; i < n; i++) {
        	if (i == 0) model.setSelectedDisplay(nodes[i], flush);
        	else model.setSelectedDisplay(nodes[i], false);
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
		if (exp == null || !(exp.getUserObject() instanceof ExperimenterData))
			throw new IllegalArgumentException("Node not valid.");
		switch (model.getState()) {
			case DISCARDED:
			case LOADING_LEAVES:
				return;
		}   
		
        if (n == null) model.fireExperimenterDataLoading((TreeImageSet) exp);
        else model.fireLeavesLoading(exp, n);
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
        if (model.getBrowserType() == FILE_SYSTEM_EXPLORER) {
        	model.setImportedImages(nodes);
        	view.loadFileSystem(false);
        } else {
        	//depending on the type of browser, present data 
            Set convertedNodes = TreeViewerTranslator.transformHierarchy(nodes, 
    					exp.getId(), -1);//exp.getDefaultGroup().getId());
            view.setExperimenterData(convertedNodes, expNode);
            model.setState(READY);
        }
        
        countItems(null);
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#addExperimenter(ExperimenterData, boolean)
     */
	public void addExperimenter(ExperimenterData experimenter, boolean load)
	{
		//TODO check state
		if (experimenter == null)
			throw new IllegalArgumentException("Experimenter cannot be null.");
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
		if (model.getBrowserType() == FILE_SYSTEM_EXPLORER) {
			view.loadFileSystem(true);
			return;
		}
		TreeImageDisplay display = model.getLastSelectedDisplay();
		if (display == null) return;
		Object ho = display.getUserObject();
		if (!(ho instanceof ExperimenterData)) return;
		//
		RefreshVisitor v = new RefreshVisitor(this);
		display.accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
		RefreshExperimenterDef def = new RefreshExperimenterDef(
								(TreeImageSet) display, 
								v.getFoundNodes(), v.getExpandedTopNodes());
		Map<Long, RefreshExperimenterDef> 
			m = new HashMap<Long, RefreshExperimenterDef>(1);
		m.put(display.getUserObjectId(), def);
		model.loadRefreshExperimenterData(m);
		fireStateChange();
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
		model.loadRefreshExperimenterData(m);
		fireStateChange();
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refreshTree()
     */
    public void refreshTree()
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
    		view.loadFileSystem(true);
    		return;
    	}
	    TreeImageDisplay root = view.getTreeRoot();
	    TreeImageSet expNode;
	    RefreshExperimenterDef def;
	    RefreshVisitor v = new RefreshVisitor(this);
	    int n = root.getChildCount();
	    Map<Long, RefreshExperimenterDef> 
	    	m = new HashMap<Long, RefreshExperimenterDef>(n);
	    int type = model.getBrowserType();
	    for (int i = 0; i < n; i++) {
	    	expNode = (TreeImageSet) root.getChildAt(i);
	    	expNode.accept(v, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
	    	if (type == Browser.IMAGES_EXPLORER)
	    		countExperimenterImages(expNode);
	    	def = new RefreshExperimenterDef(expNode, v.getFoundNodes(), 
	    									v.getExpandedTopNodes());
	    	m.put(expNode.getUserObjectId(), def);
		}
	    model.loadRefreshExperimenterData(m);
		fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setRefreshExperimenterData(Map)
     */
	public void setRefreshExperimenterData(
					Map<Long, RefreshExperimenterDef> nodes)
	{
		/*
		if (model.getState() != LOADING_DATA)
			throw new IllegalStateException("This method cannot be invoked "+
			"in the LOADING_DATA state.");
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("Experimenter cannot be null.");
			*/
		if (nodes == null || nodes.size() == 0) return;
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
		model.setSelectedDisplay(null, true);
		model.setState(READY);
		countItems(null);
		model.getParentModel().setStatus(false, "", true);
		PartialNameVisitor v = new PartialNameVisitor(view.isPartialName());
		accept(v, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
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
		if (browserType!= IMAGES_EXPLORER && browserType != FILES_EXPLORER) 
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
		if (browserType != IMAGES_EXPLORER && browserType != FILES_EXPLORER) 
			return;
		/*
		int state = model.getState();
		switch (state) {
			case COUNTING_ITEMS:
				model.setExperimenterCount(expNode, index);
				if (index != -1 && v != null) {
					view.setCountValues(expNode, index, v);
				}
				if (model.getState() == READY) fireStateChange();
	        case READY:
	        	model.setExperimenterCount(expNode, index);
	        	if (index != -1 && v != null) {
					view.setCountValues(expNode, index, v);
				}
	            view.getTreeDisplay().repaint();
	            break;
	        default:
	            throw new IllegalStateException(
	                    "This method can only be invoked in the " +
	                    "COUNTING_ITEMS or READY state.");
		}
		*/
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
	 * @see Browser#refreshExperimenter()
	 */
	public void refreshExperimenter()
	{
		if (model.getState() == DISCARDED) return;
		view.refreshExperimenter();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#browse(TreeImageDisplay, boolean)
	 */
	public void browse(TreeImageDisplay node, boolean withThumbnails)
	{
		if (node == null) return;
		model.getParentModel().browse(node, withThumbnails);
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#onSelectedNode(Object, Object, Boolean)
	 */
	public void onSelectedNode(Object parent, Object selected, 
					Boolean multiSelection)
	{
		if (selected instanceof DataObject) {
			NodeSelectionVisitor visitor = new NodeSelectionVisitor(parent, 
													(DataObject) selected);
			accept(visitor);
			TreeImageDisplay foundNode = visitor.getSelectedNode();
			if (foundNode != null) {		
				if (multiSelection) model.addFoundNode(foundNode);
				else model.setSelectedDisplay(foundNode, true);
				view.setFoundNode(model.getSelectedDisplays());
			} else 
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
		
		Iterator<TreeImageDisplay> i = nodes.iterator();
		TreeImageDisplay n;
		Object ho;
		
		switch (model.getBrowserType()) {
			case PROJECT_EXPLORER:
				while (i.hasNext()) {
					n = i.next();
					ho = n.getUserObject();
					if (ho instanceof DatasetData) {
						view.reloadContainer(n);
					}
				}
				break;
	
			default:
				break;
		}
		
			
		
		/*
		if (model.getBrowserType() != FILE_SYSTEM_EXPLORER) return;
		Iterator<TreeImageDisplay> i = nodes.iterator();
		while (i.hasNext()) 
			view.loadFile(i.next());
			*/
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
	 * @see Browser#showSupportedFiles()
	 */
	public void showSupportedFiles()
	{
		if (model.getState() == DISCARDED) return;
		firePropertyChange(FILE_FORMATS_PROPERTY, Boolean.valueOf(false), 
				Boolean.valueOf(true));
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#isObjectWritable(Object)
	 */
	public boolean isObjectWritable(Object ho)
	{
		return model.getParentModel().isObjectWritable(ho);
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
	 * @see Browser#isImporting()
	 */
	public boolean isImporting()
	{ 
		return model.getParentModel().isImporting();
	}

	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#showImporter()
	 */
	public void showImporter()
	{
		model.getParentModel().showImporter();
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
	 * @see Browser#removeTreeNodes(List)
	 */
	public void removeTreeNodes(List<TreeImageDisplay> nodes)
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
				parent.remove(node);
				view.reloadNode(parent);
			}
		}
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
        ExperimenterData exp = (ExperimenterData) ho;
        long userID = exp.getId();
        long groupID = -1;//exp.getDefaultGroup().getId();
        
        Set visLeaves = TreeViewerTranslator.transformHierarchy(leaves, userID, 
                                                                groupID);
        view.setLeavesViews(visLeaves, parent);
        
        model.setState(READY);
        if (parent != null && 
        		parent.getUserObject() instanceof TagAnnotationData)
        	countItems(DatasetData.class);
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
    
}
