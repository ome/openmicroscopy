/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserUI
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.actions.FilterMenuAction;
import org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;
import pojos.DataObject;

/** 
 * The Browser's View.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class BrowserUI
    extends JPanel
{
    
    /** The text of the dummy default node. */
    private static final String     LOADING_MSG = "Loading...";
    
    /** 
     * The text of the node added to a {@link TreeImageSet} node
     * containing no element.
     */
    private static final String     EMPTY_MSG = "Empty";
    
    /** The tree hosting the display. */
    private JTree           		treeDisplay;
    
    /** The tree displaying one selected node. */
    private JTree                   goIntoTree;
    
    /** The tool bar hosting the controls. */
    private JToolBar				menuBar;
    
    /** The Controller. */
    private BrowserControl  		controller;
    
    /** The model. */
    private BrowserModel    		model;
    
    /** The popup menu. */
    private FilterMenu      		filterMenu;
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter    		sorter;
    
    /** Reference to the listener. */
    private TreeExpansionListener	listener;
    
    /** The component hosting the tree. */
    private JScrollPane             scrollPane;
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setLayout(new BorderLayout(0, 0));
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.setBorder(null);
        p.add(menuBar);
        p.setPreferredSize(menuBar.getPreferredSize());
        add(p, BorderLayout.NORTH);
        scrollPane = new JScrollPane(treeDisplay);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /** Helper method to create the menu bar. */
    private void createMenuBar()
    {
        menuBar = new JToolBar();
        menuBar.setBorder(null);
        menuBar.setRollover(true);
        menuBar.setFloatable(false);
        JButton button = new JButton(
                controller.getAction(BrowserControl.BACKWARD_NAV));
        button.setBorderPainted(false);
        menuBar.add(button);
        //button = new JButton(
        //        controller.getAction(BrowserControl.FORWARD_NAV));
        //menuBar.add(button);
        menuBar.add(new JSeparator(SwingConstants.VERTICAL));
        button = new JButton(controller.getAction(BrowserControl.SORT));
        button.setBorderPainted(false);
        menuBar.add(button);
        button = new JButton(controller.getAction(BrowserControl.SORT_DATE));
        button.setBorderPainted(false);
        menuBar.add(button);
        button = new JButton(controller.getAction(BrowserControl.FILTER_MENU));
        button.addMouseListener((FilterMenuAction) 
                            controller.getAction(BrowserControl.FILTER_MENU));
        button.setBorderPainted(false);
        menuBar.add(button);
        menuBar.add(new JSeparator(SwingConstants.VERTICAL));
        button = new JButton(controller.getAction(BrowserControl.COLLAPSE));
        button.setBorderPainted(false);
        menuBar.add(button);
        button = new JButton(controller.getAction(BrowserControl.CLOSE));
        button.setBorderPainted(false);
        menuBar.add(button);
    }

    /** 
     * Reacts to node expansion event.
     * 
     * @param tee       The event to handle.
     * @param expanded 	Pass <code>true</code> is the node is expanded,
     * 					<code>false</code> otherwise.
     */
    private void onNodeNavigation(TreeExpansionEvent tee, boolean expanded)
    {
        TreeImageDisplay node = (TreeImageDisplay) 
        							tee.getPath().getLastPathComponent();
        controller.onNodeNavigation(node, expanded);
    }
    
    /**
     * Reacts to mouse pressed and mouse release event.
     * 
     * @param me        The event to handle.
     * @param released  Pass <code>true</code> if the method is invoked when
     *                  the mouse is released, <code>false</code> otherwise.
     */
    private void onClick(MouseEvent me, boolean released)
    {
        Point p = me.getPoint();
        int row = treeDisplay.getRowForLocation(p.x, p.y);
        if (row != -1) {
            //treeDisplay.setSelectionRow(row);
            model.setClickPoint(p);
            if (me.getClickCount() == 1) {
                if (me.isPopupTrigger()) controller.showPopupMenu();
                if (!released) controller.onClick();
            } else if (me.getClickCount() == 2) {
                model.viewDataObject();
            }
        }
    }
    
    /** Helper method to create the tree hosting the display. */
    private void createTree()
    {
        treeDisplay = new JTree();
        ToolTipManager.sharedInstance().registerComponent(treeDisplay);
        treeDisplay.setCellRenderer(new TreeCellRenderer());
        treeDisplay.setShowsRootHandles(true);
        treeDisplay.putClientProperty("JTree.lineStyle", "Angled");
        treeDisplay.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        //TreeImageSet root = new TreeImageSet(getBrowserTitle());
        TreeImageSet root = new TreeImageSet("");
        DefaultTreeModel treeModel = (DefaultTreeModel) treeDisplay.getModel();
        treeModel.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), root, 
                                root.getChildCount());
        treeDisplay.setModel(new DefaultTreeModel(root));
        treeDisplay.collapsePath(new TreePath(root.getPath()));
        treeDisplay.setRootVisible(false);
        //Add Listeners
        treeDisplay.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e, false); }
            public void mouseReleased(MouseEvent e) { onClick(e, true); }
        });
        treeDisplay.addTreeExpansionListener(listener);
                
        //Initialize the goIntoTree
        goIntoTree = new JTree();       
        ToolTipManager.sharedInstance().registerComponent(goIntoTree);
        goIntoTree.setCellRenderer(new TreeCellRenderer());
        goIntoTree.setShowsRootHandles(true);
        goIntoTree.putClientProperty("JTree.lineStyle", "Angled");
        goIntoTree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        TreeImageSet r = new TreeImageSet("");
        treeModel = (DefaultTreeModel) goIntoTree.getModel();
        goIntoTree.setModel(new DefaultTreeModel(r));
        goIntoTree.expandPath(new TreePath(r.getPath()));
        goIntoTree.setRootVisible(false);
        goIntoTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e, false); }
            public void mouseReleased(MouseEvent e) { onClick(e, true); }
        });
        goIntoTree.addTreeExpansionListener(listener);
    }
    
    /**
     * Adds the nodes to the specified parent.
     * 
     * @param parent The parent node.
     * @param nodes The list of nodes to add.
     */
    private void buildTreeNode(DefaultMutableTreeNode parent, List nodes)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
        Iterator i = nodes.iterator();
        TreeImageDisplay display;
        Set children;
        while (i.hasNext()) {
            display = (TreeImageDisplay) i.next();
            tm.insertNodeInto(display, parent, parent.getChildCount());
            if (display instanceof TreeImageSet) {
                children = display.getChildrenDisplay();
                if (children.size() != 0)
                    buildTreeNode(display, sorter.sort(children));
                else {
                    tm.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), 
                        display, display.getChildCount());
                }    
            }
        } 
    }
    
    /**
     * Adds a dummy node to the specified node.
     * 
     * @param node The parent node.
     */
    private void buildEmptyNode(DefaultMutableTreeNode node)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
        tm.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), node,
                            node.getChildCount());
    }
    
    /**
     * Expands the specified node. To avoid loop, we first need to 
     * remove the <code>TreeExpansionListener</code>.
     * 
     * @param node The node to expand.
     */
    private void expandNode(DefaultMutableTreeNode node)
    {
        //First remove listener otherwise an event is fired.
        treeDisplay.removeTreeExpansionListener(listener);
        treeDisplay.expandPath(new TreePath(node.getPath()));
        treeDisplay.addTreeExpansionListener(listener);
    }
    
    /**
     * Expands the specified node. To avoid loop, we first need to 
     * remove the <code>TreeExpansionListener</code>.
     * 
     * @param node The node to expand.
     */
    private void expandGoIntoTreeNode(DefaultMutableTreeNode node)
    {
        //First remove listener otherwise an event is fired.
        goIntoTree.removeTreeExpansionListener(listener);
        goIntoTree.expandPath(new TreePath(node.getPath()));
        goIntoTree.addTreeExpansionListener(listener);
    }
    
    /**
     * Selects the specified node.
     * 
     * @param node The node to select.
     */
    private void selectGoIntoTreeFoundNode(TreeImageDisplay node) 
    {
        TreePath path = new TreePath(node.getPath());
        goIntoTree.setSelectionPath(path);
        TreeCellRenderer renderer = (TreeCellRenderer) 
                    goIntoTree.getCellRenderer();
        goIntoTree.requestFocus();
        renderer.getTreeCellRendererComponent(goIntoTree, node, 
                            goIntoTree.isPathSelected(path),
                                    false, true, 0, false);
    }
    
    /** Navigates into the selected node. */
    private void loadGoIntoTree()
    {
        DefaultTreeModel dtm = (DefaultTreeModel) goIntoTree.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
        Object path = treeDisplay.getLastSelectedPathComponent();
        if (path == null) return;
        root.removeAllChildren();
        dtm.insertNodeInto((TreeImageDisplay) path, root, root.getChildCount());
        dtm.reload();
        expandGoIntoTreeNode((TreeImageDisplay) path);
        scrollPane.getViewport().removeAll();
        scrollPane.getViewport().add(goIntoTree);
        repaint();
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserControl, BrowserModel) initialize} method
     * should be called straight after to link this View to the Controller.
     */
    BrowserUI()
    {
        sorter = new ViewerSorter();
        listener = new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                onNodeNavigation(e, false);
            }
            public void treeExpanded(TreeExpansionEvent e) {
                onNodeNavigation(e, true);  
            }   
        };
    }
    
    /**
     * Links this View to its Controller and its Model.
     * 
     * @param controller The Controller.
     * @param model		The Model
     */
    void initialize(BrowserControl controller, BrowserModel model)
    {
        this.controller = controller;
        this.model = model;
        createMenuBar();
        createTree();
        buildGUI();
    }

    /**
     * Creates a dummy loading node whose parent is the specified node.
     * 
     * @param parent The parent node.
     */
    void loadAction(TreeImageDisplay parent)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
        parent.removeAllChildren();
        tm.insertNodeInto(new DefaultMutableTreeNode(LOADING_MSG), parent,
                			parent.getChildCount());
        tm.reload(parent);
    }
    
    /**
     * Displays the specified nodes in the tree.
     * 
     * @param nodes The collection of nodes to add.
     */
    void setViews(Set nodes)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
        root.removeAllChildren();
        root.setChildrenLoaded(Boolean.TRUE);
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                root.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(root, sorter.sort(nodes));
        } else buildEmptyNode(root);
        dtm.reload();
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /**
     * Adds the specified nodes to the specified parent display. 
     * 
     * @param nodes     The collection of nodes to add.
     * @param parent    The parent node.
     */
    void setViews(Set nodes, TreeImageDisplay parent)
    {
        parent.removeAllChildren();
        parent.setChildrenLoaded(Boolean.TRUE);
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                parent.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(parent, sorter.sort(nodes));
        }
        else buildEmptyNode(parent);
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        dtm.reload(parent);
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /**
     * Adds the specifies nodes to the currently selected
     * {@link TreeImageDisplay}.
     * 
     * @param nodes The collection of nodes to add.
     */
    void setLeavesViews(Set nodes)
    {
        TreeImageDisplay node = model.getLastSelectedDisplay();
        if (node instanceof TreeImageNode) return;
        node.removeAllChildren();
        node.setChildrenLoaded(Boolean.TRUE);
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                node.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(node, sorter.sort(nodes));
        } else buildEmptyNode(node);
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        dtm.reload(node);
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /**
     * Returns the tree hosting the display.
     * 
     * @return See above.
     */
    JTree getTreeDisplay() { return treeDisplay; }
    
    /**
     * Returns the tree currently displayed.
     * 
     * @return See above.
     */
    JTree getSelectedTree()
    {
       if (model.isMainTree()) return treeDisplay;
       return goIntoTree;
    }
    
    /**
     * Returns the root node of the tree.
     * 
     * @return See above.
     */
    TreeImageDisplay getTreeRoot()
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        return(TreeImageDisplay) dtm.getRoot();
    }
    
    /**
     * Returns the title of the Browser according to the type.
     * 
     * @return See above.
     */
    String getBrowserTitle()
    {
        switch (model.getBrowserType()) {
            case Browser.PROJECT_EXPLORER:
                return Browser.HIERARCHY_TITLE;
            case Browser.CATEGORY_EXPLORER:
                return Browser.CATEGORY_TITLE;
            case Browser.IMAGES_EXPLORER:
                return Browser.IMAGES_TITLE;
        }
        return "";
    }

    /**
     * Sets the sorted nodes.
     * 
     * @param nodes The collection of nodes to set.
     */
    void setSortedNodes(List nodes)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
        root.removeAllChildren();
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                root.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(root, nodes);
        }
        else buildEmptyNode(root);
        dtm.reload();
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /**
     * Creates or recycles the {@link FilterMenu} and brings it on screen.
     * 
     * @param c The component invoking the menu.
     * @param p The location of the click.
     */
    void showFilterMenu(Component c, Point p)
    {
        if (filterMenu == null) {
            filterMenu = new FilterMenu(model);
            filterMenu.addPropertyChangeListener(
                    FilterMenu.FILTER_SELECTED_PROPERTY, controller);
        }
        filterMenu.show(c, p.x, p.y);
    }
    
    /**
     * Selects the specified node.
     * 
     * @param node The node to select.
     */
    void selectFoundNode(TreeImageDisplay node)
    {
        TreePath path = new TreePath(node.getPath());
        treeDisplay.setSelectionPath(path);
        TreeCellRenderer renderer = (TreeCellRenderer) 
        			treeDisplay.getCellRenderer();
        treeDisplay.requestFocus();
        renderer.getTreeCellRendererComponent(treeDisplay, node, 
                					treeDisplay.isPathSelected(path),
                					false, true, 0, false);
        if (!model.isMainTree()) {
            loadGoIntoTree();
            selectGoIntoTreeFoundNode(node);
        }
    }
    
    /** Removes all the nodes from the tree, excepted the root node. */
    void clearTree()
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
        root.removeAllChildren();
        root.removeAllChildrenDisplay();
        buildEmptyNode(root);
        dtm.reload();
        collapsePath(root);
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /**
     * Collapses the specified node. To avoid loop, we first need to 
     * remove the <code>TreeExpansionListener</code>.
     * 
     * @param node The node to collapse.
     */
    void collapsePath(DefaultMutableTreeNode node)
    {
        //First remove listener otherwise an event is fired.
        treeDisplay.removeTreeExpansionListener(listener);
        treeDisplay.collapsePath(new TreePath(node.getPath()));
        treeDisplay.addTreeExpansionListener(listener);
    }
    
    /** 
     * Collapses the node when an on-going data loading is cancelled.
     * 
     * @param node The node to collapse.
     */
    void cancel(DefaultMutableTreeNode node)
    {
        if (node.getChildCount() <= 1) {
            if (node.getUserObject() instanceof String) {
                node.removeAllChildren(); 
                buildEmptyNode(node);
            }
        }
        //in this order otherwise the node is not collapsed.
        ((DefaultTreeModel) treeDisplay.getModel()).reload(node);
        collapsePath(node);
    }
    
    /**
     * Update the specified set of nodes.
     * 
     * @param nodes The collection of nodes to update.
     * @param object The <code>DataObject</code> to update.
     */
    void updateNodes(List nodes, DataObject object)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        Iterator i = nodes.iterator(); 
        TreeImageDisplay node;
        while (i.hasNext()) {
            node = (TreeImageDisplay) i.next();
            node.setUserObject(object);
            dtm.nodeChanged(node);
        }
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /**
     * Removes the specified set of nodes from the tree.
     * 
     * @param nodes         The collection of nodes to remove.
     * @param parentDisplay The selected parent.
     */
    void removeNodes(List nodes, TreeImageDisplay parentDisplay)
    {
        if (parentDisplay == null) parentDisplay = getTreeRoot();
        Iterator i = nodes.iterator(); 
        TreeImageDisplay node;
        TreeImageDisplay parent;
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        while (i.hasNext()) {
            node = (TreeImageDisplay) i.next();
            parent = node.getParentDisplay();
            if (parent.isChildrenLoaded()) {
                parent.removeChildDisplay(node);
                parent.remove(node);
                dtm.reload(parent);
                if (parent.equals(parentDisplay))
                    treeDisplay.setSelectionPath(
                            new TreePath(parent.getPath()));
            }
        }
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /**
     * Adds the newly created node to the tree.
     * 
     * @param nodes         The collection of the parent nodes.
     * @param newNode       The node to add to the parent.
     * @param parentDisplay The selected parent.
     */
    void createNodes(List nodes, TreeImageDisplay newNode, 
                    TreeImageDisplay parentDisplay)
    {
        if (parentDisplay == null) parentDisplay = getTreeRoot();
        Iterator i = nodes.iterator();
        TreeImageDisplay parent;
        List list;
        Iterator j;
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        //buildEmptyNode(newNode);
        boolean toLoad = false;
        while (i.hasNext()) {
            parent = (TreeImageDisplay) i.next();
            //problem will come when we have images
            if (parent.isChildrenLoaded()) {
                parent.addChildDisplay(newNode); 
                list = sorter.sort(parent.getChildrenDisplay());
                parent.removeAllChildren();
                j = list.iterator();
                while (j.hasNext())
                    dtm.insertNodeInto((TreeImageDisplay) j.next(), parent,
                                    parent.getChildCount());
                dtm.reload(parent);
                expandNode(parent);
                if (parent.equals(parentDisplay))
                    treeDisplay.setSelectionPath(
                            new TreePath(newNode.getPath()));
            } else { //Only the currently selected one will be loaded.
                if (parent.equals(parentDisplay)) toLoad = true;
            }
        }
        //should be leaves. Need to review that code.
        if (toLoad) {
            if (parentDisplay.getParentDisplay() == null) //root
                controller.loadData();
            else controller.loadLeaves();
        }
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /** Loads the children of the root node. */
    void loadRoot()
    {
        treeDisplay.expandPath(new TreePath(getTreeRoot().getPath()));
    }

    /** Displays the main tree or navigates into the selected node. 
     * 
     * @param previous Indicates which element tree was displayed.
     */
    void navigate(boolean previous)
    {
        if (model.isMainTree()) {
            scrollPane.getViewport().removeAll();
            scrollPane.getViewport().add(treeDisplay);
            repaint();
            return;
        }
        DefaultTreeModel dtm = (DefaultTreeModel) goIntoTree.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
        Object path = treeDisplay.getLastSelectedPathComponent();
        if (!previous) //
            path = goIntoTree.getLastSelectedPathComponent();
        if (path == null) return;
        root.removeAllChildren();
        dtm.insertNodeInto((TreeImageDisplay) path, root, root.getChildCount());
        dtm.reload(root);
        //expandGoIntoTreeNode((TreeImageDisplay) path);
        scrollPane.getViewport().removeAll();
        scrollPane.getViewport().add(goIntoTree);
        repaint();
    }
    
}
