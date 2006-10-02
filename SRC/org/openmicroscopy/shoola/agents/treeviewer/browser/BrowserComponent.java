/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserComponent
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
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.treeviewer.ShowProperties;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ClassificationVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.EditVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.LeavesVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.RefreshVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.SortCmd;
import org.openmicroscopy.shoola.agents.treeviewer.util.FilterWindow;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.ImageData;
import pojos.ProjectData;

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
    private BrowserModel    model;
    
    /** The View sub-component. */
    private BrowserUI       view;
    
    /** The Controller sub-component. */
    private BrowserControl  controller;
    
    /**
     * Returns the frame hosting the {@link BrowserUI view}.
     * 
     * @param c The parent container.
     * @return See above.
     */
    private JFrame getViewParent(Container c)
    {
        if (c instanceof JFrame) return (JFrame) c;
        return getViewParent(c.getParent());
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
        view.createNodes(nodes, display, parentDisplay);
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
        Object ho = node.getUserObject();
        if (ho instanceof DataObject) {
            EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
            bus.post(new ShowProperties((DataObject) ho, ShowProperties.EDIT));
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
    
    /** Links up the MVC triad. */
    void initialize()
    {
        model.initialize(this);
        controller.initialize(view);
        view.initialize(controller, model);
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
                view.loadRoot();
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
     * @see Browser#setNodes(Set)
     */
    public void setNodes(Set nodes)
    {
        if (model.getState() != LOADING_DATA)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_DATA "+
                    "state.");
        if (nodes == null) throw new NullPointerException("No nodes.");
        long userID = model.getUserID();
        long groupID = model.getRootGroupID();
        Set visNodes = TreeViewerTranslator.transformHierarchy(nodes, userID,
                                                            groupID);
        view.setViews(visNodes, true);
        model.setState(READY);
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
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
        if ((state == LOADING_DATA) || (state == LOADING_LEAVES) ||
             (state == COUNTING_ITEMS)) {
            model.cancel();
            if (state != COUNTING_ITEMS) 
                view.cancel(model.getLastSelectedDisplay()); 
            fireStateChange();
        }
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#loadData()
     */
    public void loadData()
    {
        int state = model.getState();
        if ((state == DISCARDED) || (state == LOADING_LEAVES))
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED or" +
                    "LOADING_LEAVES state.");
        if (model.getBrowserType() == Browser.IMAGES_EXPLORER)
            throw new IllegalArgumentException("Method should only be invoked" +
                    " by the Hiearchy and Category Explorer.");
        model.fireDataLoading();
        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, false);
        fireStateChange();
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
        if (model.getFilterType() == NO_IMAGES_FILTER) 
            view.loadAction(view.getTreeRoot());
        model.fireFilterDataLoading();
        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, false);
        fireStateChange();
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#loadData()
     */
    public void loadLeaves()
    {
        int state = model.getState();
        if ((state == DISCARDED) || (state == LOADING_LEAVES))
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED or " +
                    "LOADING_LEAVES state.");
        System.out.println("leaves");
        model.fireLeavesLoading();
        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, false);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setLeaves(Set)
     */
    public void setLeaves(Set leaves)
    {
        if (model.getState() != LOADING_LEAVES)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_LEAVES "+
                    "state.");
        if (leaves == null) throw new NullPointerException("No leaves.");
        long userID = model.getUserID();
        long groupID = model.getRootGroupID();
        Set visLeaves = TreeViewerTranslator.transformHierarchy(leaves, userID, 
                                                                groupID);
        view.setLeavesViews(visLeaves);
        model.setState(READY);
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setSelectedDisplay(TreeImageDisplay)
     */
    public void setSelectedDisplay(TreeImageDisplay display)
    {
        switch (model.getState()) {
            //case LOADING_DATA:
            //case LOADING_LEAVES:
            case DISCARDED:
                throw new IllegalStateException(
                        "This method cannot be invoked in the LOADING_DATA, "+
                        " LOADING_LEAVES or DISCARDED state.");
        }
        TreeImageDisplay oldDisplay = model.getLastSelectedDisplay();
        //if (oldDisplay != null && oldDisplay.equals(display)) return;
        if (display != null && display.getUserObject() instanceof String) 
            display = null;
        model.setSelectedDisplay(display);
        firePropertyChange(SELECTED_DISPLAY_PROPERTY, oldDisplay, display);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#showPopupMenu()
     */
    public void showPopupMenu()
    {
        int state = model.getState();
        switch (state) {
            case LOADING_DATA:
            case LOADING_LEAVES:
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can only be invoked in the LOADING_DATA, "+
                        " LOADING_LEAVES or DISCARDED state.");
        }
        firePropertyChange(POPUP_MENU_PROPERTY, null, view.getSelectedTree());
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
            case CATEGORY_EXPLORER:
                return im.getIcon(IconManager.CATEGORY_EXPLORER);
            case IMAGES_EXPLORER:
                return im.getIcon(IconManager.IMAGES_EXPLORER);
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
        	case COUNTING_ITEMS:
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
        SortCmd cmd = new SortCmd(this, sortType);
        cmd.execute();
        view.setSortedNodes(cmd.getSortedNodes());
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setFilterNodes(Set, int)
     */
    public void setFilterNodes(Set nodes, int type)
    {
        if (model.getState() != LOADING_DATA)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_DATA"+
                    "state.");
        if (nodes == null) throw new NullPointerException("No nodes.");
        int index = -1;
        if (type == Browser.IN_DATASET_FILTER) index = FilterWindow.DATASET;
        else if (type == Browser.IN_CATEGORY_FILTER) 
            index = FilterWindow.CATEGORY;
        if (index == -1) throw new IllegalStateException("Index not valid.");
        model.setState(READY);
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
        JFrame frame = getViewParent(view.getParent());
        long userID = model.getUserID();
        long groupID = model.getRootGroupID();
        Set n = TreeViewerTranslator.transformDataObjectsCheckNode(nodes, 
                                            userID, groupID);
        FilterWindow window = new FilterWindow(this, frame, index, n);
        window.addPropertyChangeListener(TreeViewer.FILTER_NODES_PROPERTY, 
                                        controller);
        window.addPropertyChangeListener(FilterWindow.CLOSE_PROPERTY, 
                                        controller);
        UIUtilities.centerAndShow(window);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#showFilterMenu(Component, Point)
     */
    public void showFilterMenu(Component c, Point p)
    {
        view.showFilterMenu(c, p);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#loadFilteredImageData(Set)
     */
    public void loadFilteredImageData(Set nodes)
    {
        int state = model.getState();
        if ((state == DISCARDED) || (state == LOADING_LEAVES))
            throw new IllegalStateException(
                    "This method cannot be invoked in the DISCARDED or" +
                    "LOADING_LEAVES state.");
        if (model.getBrowserType() != IMAGES_EXPLORER)
            throw new IllegalArgumentException("BrowserType not valid.");
        if (model.getFilterType() == NO_IMAGES_FILTER)
            throw new IllegalArgumentException("The method cannot be " +
                    "invoked for the NO_IMAGES_FILTER filter type.");
        if (nodes == null || nodes.size() == 0) 
            throw new IllegalArgumentException("No nodes.");
        model.fireFilteredImageDataLoading(nodes);
        model.getParentModel().setStatus(true, TreeViewer.LOADING_TITLE, false);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refresh()
     */
    public void refresh()
    {
        switch (model.getState()) {
            case LOADING_DATA:
            case LOADING_LEAVES:
            case DISCARDED:
                throw new IllegalStateException(
                        "This method cannot be invoked in the LOADING_DATA, "+
                        " LOADING_LEAVES or DISCARDED state.");
        }
        TreeImageDisplay display = model.getLastSelectedDisplay();
        if (display == null) return;
        if (!display.isChildrenLoaded() && display.numberItems != 0) return;
        TreeImageDisplay root = view.getTreeRoot();
        display.removeAllChildrenDisplay();
        if (root.equals(display)) {
            if (model.getBrowserType() == IMAGES_EXPLORER)
                loadFilteredImagesForHierarchy();
            else loadData();
        } else model.refreshSelectedDisplay();
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
	        case DISCARDED:
	            throw new IllegalStateException(
	                    "This method cannot be invoked in the LOADING_DATA, "+
	                    " LOADING_LEAVES or DISCARDED state.");
        }
        TreeImageDisplay root = view.getTreeRoot();
        //if (!root.isChildrenLoaded()) return;
	    if (!model.isSelected()) {
	        view.clearTree();
	        return;
	    }
        if (model.getBrowserType() == IMAGES_EXPLORER) {
            root.removeAllChildrenDisplay();
            model.setSelectedDisplay(root);
            loadFilteredImagesForHierarchy();
        } else {
            RefreshVisitor visitor = new RefreshVisitor(this);
            accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
            root.removeAllChildrenDisplay();
            model.setSelectedDisplay(root);
            if (visitor.getFoundNodes().size() == 0) loadData();
            else model.loadRefreshedData(visitor.getFoundNodes());
        }
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setContainerNodes(Set, TreeImageDisplay)
     */
    public void setContainerNodes(Set nodes, TreeImageDisplay parent)
    {
        int state = model.getState();
        if (state != LOADING_DATA)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_DATA "+
                    "state.");
        if (nodes == null) throw new NullPointerException("No nodes.");
        TreeImageDisplay parentDisplay = model.getLastSelectedDisplay();
        long userID = model.getUserID();
        long groupID = model.getRootGroupID();
        if (parent == null) { //root
            view.setViews(TreeViewerTranslator.transformHierarchy(nodes, userID,
                                                            groupID), true);
        }  else view.setViews(TreeViewerTranslator.transformContainers(nodes, 
                                        userID, groupID), 
                            parentDisplay);
        if (parentDisplay != null)
            parentDisplay.setChildrenLoaded(Boolean.TRUE);
        model.fireContainerCountLoading();
        model.getParentModel().setStatus(false, "", true);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getRootLevel()
     */
    public int getRootLevel()
    {
        if (model.getState() == DISCARDED)
		    throw new IllegalStateException(
                    "This method can't only be invoked in the DISCARDED " +
                    "state.");
        return model.getRootLevel();
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
        return model.getRootGroupID();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setContainerCountValue(int, int)
     */
    public void setContainerCountValue(long containerID, int value)
    {
        int state = model.getState();
        switch (state) {
	        case COUNTING_ITEMS:
	            model.setContainerCountValue(view.getTreeDisplay(), 
	                    					containerID, value);
	            if (model.getState() == READY) fireStateChange();
	            break;
	        case READY:
	            model.setContainerCountValue(view.getTreeDisplay(), 
    										containerID, value);
	            break;
	        default:
	            throw new IllegalStateException(
	                    "This method can only be invoked in the " +
	                    "COUNTING_ITEMS or READY state.");
        }
        model.getParentModel().setStatus(false, "", true);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getContainersWithImagesNodes()
     */
    public Set getContainersWithImagesNodes()
    {
        //Note: avoid caching b/c we don't know yet what we are going
        //to do with updates
        ContainerFinder finder = new ContainerFinder();
        accept(finder, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
        return finder.getContainerNodes();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getContainersWithImages()
     */
    public Set getContainersWithImages()
    {
        //Note: avoid caching b/c we don't know yet what we are going
        //to do with updates
        ContainerFinder finder = new ContainerFinder();
        accept(finder, TreeImageDisplayVisitor.TREEIMAGE_SET_ONLY);
        return finder.getContainers();
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
        ArrayList list = new ArrayList(nodes.size());
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
        TreeImageDisplay node = (TreeImageDisplay) l.get(index);
        view.selectFoundNode(node);
        Object ho = node.getUserObject();
        if (ho instanceof DataObject) {
            EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
            bus.post(new ShowProperties((DataObject) ho, ShowProperties.EDIT));
        }
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
        setSelectedDisplay(null);
        model.setSelected(b);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refreshEdition(DataObject, int)
     */
    public void refreshEdition(DataObject object, int op)
    {
        switch (model.getState()) {
            case NEW:
            case READY:   
                break;
            default:
                new IllegalStateException("This method can only be invoked " +
                        "in the NEW or READY state.");
        }
        Object o = object;
        List nodes = null;
        TreeImageDisplay parentDisplay = null;
        if (op == TreeViewer.CREATE_OBJECT) {
            TreeImageDisplay node = getLastSelectedDisplay();
            if ((object instanceof ProjectData) ||
                (object instanceof CategoryGroupData)) {
                nodes = new ArrayList(1);
                nodes.add(view.getTreeRoot());
                parentDisplay = view.getTreeRoot();
            } else 
                o = node.getUserObject();
        }
        if (nodes == null) {
            EditVisitor visitor = new EditVisitor(this, o);
            accept(visitor, TreeImageDisplayVisitor.ALL_NODES);
            nodes = visitor.getFoundNodes();
        }
        
        if (op == TreeViewer.UPDATE_OBJECT) view.updateNodes(nodes, object);
        else if (op == TreeViewer.REMOVE_OBJECT) removeNodes(nodes);
        else if (op == TreeViewer.CREATE_OBJECT) {
            long userID = model.getUserID();
            long groupID = model.getRootGroupID();
            if (parentDisplay == null)
                parentDisplay = getLastSelectedDisplay();
            createNodes(nodes, 
                    TreeViewerTranslator.transformDataObject(object, userID, 
                            groupID), parentDisplay);
        }     
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#refreshClassification(ImageData[], Set, int)
     */
    public void refreshClassification(ImageData[] images, Set categories, int m)
    {
        switch (model.getState()) {
            case NEW:
            case READY:   
                break;
            default:
                new IllegalStateException("This method can only be invoked " +
                        "in the NEW or READY state.");
        }
        if (categories == null)
            throw new IllegalArgumentException("Categories shouln't be null.");
        if (images == null)
            throw new IllegalArgumentException("No image.");
        if (images.length == 0)
            throw new IllegalArgumentException("No image.");
        if (m != Classifier.CLASSIFY_MODE && 
            m != Classifier.DECLASSIFY_MODE)
            throw new IllegalArgumentException("Classification mode not " +
                    "supported.");
        ImageData img;
        ClassificationVisitor visitor;
        List nodes;
        long userID = model.getUserID();
        long groupID = model.getRootGroupID();
        TreeImageDisplay d;
        int editorType = model.getBrowserType();
        for (int i = 0; i < images.length; i++) {
            img = images[i];
            visitor = new ClassificationVisitor(this, img, categories);
            accept(visitor, TreeImageDisplayVisitor.TREEIMAGE_NODE_ONLY);
            nodes = visitor.getFoundNodes();
            d = TreeViewerTranslator.transformDataObject(img, userID, groupID);
            if (editorType == CATEGORY_EXPLORER) {
                if (m == Classifier.CLASSIFY_MODE) {
                    createNodes(nodes, d, 
                            getLastSelectedDisplay().getParentDisplay());
                }
                else removeNodes(nodes);
            } else if (editorType == PROJECT_EXPLORER || 
                    editorType == IMAGES_EXPLORER)
                view.updateNodes(nodes, img);
        }
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setFilterType(int)
     */
    public void setFilterType(int type)
    {
        switch (model.getState()) {
            case LOADING_DATA:
            case LOADING_LEAVES:
            case DISCARDED:
                throw new IllegalStateException(
                        "This method cannot be invoked in the LOADING_DATA, "+
                        " LOADING_LEAVES or DISCARDED state.");
        }
        if (model.getBrowserType() != IMAGES_EXPLORER)
            throw new IllegalArgumentException("This method can only be " +
                    "invoked by the Images Explorer.");
        switch (type) {
            case NO_IMAGES_FILTER:
            case IN_DATASET_FILTER:
            case IN_CATEGORY_FILTER:
                break;
            default:
                throw new IllegalArgumentException("Filter not supported.");
        }
        //if (model.getFilterType() == type) return;
        model.setFilterType(type);
        refreshTree();
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getLeaves()
     */
    public Set getLeaves()
    {
        if (model.getBrowserType() != IMAGES_EXPLORER) 
            throw new IllegalArgumentException("This method sould only " +
                    "be invoked for the Images Explorer.");
        LeavesVisitor visitor = new LeavesVisitor(this);
        accept(visitor);
        return visitor.getNodeIDs();
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
     * @see Browser#setSelectedDisplays(TreeImageDisplay[])
     */
    public void setSelectedDisplays(TreeImageDisplay[] nodes)
    {
        if (nodes.length == 0) return;
        TreeImageDisplay oldDisplay = model.getLastSelectedDisplay();
        TreeImageDisplay display = nodes[nodes.length-1];
        if (oldDisplay != null && oldDisplay.equals(display)) return;
        model.setSelectedDisplays(nodes);
        firePropertyChange(SELECTED_DISPLAY_PROPERTY, oldDisplay, display);
    }

    /**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#navigate(boolean)
     */
    public void navigate(boolean v)
    {
        controller.getAction(BrowserControl.BACKWARD_NAV).setEnabled(!v);
        model.setMainTree(v);
        view.navigate();
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
    public boolean isDisplayed()
    {
        return model.isDisplayed();
    }

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
     * @see Browser#setRefreshedHierarchy(Map)
     */
    public void setRefreshedHierarchy(Map nodes)
    {
        if (model.getState() != LOADING_DATA)
            throw new IllegalStateException("This method cannot be invoked "+
                "in the LOADING_DATA state.");
        long userID = model.getUserID();
        long groupID = model.getRootGroupID();
        view.setViews(TreeViewerTranslator.refreshHierarchy(nodes, userID,
                groupID), false); 
        model.fireContainerCountLoading();
        model.getParentModel().setStatus(false, "", true);
        fireStateChange(); 
    }
    
}
