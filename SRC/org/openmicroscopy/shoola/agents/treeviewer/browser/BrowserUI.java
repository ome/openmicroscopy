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

/** 
 * 
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
    private JTree           treeDisplay;
    
    /** The toolBar. */
    private JToolBar        menuBar;
    
    /** The Controller. */
    private BrowserControl  controller;
    
    /** The model. */
    private BrowserModel    model;
    
    /** The popup menu. */
    private FilterMenu      filterMenu;
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter    sorter;
    
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
        add(new JScrollPane(treeDisplay), BorderLayout.CENTER);
    }
    
    /** Helper method to create the menu bar. */
    private void createMenuBar()
    {
        menuBar = new JToolBar();
        menuBar.setBorder(null);
        menuBar.setRollover(true);
        menuBar.setFloatable(false);
        JButton button = new JButton(controller.getAction(Browser.SORT));
        menuBar.add(button);
        button = new JButton(controller.getAction(Browser.SORT_DATE));
        menuBar.add(button);
        button = new JButton(controller.getAction(Browser.FILTER_MENU));
        button.addMouseListener((FilterMenuAction) 
                                controller.getAction(Browser.FILTER_MENU));
        menuBar.add(button);
        menuBar.add(new JSeparator(SwingConstants.VERTICAL));
        button = new JButton(controller.getAction(Browser.COLLAPSE));
        menuBar.add(button);
        button = new JButton(controller.getAction(Browser.CLOSE));
        menuBar.add(button);
    }
    
    /** 
     * Reacts to node expansion event.
     * 
     * @param tee The event to handle.
     */
    private void onNodeNavigation(TreeExpansionEvent tee)
    {
        TreePath path = tee.getPath();
        TreeImageDisplay node = (TreeImageDisplay) 
                                        path.getLastPathComponent();
        controller.onNodeNavigation(node);
    }
    
    /**
     * Reacts to mouse pressed and mouse release event.
     * 
     * @param me The event to handle.
     */
    private void onClick(MouseEvent me)
    {
        Point p = me.getPoint();
        int row = treeDisplay.getRowForLocation(p.x, p.y);
        if (row != -1) {
            treeDisplay.setSelectionRow(row);
            model.setClickPoint(p);
            controller.onClick(me.isPopupTrigger());
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
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeImageSet root = new TreeImageSet(getBrowserTitle());
        DefaultTreeModel treeModel = (DefaultTreeModel) treeDisplay.getModel();
        treeModel.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), root, 
                                root.getChildCount());
        treeDisplay.setModel(new DefaultTreeModel(root));
        treeDisplay.collapsePath(new TreePath(root.getPath()));
        
        //Add Listeners
        treeDisplay.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
        treeDisplay.addTreeExpansionListener(new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                onNodeNavigation(e);
            }
            public void treeExpanded(TreeExpansionEvent e) {
                onNodeNavigation(e);  
            }   
        });
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
                else 
                    tm.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), 
                                   display, display.getChildCount());
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
     * Creates a new instance.
     * The {@link #initialize(BrowserControl, BrowserModel) initialize} method
     * should be called straigh after to link this View to the Controller.
     */
    BrowserUI()
    {
        sorter = new ViewerSorter();
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
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                root.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(root, sorter.sort(nodes));
        }
        else buildEmptyNode(root);
        dtm.reload();
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
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                parent.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(parent, sorter.sort(nodes));
        }
        else buildEmptyNode(parent);
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        dtm.reload(parent);
    }
    
    void setCreatedNode(TreeImageDisplay node, TreeImageDisplay parent)
    {
        parent.addChildDisplay(node);
        Set children = parent.getChildrenDisplay();
        parent.removeAllChildren();
        buildTreeNode(parent, sorter.sort(children));
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        dtm.reload(parent);
    }
    
    /**
     * Adds the specifies nodes to the currently selected
     * {@link TreeImageDisplay}.
     * 
     * @param nodes The collection of nodes to add.
     */
    void setLeavesViews(Set nodes)
    {
        TreeImageDisplay node = model.getSelectedDisplay();
        if (node instanceof TreeImageNode) return;
        node.removeAllChildren();
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                node.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(node, sorter.sort(nodes));
        }
        else buildEmptyNode(node);
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        dtm.reload(node);
    }
    
    /** Collapses the tree when a data retrieval is cancelled. */
    void cancelDataLoading()
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
        treeDisplay.collapsePath(new TreePath(root.getPath()));
    }
    
    /** Collapses the node when a data retrieval is cancelled. */
    void cancelLeavesLoading()
    {
        TreeImageDisplay node = model.getSelectedDisplay();
        treeDisplay.collapsePath(new TreePath(node.getPath()));
    }
    
    /**
     * Collapses the specified node.
     * 
     * @param node The node.
     */
    void collapse(TreeImageDisplay node)
    {
        treeDisplay.collapsePath(new TreePath(node.getPath()));
    }  
    
    /**
     * Returns the tree hosting the display.
     * 
     * @return See above.
     */
    JTree getTreeDisplay() { return treeDisplay; }
    
    /**
     * Returns the title of the Browser according to the type.
     * 
     * @return See above.
     */
    String getBrowserTitle()
    {
        switch (model.getBrowserType()) {
            case Browser.HIERARCHY_EXPLORER:
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
    }
    
    /**
     * Creates or recycles the {@link FilterMenu} and brings it on screen.
     * 
     * @param c The component invoking the menu.
     * @param p The location of the click.
     */
    void showFilterMenu(Component c, Point p)
    {
        if (filterMenu == null) filterMenu = new FilterMenu(controller);
        filterMenu.show(c, p.x, p.y);
    }
    
}
