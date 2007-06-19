/*
 * org.openmicroscopy.shoola.agents.treeviewer.browser.BrowserUI
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
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.actions.FilterMenuAction;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import pojos.DataObject;
import pojos.ExperimenterData;

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
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter    		sorter;
    
    /** Reference to the listener. */
    private TreeExpansionListener	listener;
    
    /** Reference to the selection listener. */
    private TreeSelectionListener	selectionListener;
    
    /** The component hosting the tree. */
    private JScrollPane             scrollPane;

    /** Collections of nodes whose <code>enabled</code> flag has to be reset. */
    private Set<TreeImageDisplay>	nodesToReset;
    
    /** Button indicating if the partial name is displayed or not. */
    private JToggleButton			partialButton;
    
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
        //menuBar.add(button);
        button = new JButton(controller.getAction(BrowserControl.FORWARD_NAV));
        //menuBar.add(button);
        //menuBar.add(new JSeparator(SwingConstants.VERTICAL));
        ButtonGroup group = new ButtonGroup();
        JToggleButton b = new JToggleButton();
        group.add(b);
        b.setBorderPainted(true);
        b.setSelected(true);
        b.setAction(controller.getAction(BrowserControl.SORT));
        
        menuBar.add(b);
        b = new JToggleButton(controller.getAction(BrowserControl.SORT_DATE));
        
        b.setBorderPainted(true);
        group.add(b);
        menuBar.add(b);
       
        partialButton = new JToggleButton(
        				controller.getAction(BrowserControl.PARTIAL_NAME));
        partialButton.setBorderPainted(true);
        menuBar.add(partialButton);
        
        button = new JButton(controller.getAction(BrowserControl.FILTER_MENU));
        button.addMouseListener((FilterMenuAction) 
                            controller.getAction(BrowserControl.FILTER_MENU));
        button.setBorderPainted(false);
        if (model.getBrowserType() == Browser.IMAGES_EXPLORER)
        	menuBar.add(button);
        menuBar.add(new JSeparator(JSeparator.VERTICAL));
        button = new JButton(controller.getAction(BrowserControl.COLLAPSE));
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
        node.setExpanded(expanded);
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
        JTree tree = getSelectedTree();
        int row = tree.getRowForLocation(p.x, p.y);
        
        if (row != -1) {
            if (me.getClickCount() == 1) {
                model.setClickPoint(p);
                if (me.isPopupTrigger()) controller.showPopupMenu();
                //if (!released) controller.onClick();
            } else if (me.getClickCount() == 2 && released) {
            	//controller.cancel();
                model.viewDataObject();
            }
        }
    }
    
    /**
     * Handles the mouse moved event. Displays the properties fo the
     * the nodes the mouse is over.
     * 
     * @param e	The mouse event to handle.
     */
    private void rollOver(MouseEvent e)
    {
    	if (!model.getParentModel().isRollOver()) return;
    	JTree tree = treeDisplay;
    	TreePath path = treeDisplay.getClosestPathForLocation(
    											e.getX(), e.getY());
        Rectangle bounds = tree.getPathBounds(path);
        if (!bounds.contains(e.getPoint())) return;
        TreeImageDisplay node = (TreeImageDisplay) path.getLastPathComponent();
    	Object uo = node.getUserObject();
    	if (!(uo instanceof DataObject)) return;
    	model.getParentModel().showProperties((DataObject) uo, 
    								TreeViewer.PROPERTIES_EDITOR);
    }
    
    /** 
     * Helper method to create the trees hosting the display. 
     * 
     * @param exp The logged in experimenter.
     */
    private void createTrees(ExperimenterData exp)
    {
        treeDisplay = new JTree();
        treeDisplay.setVisible(true);
        ToolTipManager.sharedInstance().registerComponent(treeDisplay);
        treeDisplay.setCellRenderer(new TreeCellRenderer());
        treeDisplay.setShowsRootHandles(true);
        //treeDisplay.putClientProperty("JTree.lineStyle", "Angled");
        treeDisplay.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        TreeImageSet root = new TreeImageSet("");
        if (exp != null) root.setUserObject(exp);
        DefaultTreeModel treeModel = (DefaultTreeModel) treeDisplay.getModel();
        treeModel.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), root, 
                                root.getChildCount());
        treeDisplay.setModel(new DefaultTreeModel(root));
        treeDisplay.collapsePath(new TreePath(root.getPath()));
        //treeDisplay.setRootVisible(false);
        //Add Listeners
        //treeDisplay.requestFocus();
        treeDisplay.addMouseListener(new MouseAdapter() {
           public void mousePressed(MouseEvent e) { onClick(e, false); }
           public void mouseReleased(MouseEvent e) { onClick(e, true); }
           
          // public void mouseMoved(MouseEvent e) { rollOver(e); }
        });
        treeDisplay.addMouseMotionListener(new MouseMotionAdapter() {
           
            public void mouseMoved(MouseEvent e) { rollOver(e); }
         });
        treeDisplay.addTreeExpansionListener(listener);
        selectionListener = new TreeSelectionListener() {
        
            public void valueChanged(TreeSelectionEvent e)
            {
                controller.onClick();
            }
        };
        treeDisplay.addTreeSelectionListener(selectionListener);
        
        treeDisplay.addKeyListener(new KeyAdapter() {
	
			public void keyPressed(KeyEvent e)
			{
				switch (e.getKeyCode()) {
					case KeyEvent.VK_ENTER:
						ViewCmd cmd = new ViewCmd(model.getParentModel());
					    cmd.execute();
						break;
					case KeyEvent.VK_DELETE:
						switch (model.getState()) {
							case Browser.LOADING_DATA:
							case Browser.LOADING_LEAVES:
							case Browser.COUNTING_ITEMS:  
								break;
							default:
								DeleteCmd c = new DeleteCmd(
												model.getParentModel());
								c.execute();
						}
				}
			}
		});
        //Initialize the goIntoTree
        goIntoTree = new JTree();      
        goIntoTree.setVisible(true);
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
        goIntoTree.addTreeSelectionListener(new TreeSelectionListener() {
            
            public void valueChanged(TreeSelectionEvent e)
            {
                controller.onClick();
            }
        });
    }

    /**
     * Adds the nodes to the specified parent.
     * 
     * @param parent    The parent node.
     * @param nodes     The list of nodes to add.
     * @param tm        The  tree model.
     */
    private void buildTreeNode(TreeImageDisplay parent, 
                                Collection nodes, DefaultTreeModel tm)
    {
        if (nodes.size() == 0) {
            tm.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), 
                    parent, parent.getChildCount());
            return;
        }
        Iterator i = nodes.iterator();
        TreeImageDisplay display;
        Set children;
        parent.removeAllChildren();
        while (i.hasNext()) {
            display = (TreeImageDisplay) i.next();
            tm.insertNodeInto(display, parent, parent.getChildCount());
            if (display instanceof TreeImageSet) {
                children = display.getChildrenDisplay();
                if (children.size() != 0) {
                    if (display.containsImages()) {
                    	display.setExpanded(true);
                    	setExpandedParent(display, false);
                    	nodesToReset.add(display);
                    	buildTreeNode(display, sorter.sort(children), tm);
                        expandNode(display);
                        tm.reload(display);
                    } else {
                    	if (display.isExpanded()) {
                    		setExpandedParent(display, false);
                        	nodesToReset.add(display);
                    	}
                    	buildTreeNode(display, sorter.sort(children), tm);
                    }
                } else {
                    tm.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), 
                        display, display.getChildCount());
                }  
            }
        } 
        if (parent.isExpanded()) {
            expandNode(parent);
            tm.reload(parent);
        }
    }
    
    /**
     * Sets the value of the <code>expanded</code> flag for the parent of 
     * the specified node.
     * 
     * @param n	The node to handle.
     * @param b	The value to set.
     */
    private void setExpandedParent(TreeImageDisplay n, boolean b)
    {
    	TreeImageDisplay p = n.getParentDisplay();
    	if (p != null) {
    		p.setExpanded(b);
    		setExpandedParent(p, b);
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
    private void expandNode(TreeImageDisplay node)
    {
        //First remove listener otherwise an event is fired.
    	node.setExpanded(true);
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
        //goIntoTree.requestFocus();
        renderer.getTreeCellRendererComponent(goIntoTree, node, 
                            goIntoTree.isPathSelected(path),
                                    false, true, 0, false);
    }
    
    /** Navigates into the selected node. */
    private void loadGoIntoTree()
    {
        DefaultTreeModel dtm = (DefaultTreeModel) goIntoTree.getModel();
        DefaultMutableTreeNode r = (DefaultMutableTreeNode) dtm.getRoot();
        TreeImageDisplay d = model.getLastSelectedDisplay();
        r.removeAllChildren();
        TreeImageDisplay copy = d.copy();
        dtm.insertNodeInto(copy, r, r.getChildCount());
        buildTreeNode(copy, sorter.sort(copy.getChildrenDisplay()), dtm);
        dtm.reload(r);
        if (copy.isChildrenLoaded()) expandGoIntoTreeNode(copy);
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
        nodesToReset = new HashSet<TreeImageDisplay>();
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
     * @param controller    The Controller.
     * @param model         The Model.
     * @param exp			The experimenter the tree view is for.
     */
    void initialize(BrowserControl controller, BrowserModel model, 
    						ExperimenterData exp)
    {
        this.controller = controller;
        this.model = model;
        createMenuBar();
        createTrees(exp);
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
     * @param nodes     The collection of nodes to add.
     */
    void setViews(Set nodes)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
        root.removeAllChildren();
        root.setChildrenLoaded(Boolean.TRUE);
        root.setExpanded(true);
        dtm.reload();
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                root.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(root, sorter.sort(nodes), 
                        (DefaultTreeModel) treeDisplay.getModel());
        } else buildEmptyNode(root);
        if (!model.isMainTree()) loadGoIntoTree();
        Iterator j = nodesToReset.iterator();
        while (j.hasNext()) {
			setExpandedParent((TreeImageDisplay) j.next(), true);
		}
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
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                parent.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(parent, sorter.sort(nodes), dtm);
        } else buildEmptyNode(parent);
        dtm.reload(parent);
        if (!model.isMainTree()) loadGoIntoTree();
    }
    
    /**
     * Adds the specifies nodes to the currently selected
     * {@link TreeImageDisplay}.
     * 
     * @param nodes     The collection of nodes to add.
     * @param parent    The parent of the nodes.
     */
    void setLeavesViews(Set nodes, TreeImageSet parent)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        parent.removeAllChildren();
        parent.setChildrenLoaded(Boolean.TRUE);
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                parent.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(parent, sorter.sort(nodes), dtm);
        } else buildEmptyNode(parent);
        dtm.reload(parent);
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
        if (treeDisplay == null) return null;
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        if (dtm == null) return null;
        return (TreeImageDisplay) dtm.getRoot();
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
     * Creates or recycles the {@link FilterMenu} and brings it on screen.
     * 
     * @param c The component invoking the menu.
     * @param p The location of the click.
     */
    void showFilterMenu(Component c, Point p)
    {
        //if (filterMenu == null) {
    	FilterMenu   filterMenu = new FilterMenu(model);
    	filterMenu.addPropertyChangeListener(
    			FilterMenu.FILTER_SELECTED_PROPERTY, controller);
        //}
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
        //treeDisplay.requestFocus();
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
        if (node == null) return;
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
    
    /**
     * Sets the sorted nodes.
     * 
     * @param nodes     The collection of nodes to set.
     * @param parentNode The parent whose children have been sorted.
     */
    void setSortedNodes(List nodes, TreeImageDisplay parentNode)
    {
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        parentNode.removeAllChildren();
        Iterator i = nodes.iterator(); 
        boolean b = (parentNode.equals(dtm.getRoot()));
        TreeImageDisplay child;
        while (i.hasNext()) {
            child = (TreeImageDisplay) i.next();
            if (b) parentNode.addChildDisplay(child);
            dtm.insertNodeInto(child, parentNode, parentNode.getChildCount());
        }
        dtm.reload(parentNode);
        expandNode(parentNode);
        if (!model.isMainTree()) {
            i = nodes.iterator();
            List<TreeImageDisplay> 
            	copies = new ArrayList<TreeImageDisplay>(nodes.size());
            while (i.hasNext()) {
                copies.add(((TreeImageDisplay) i.next()).copy());
            }
            dtm = (DefaultTreeModel) goIntoTree.getModel();
            DefaultMutableTreeNode r = (DefaultMutableTreeNode) dtm.getRoot();
            TreeImageDisplay d = model.getLastSelectedDisplay();
            r.removeAllChildren();
            TreeImageDisplay copy = d.copy();
            dtm.insertNodeInto(copy, r, r.getChildCount());
            buildTreeNode(copy, copies, dtm);
            dtm.reload(r);
            if (copy.isChildrenLoaded()) expandGoIntoTreeNode(copy);
            scrollPane.getViewport().removeAll();
            scrollPane.getViewport().add(goIntoTree);
            repaint();
        }
    }
    
    /**
     * Sorts the nodes in the tree view  according to the specified index.
     * 
     * @param type 	One out of the following constants: 
     * 				{@link  Browser#SORT_NODES_BY_DATE} or 
     * 				{@link  Browser#SORT_NODES_BY_NAME}.
     */
    void sortNodes(int type)
    {
        sorter.setByDate(type == Browser.SORT_NODES_BY_DATE);
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
        Set children = root.getChildrenDisplay();
        root.removeAllChildren();
        dtm.reload(root);
        if (children.size() != 0) {
            buildTreeNode(root, sorter.sort(children), dtm);
        } else buildEmptyNode(root);
        if (!model.isMainTree()) loadGoIntoTree();
        Iterator j = nodesToReset.iterator();
        while (j.hasNext()) {
			setExpandedParent((TreeImageDisplay) j.next(), true);
		}
    }
    
    /** Loads the children of the root node. */
    void loadRoot()
    {
        treeDisplay.expandPath(new TreePath(getTreeRoot().getPath()));
    }

    /** Displays the main tree or navigates into the selected node. */
    void navigate()
    {
        if (model.isMainTree()) {
            scrollPane.getViewport().removeAll();
            scrollPane.getViewport().add(treeDisplay);
            repaint();
        } else {
            loadGoIntoTree();
        } 
    }

    /** 
     * Reacts to state change.
     * 
     * @param b Pass <code>true</code> to enable the trees, <code>false</code>
     *          otherwise.
     */
    void onStateChanged(boolean b)
    {
        model.getParentModel().onComponentStateChange(b);
    }

    /**
     * Enables the components composing the display depending on the specified
     * parameter.
     * 
     * @param b Pass <code>true</code> to enable the component, 
     *          <code>false</code> otherwise.
     */
    void onComponentStateChange(boolean b)
    {
        treeDisplay.setEnabled(b);
        goIntoTree.setEnabled(b);
    }

    /** Resets the UI so that we have no node selected in trees. */
    void setNullSelectedNode()
    {
        if (getTreeRoot() != null) {
            treeDisplay.setSelectionRow(-1);
            goIntoTree.setSelectionRow(-1);
        }
    }
    
    /** 
     * Returns <code>true</code> if the partial name is displayed, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isPartialName() { return !partialButton.isSelected(); }
    
    /**
     * Removes the collection of <code>TreePath</code>s from the main tree.
     * We first need to remove the <code>TreeSelectionListener</code> to avoid 
     * loop.
     * 
     * @param paths Collection of paths to be removed.
     */
    void removeTreePaths(List paths)
    {
    	treeDisplay.removeTreeSelectionListener(selectionListener);
    	Iterator j = paths.iterator();
        while (j.hasNext()) 
        	treeDisplay.removeSelectionPath((TreePath) j.next());

        treeDisplay.addTreeSelectionListener(selectionListener);
    }
    
    /**
     * Sets the user object of the root node.
     * 
     * @param experimenter The user object to set.
     */
    void setRootNode(ExperimenterData experimenter)
    {
    	if (experimenter == null)
    		getTreeRoot().setUserObject("");
    	else getTreeRoot().setUserObject(experimenter);
    }
    
}
