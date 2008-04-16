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
import java.util.Map;
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
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.util.TreeCellRenderer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.ImageData;

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
    
    /**
     * Handles the mouse pressed and released.
     * 
     * @param loc			The location of the mouse click.
     * @param popupTrigger	Pass <code>true</code> if the mouse event is the 
     * 						popup menu trigger event for the platform,
     * 						<code>false</code> otherwise.
     */
    private void handleMouseClick(Point loc, boolean popupTrigger)
    {
    	if (treeDisplay.getRowForLocation(loc.x, loc.y) == -1 && popupTrigger) {
    		model.setClickPoint(loc);
    		controller.showPopupMenu(TreeViewer.PARTIAL_POP_UP_MENU);
		}
    }
    
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
        treeDisplay.addMouseListener(new MouseAdapter() {
    		
        	/**
        	 * Pops up a menu if the mouse click occurs on the tree
        	 * but not on the a node composing the tree
        	 * @see MouseAdapter#mousePressed(MouseEvent)
        	 */
			public void mousePressed(MouseEvent e)
			{
				handleMouseClick(e.getPoint(), e.isPopupTrigger());
			}
		
			/**
        	 * Pops up a menu if the mouse click occurs on the tree
        	 * but not on the a node composing the tree
        	 * @see MouseAdapter#mouseReleased(MouseEvent)
        	 */
			public void mouseReleased(MouseEvent e)
			{
				handleMouseClick(e.getPoint(), e.isPopupTrigger());
			}
		});
    }
    
    /** Helper method to create the menu bar. */
    private void createMenuBar()
    {
        menuBar = new JToolBar();
        menuBar.setBorder(null);
        menuBar.setRollover(true);
        menuBar.setFloatable(false);
       
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
        menuBar.add(new JSeparator(JSeparator.VERTICAL));
        JButton button = new JButton(
        			controller.getAction(BrowserControl.COLLAPSE));
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
        int row = treeDisplay.getRowForLocation(p.x, p.y);
        
        if (row != -1) {
            if (me.getClickCount() == 1) {
                model.setClickPoint(p);
                if (me.isPopupTrigger()) 
                	controller.showPopupMenu(TreeViewer.FULL_POP_UP_MENU);
                //if (!released) controller.onClick();
            } else if (me.getClickCount() == 2 && released) {
            	//controller.cancel();
                //model.viewDataObject();
            	TreeImageDisplay d  = model.getLastSelectedDisplay();
                if (d == null) return;
                Object o = d.getUserObject();
                if (o instanceof ImageData) {
                	Rectangle r = model.getParentModel().getUI().getBounds();
        			TreeViewerAgent.getRegistry().getEventBus().post(
        					new ViewImage((ImageData) o, r));
                }
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
    	//model.getParentModel().showProperties((DataObject) uo, 
    	//							TreeViewer.PROPERTIES_EDITOR);
    }
    
    /**
     * Creates an experimenter node hosting the passed experimenter.
     * 
     * @param exp	The experimenter to add.
     * @return See above.
     */
    private TreeImageSet createExperimenterNode(ExperimenterData exp)
    {
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeImageSet node = new TreeImageSet(exp);
    	if (model.getBrowserType() == Browser.IMAGES_EXPLORER) {
    		createTimeElements(node);
    	} else buildEmptyNode(node);
    	TreeImageDisplay root = getTreeRoot();
    	root.addChildDisplay(node);
    	tm.insertNodeInto(node, root, root.getChildCount());
    	return node;
    }
    
    /**
     * Creates the smart folders added to the passed node.
     * 
     * @param parent	The parent of the smart folder.
     */
    private void createTimeElements(TreeImageSet parent)
    {
    	createTimeNode(TreeImageTimeSet.WEEK, parent, true);
		createTimeNode(TreeImageTimeSet.TWO_WEEK, parent, true);
		TreeImageSet n = createTimeNode(TreeImageTimeSet.YEAR, parent, false);
    	int month = TreeImageTimeSet.getCurrentMonth()+1;
    	for (int i = 0; i < month; i++) 
    		createTimeNode(TreeImageTimeSet.YEAR, i, n);
    	n.setNumberItems(-1);
    	n = createTimeNode(TreeImageTimeSet.YEAR_BEFORE, parent, false);
    	for (int i = 0; i < 12; i++) 
    		createTimeNode(TreeImageTimeSet.YEAR_BEFORE, i, n);
    	n.setNumberItems(-1);
    	createTimeNode(TreeImageTimeSet.OTHER, parent, true);
    	//parent.setChildrenLoaded(true);
    }
    
    /**
     * Creates and returns a {@link TreeImageTimeSet}.
     * 
     * @param index 	One of the following constants: 
     * 					{@link TreeImageTimeSet#YEAR} or 
     * 					{@link TreeImageTimeSet#WEEK}
     * @param parent	The parent of the new node.
     * @param empty		Pass <code>true</code> to add an empty node,
     * 					<code>false</code> otherwise.
     * @return See above.
     */
    private TreeImageTimeSet createTimeNode(int index, TreeImageSet parent, 
    										boolean empty)
    {
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeImageTimeSet date = new TreeImageTimeSet(index);
    	if (empty) buildEmptyNode(date);
    	parent.addChildDisplay(date);
    	tm.insertNodeInto(date, parent, parent.getChildCount());
    	return date;
    }
    
    /**
     * Creates and returns a {@link TreeImageTimeSet}.
     * 
     * @param index 	One of the following constants: 
     * 					{@link TreeImageTimeSet#YEAR} or 
     * 					{@link TreeImageTimeSet#YEAR_BEFORE}.
     * @param month		The index of the month.
     * @param parent	The parent of the new node.
     * @return See above.
     */
    private TreeImageTimeSet createTimeNode(int index, int month, 
    										TreeImageSet parent)
    {
    	DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
    	TreeImageTimeSet date = new TreeImageTimeSet(index, month);
    	buildEmptyNode(date);
    	parent.addChildDisplay(date);
    	tm.insertNodeInto(date, parent, parent.getChildCount());
    	parent.setChildrenLoaded(true);
    	return date;
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
        treeDisplay.setRootVisible(false);
        ToolTipManager.sharedInstance().registerComponent(treeDisplay);
        treeDisplay.setCellRenderer(new TreeCellRenderer());
        treeDisplay.setShowsRootHandles(true);
        //treeDisplay.putClientProperty("JTree.lineStyle", "Angled");
        treeDisplay.getSelectionModel().setSelectionMode(
                TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        TreeImageSet root = new TreeImageSet("");
        /*
        if (exp != null) root.setUserObject(exp);
        DefaultTreeModel treeModel = (DefaultTreeModel) treeDisplay.getModel();
        treeModel.insertNodeInto(new DefaultMutableTreeNode(EMPTY_MSG), root, 
                                root.getChildCount());
        treeDisplay.setModel(new DefaultTreeModel(root));
        treeDisplay.collapsePath(new TreePath(root.getPath()));
        */
        //NEW
        treeDisplay.setModel(new DefaultTreeModel(root));
        TreeImageSet node = createExperimenterNode(exp);
        treeDisplay.collapsePath(new TreePath(node.getPath()));
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
							//case Browser.COUNTING_ITEMS:  
								break;
							default:
								DeleteCmd c = new DeleteCmd(
												model.getParentModel());
								c.execute();
						}
				}
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
        List children;
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
                    		setExpandedParent(display, true);
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
     * Sorts the children of the passed node.
     * 
     * @param node The node to handle.
     */
    private void sortNode(TreeImageTimeSet node)
    {
    	DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
    	List children = node.getChildrenDisplay();
    	Iterator j;
    	if (node.containsImages()) {
    		node.removeAllChildren();
        	dtm.reload(node);
        	if (children.size() != 0) {
        		buildTreeNode(node, sorter.sort(children), dtm);
        	} else buildEmptyNode(node);
        	j = nodesToReset.iterator();
        	while (j.hasNext()) {
        		setExpandedParent((TreeImageDisplay) j.next(), true);
        	}
    	} else {
    		if (children.size() != 0) {
    			j = children.iterator();
    			while (j.hasNext())
					sortNode((TreeImageTimeSet) j.next());
        	} else buildEmptyNode(node);
    	}
    }
    
    /**
     * Refreshes the passed time node.
     * 
     * @param node		The node to refresh.
     * @param elements	The elements to add.
     */
    private void refreshTimeNode(TreeImageSet node, Set elements)
	{
		node.removeAllChildren();
		node.removeAllChildrenDisplay();
		Iterator k = elements.iterator();
		TreeImageDisplay child;
		while (k.hasNext()) {
			child = (TreeImageDisplay) k.next();
			node.addChildDisplay(child);
		}

		buildTreeNode(node, sorter.sort(elements), 
                (DefaultTreeModel) treeDisplay.getModel());
		node.setExpanded(true);
		expandNode(node);
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
    	if (controller == null)
    		throw new IllegalArgumentException("Controller cannot be null");
    	if (model == null)
    		throw new IllegalArgumentException("Model cannot be null");
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
     * Returns the tree hosting the display.
     * 
     * @return See above.
     */
    JTree getTreeDisplay() { return treeDisplay; }
    
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
            case Browser.TAGS_EXPLORER:
                return Browser.TAGS_TITLE;
        }
        return "";
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
        TreeImageDisplay n;
        while (i.hasNext()) {
            parent = (TreeImageDisplay) i.next();
            //problem will come when we have images
            if (parent.isChildrenLoaded()) {
                parent.addChildDisplay(newNode); 
                list = sorter.sort(parent.getChildrenDisplay());
                parent.removeAllChildren();
                j = list.iterator();
                while (j.hasNext()) {
                	n = (TreeImageDisplay) j.next();
                	buildEmptyNode(n);
                	dtm.insertNodeInto(n, parent, parent.getChildCount());
                }
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
        if (toLoad) { //TO BE MODIFIED
            //if (parentDisplay.getParentDisplay() == null) //root
            //    controller.loadData();
            //else controller.loadLeaves();
        }
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
    	boolean b = type == Browser.SORT_NODES_BY_DATE;
        sorter.setByDate(b);
        sorter.setAscending(!b);
        DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
        TreeImageDisplay root = (TreeImageDisplay) dtm.getRoot();
    	int n = root.getChildCount();
    	TreeImageDisplay node;
    	List children;
    	Iterator j;
        switch (model.getBrowserType()) {
			case Browser.IMAGES_EXPLORER:
				for (int i = 0; i < n; i++) {
					node = (TreeImageDisplay) root.getChildAt(i);
					children = node.getChildrenDisplay();
					j = children.iterator();
					while (j.hasNext()) 
						sortNode((TreeImageTimeSet) j.next());
				}	       
				break;
			default:
				for (int i = 0; i < n; i++) {
					node = (TreeImageDisplay) root.getChildAt(i);
					children = node.getChildrenDisplay();
					node.removeAllChildren();
					dtm.reload(node);
					if (children.size() != 0) {
						buildTreeNode(node, sorter.sort(children), dtm);
					} else buildEmptyNode(node);
					j = nodesToReset.iterator();
					while (j.hasNext()) {
						setExpandedParent((TreeImageDisplay) j.next(), true);
					}
				}	        	
		}
    }
    
    /** Loads the children of the root node. */
    void loadRoot()
    {
        treeDisplay.expandPath(new TreePath(getTreeRoot().getPath()));
    }
    
    /** Loads the children of the currently logged in experimenter. */
    void loadExperimenterData()
    {
    	TreeImageDisplay root = getTreeRoot();
    	TreeImageDisplay child = (TreeImageDisplay) root.getFirstChild();
        treeDisplay.expandPath(new TreePath(child.getPath()));
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
    }

    /** Resets the UI so that we have no node selected in trees. */
    void setNullSelectedNode()
    {
        if (getTreeRoot() != null) {
            treeDisplay.setSelectionRow(-1);
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
     * Adds the experimenter's data to the passed node.
     * 
     * @param nodes		The data to add.
     * @param expNode	The selected experimenter node.
     */
	void setExperimenterData(Set nodes, TreeImageDisplay expNode)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		expNode.removeAllChildren();
		expNode.removeAllChildrenDisplay();
		expNode.setChildrenLoaded(Boolean.TRUE);
		expNode.setExpanded(true);
        dtm.reload();
        if (nodes.size() != 0) {
            Iterator i = nodes.iterator();
            while (i.hasNext())
            	expNode.addChildDisplay((TreeImageDisplay) i.next()) ;
            buildTreeNode(expNode, sorter.sort(nodes), 
                        (DefaultTreeModel) treeDisplay.getModel());
        } else buildEmptyNode(expNode);
        Iterator j = nodesToReset.iterator();
        while (j.hasNext()) {
			setExpandedParent((TreeImageDisplay) j.next(), true);
		}
	}

	/**
	 * Sets the number of items imported during a period of time.
	 * 
	 * @param expNode 	The node hosting the experimenter.
	 * @param index		The index of the time node.
	 * @param value		The value to set.
	 */
	void setCountValues(TreeImageDisplay expNode, int index, Object value)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		expNode.setChildrenLoaded(Boolean.TRUE);
		expNode.setExpanded(true);
		int n = expNode.getChildCount();
		TreeImageTimeSet node;
		List l;
		Iterator i, k;
		TreeImageTimeSet child;
		//Test
		List<TreeImageTimeSet> toRemove = new ArrayList<TreeImageTimeSet>();
		List<TreeImageTimeSet> toKeep = new ArrayList<TreeImageTimeSet>();
		int number;
		int total;
		for (int j = 0; j < n; j++) {
			node = (TreeImageTimeSet) expNode.getChildAt(j);
			if (node.getType() == index) {
				if (value instanceof Integer) 
					node.setNumberItems((Integer) value);
				else if (value instanceof List) {
					l = (List) value;
					total = 0;
					i = node.getChildrenDisplay().iterator();
					while (i.hasNext()) {
						child = (TreeImageTimeSet) i.next();
						number = child.countTime(l);
						total += number;
						if (number > 0) {
							child.setNumberItems(number);
							toKeep.add(child);
						} else {
							toRemove.add(child);
						}
					}
					node.removeAllChildren();
					node.removeChildrenDisplay(toRemove);
					node.setNumberItems(total);
					k = toKeep.iterator();
					while (k.hasNext()) {
						dtm.insertNodeInto((TreeImageTimeSet) k.next(), node, 
												node.getChildCount());
					}
				}
				dtm.reload(node);
			}
		}
	}
		
	/**
	 * Refreshes the folder hosting the time.
	 * 
	 * @param expNode	The experimenter node to refresh.
	 * @param r			The data to display.
	 */
	void refreshTimeFolder(TreeImageDisplay expNode, Map<Integer, Set> r)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		expNode.setChildrenLoaded(Boolean.TRUE);
		expNode.setExpanded(true);
		if (r == null || r.size() == 0) return;
		Iterator i = r.keySet().iterator();
		int index;
		int n = expNode.getChildCount();
		TreeImageTimeSet node, child;
		dtm.reload();
		int nodeType;
		List children;
		Iterator s;
		while (i.hasNext()) {
			index = (Integer) i.next();
			for (int j = 0; j < n; j++) {
				node = (TreeImageTimeSet) expNode.getChildAt(j);
				nodeType = node.getType();
				switch (nodeType) {
					case TreeImageTimeSet.YEAR:
					case TreeImageTimeSet.YEAR_BEFORE:
						children = node.getChildrenDisplay();
						s = children.iterator();
						while (s.hasNext()) {
							child = (TreeImageTimeSet) s.next();
							if (child.getIndex() == index) 
								refreshTimeNode(child, r.get(index));
						}
					default:
						if (node.getIndex() == index) 
							refreshTimeNode(node, r.get(index));
						break;
				}
			}
		}
		setExpandedParent(expNode, true);
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
    }
    
	/**
	 * Adds a new experimenter to the tree.
	 * 
	 * @param experimenter  The experimenter to add.
	 * @param load			Pass <code>true</code> to load the data,
	 * 						<code>false</code> otherwise.
	 */
	void addExperimenter(ExperimenterData experimenter, boolean load)
	{
		TreeImageSet node = createExperimenterNode(experimenter);
		DefaultTreeModel dtm = (DefaultTreeModel) treeDisplay.getModel();
		dtm.reload();
		if (load)
			treeDisplay.expandPath(new TreePath(node.getPath()));
	}

	/**
	 * Removes the specified experimenter from the tree.
	 * 
	 * @param exp The experimenter data to remove.
	 */
	void removeExperimenter(ExperimenterData exp)
	{
		TreeImageDisplay root = getTreeRoot();
		List<TreeImageDisplay> nodesToKeep = new ArrayList<TreeImageDisplay>();
		TreeImageDisplay element, node = null;
		Object ho;
		ExperimenterData expElement;
		for (int i = 0; i < root.getChildCount(); i++) {
			element = (TreeImageDisplay) root.getChildAt(i);
			ho = element.getUserObject();
			if (ho instanceof ExperimenterData) {
				expElement = (ExperimenterData) ho;
				if (expElement.getId() == exp.getId())
					node = element;
				else nodesToKeep.add(element);
			}
		}
		if (node != null) root.removeChildDisplay(node);
		Iterator i = nodesToKeep.iterator();
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		root.removeAllChildren();
		while (i.hasNext()) {
			tm.insertNodeInto((TreeImageSet) i.next(), root, 
							root.getChildCount());
		}
		tm.reload();
	}

	/**
	 * Returns the node hosting the logged in user.
	 * 
	 * @return See above.
	 */
	TreeImageDisplay getLoggedExperimenterNode()
	{
		TreeImageDisplay root = getTreeRoot();
		return (TreeImageDisplay) root.getChildAt(0);
	}

	/** Refreshes the experimenter data. */
	void refreshExperimenter()
	{
		TreeImageDisplay root = getTreeRoot();
		TreeImageDisplay element = (TreeImageDisplay) root.getChildAt(0);
		Object ho = element.getUserObject();
		if (ho instanceof ExperimenterData) {
			element.setUserObject(model.getUserDetails());
			DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
			tm.reload(element);
		}
	}

	/**
	 * Sets the nodes selecting via other views.
	 * 
	 * @param newSelection	The collection of nodes to select.
	 */
	void setFoundNode(TreeImageDisplay[] newSelection)
	{
		treeDisplay.removeTreeSelectionListener(selectionListener);
		treeDisplay.clearSelection();
		if (newSelection != null) {
			TreePath[] paths = new TreePath[newSelection.length];
			for (int i = 0; i < newSelection.length; i++) 
				paths[i] = new TreePath(newSelection[i].getPath());

			treeDisplay.setSelectionPaths(paths);
		}
		
		treeDisplay.repaint();
		treeDisplay.addTreeSelectionListener(selectionListener);
	}
    
}
