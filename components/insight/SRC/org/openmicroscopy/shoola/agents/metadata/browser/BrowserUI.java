/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.browser;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.openmicroscopy.shoola.agents.events.treeviewer.DataObjectSelectionEvent;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.metadata.MetadataViewerAgent;
import org.openmicroscopy.shoola.agents.metadata.util.TreeCellRenderer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.env.event.EventBus;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ProjectData;

/** 
 * The view.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class BrowserUI 
	extends JPanel
	implements ActionListener
{

	/** The text of the dummy default node. */
	static final String     		LOADING_MSG = "Loading...";

	/** The text of the default node when the object has not parents */
	//static final String     		NO_PARENTS_MSG = "Wild and Free. " +
	//		"As data, and maybe all creatures should be.";
	static final String     		NO_PARENTS_MSG = "Not in a container.";
	
	/** Action Id indicating to browse the item. */
	private static final int		VIEW = 0;
	
    /** 
     * The text of the node added to a {@link TreeBrowserSet} node
     * containing no element.
     */
    private static final String     EMPTY_MSG = "";

	/** Reference to the controller. */
	private BrowserControl			controller;
	
	/** Reference to the Model. */
	private BrowserModel 			model;
	
	 /** The tree hosting the display. */
    private JTree					treeDisplay;
    
    /** The tool bar hosting the controls. */
    private JToolBar				menuBar;
    
    /** Reference to the listener. */
    private TreeExpansionListener	listener;
    
    /** Reference to the selection listener. */
    private TreeSelectionListener	selectionListener;
    
    /** Menu used to handle the items. */
    private JPopupMenu				menu;
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter    		sorter;
    
    /**
     * Creates the menu to manage the hierarchy.
     * 
     * @return See above.
     */
    private JPopupMenu createManagementMenu()
    {
    	if (menu != null) return menu;
    	menu = new JPopupMenu();
    	menu.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
    	IconManager icons = IconManager.getInstance();
		JMenuItem item = new JMenuItem("Browse");
		item.setActionCommand(""+VIEW);
		item.addActionListener(this);
		item.setIcon(icons.getIcon(IconManager.BROWSE));
		menu.add(item);
    	return menu;
    }
    
    /**
     * Handles the mouse pressed and released.
     * 
     * @param evt The event to handle.
     */
    private void handleMouseClick(MouseEvent evt)
    {
    	if (evt.getClickCount() == 2) {
    		TreeBrowserDisplay node = model.getLastSelectedNode();
        	if (node == null) return;
        	Object uo = node.getUserObject();
        	
        	if (uo instanceof ProjectData || uo instanceof DatasetData) {
        		long id = ((DataObject) uo).getId();
        		DataObjectSelectionEvent event = 
        			new DataObjectSelectionEvent(uo.getClass(), id);
        		event.setSelectTab(true);
        		EventBus bus = MetadataViewerAgent.getRegistry().getEventBus();
        		bus.post(event);
        	} 
    	}
    }
    
    /** Helper method to create the menu bar. */
    private void createMenuBar()
    {
    	menuBar = new JToolBar();
        menuBar.setBorder(null);
        menuBar.setRollover(true);
        menuBar.setFloatable(false);
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
     * Adds a tree menu to the passed node.
     * 
     * @param parent The node the menus is attached to.
     */
    private void addMenuToNode(TreeBrowserDisplay parent)
    {
    	if (parent instanceof TreeBrowserSet) 
			buildEmptyNode(parent);
    }

    /** Creates  the tree. */
    private void createTree()
    {
    	treeDisplay = new JTree();
        treeDisplay.setVisible(true);
        treeDisplay.setRootVisible(false);
        treeDisplay.setCellRenderer(new TreeCellRenderer());
        treeDisplay.setShowsRootHandles(true);
        ToolTipManager.sharedInstance().registerComponent(treeDisplay);
        TreeBrowserDisplay root = model.getLastSelectedNode();
        
        treeDisplay.setModel(new DefaultTreeModel(root));
        //addMenuToNode(root);
        treeDisplay.expandPath(new TreePath(root.getPath()));
    	treeDisplay.addMouseListener(new MouseAdapter() {
    		
        	/**
        	 * Pops up a menu if the mouse click occurs on the tree
        	 * but not on the a node composing the tree
        	 * @see MouseAdapter#mousePressed(MouseEvent)
        	 */
			public void mousePressed(MouseEvent e) { handleMouseClick(e); }
		
			/**
        	 * Pops up a menu if the mouse click occurs on the tree
        	 * but not on the a node composing the tree
        	 * @see MouseAdapter#mouseReleased(MouseEvent)
        	 */
			public void mouseReleased(MouseEvent e) { handleMouseClick(e); }
		});
    	selectionListener = new TreeSelectionListener() {
            
            public void valueChanged(TreeSelectionEvent e)
            {
                controller.onClick();
            }
        };
        treeDisplay.addTreeSelectionListener(selectionListener);
        listener = new TreeExpansionListener() {
            public void treeCollapsed(TreeExpansionEvent e) {
                onNodeNavigation(e, false);
            }
            public void treeExpanded(TreeExpansionEvent e) {
                onNodeNavigation(e, true);  
            }   
        };
        treeDisplay.addTreeExpansionListener(listener);
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
        TreeBrowserDisplay node = (TreeBrowserDisplay) 
        							tee.getPath().getLastPathComponent();
        node.setExpanded(expanded);
        controller.onNodeNavigation(node);
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
    	setLayout(new GridBagLayout());
    	GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.insets = new Insets(0, 2, 2, 0);
		constraints.gridy = 0;
		constraints.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
		constraints.weightx = 1.0;  
		JScrollPane pane = new JScrollPane(treeDisplay);
		Dimension d = pane.getPreferredSize();
		pane.getViewport().setPreferredSize(new Dimension(d.width, 100));
    	pane.setBorder(null);
    	add(pane, constraints);
    	
    }
    
    /** Creates a new instance. */
    BrowserUI() {}
    
    /**
     * Links this View to its Controller and its Model.
     * 
     * @param model         Reference to the Model. 
     * 						Mustn't be <code>null</code>.
     * @param controller	Reference to the Controller.
     * 						Mustn't be <code>null</code>.
     */
    void initialize(BrowserModel model, BrowserControl controller)
    {
    	if (controller == null)
    		throw new IllegalArgumentException("Controller cannot be null");
    	if (model == null)
    		throw new IllegalArgumentException("Model cannot be null");
        this.controller = controller;
        this.model = model;
        sorter = new ViewerSorter();
        createMenuBar();
        createTree();
        buildGUI();
    }

    /** 
     * Returns the selected tree.
     * 
     * @return See above.
     */
	JTree getTreeDisplay() { return treeDisplay; }
    
	/** Sets the root of the tree. */
	void setRootNode()
	{
		treeDisplay.removeAll();
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		TreeBrowserDisplay root = model.getLastSelectedNode();
        treeDisplay.setModel(new DefaultTreeModel(root));
        tm.reload(root);
		/*
		treeDisplay.removeAll();
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		TreeBrowserSet oldRoot = (TreeBrowserSet) tm.getRoot();
		Set children = oldRoot.getChildrenDisplay();
		TreeBrowserDisplay root = model.getLastSelectedNode();
        
        treeDisplay.setModel(new DefaultTreeModel(root));
        //addMenuToNode(root);
        tm = (DefaultTreeModel) treeDisplay.getModel();
        if (children == null || children.size() == 0) {
        	treeDisplay.expandPath(new TreePath(root.getPath()));
        	
        } else {
        	Iterator i = root.getChildrenDisplay().iterator();
        	TreeBrowserDisplay node, newNode;
        	Map<String, TreeBrowserDisplay> 
        		map = new HashMap<String, TreeBrowserDisplay>();
        	while (i.hasNext()) {
				node = (TreeBrowserDisplay) i.next();
				map.put(node.toString(), node);
			}
        	i = children.iterator();
        	while (i.hasNext()) {
				node = (TreeBrowserDisplay) i.next();
				if (node.isExpanded()) {
					newNode = map.get(node.toString());
					if (newNode != null) {
						treeDisplay.expandPath(new TreePath(newNode.getPath()));
					}
				}
			}
        	
        }
        tm.reload(root);
        */
	}
	
	/**
	 * Creates a dummy loading node whose parent is the specified node.
	 * 
	 * @param message	The value of the default node.
	 */
    void addDefaultNode(String message)
    {
        addDefaultNode(model.getRoot(), message);
    }
    
    /**
     * Creates a dummy loading node to the specified node.
     * 
     * @param node 		The parent of the default node.
     * @param message	The value of the default node.
     */
    void addDefaultNode(TreeBrowserDisplay node, String message)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
        node.removeAllChildren();
        node.removeAllChildrenDisplay();
        if (message != null)
        	tm.insertNodeInto(new TreeBrowserNode(message), node,
                			node.getChildCount());
        tm.reload(node);
    }

    /**
     * Adds the nodes to the specified parent node.
     * 
     * @param parent	The parent node.
     * @param nodes		The nodes to add.
     */
	void setNodes(TreeBrowserDisplay parent, Collection nodes)
	{
		List sortedNodes = sorter.sort(nodes);
		DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
		parent.removeAllChildren();
		parent.removeAllChildrenDisplay();
		Iterator i = sortedNodes.iterator();
		TreeBrowserDisplay child;
		//addMenuToNode(parent);
		while (i.hasNext()) {
			child = (TreeBrowserDisplay) i.next();
			if (!(child instanceof TreeBrowserNode))
				addMenuToNode(child);
			parent.addChildDisplay(child);
			tm.insertNodeInto(child, parent, parent.getChildCount());
		}
		tm.reload(parent);
	}

	/**
	 * Reacts to selection in the management menu.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case VIEW:
				controller.browser(model.getSelectedNodes());
				break;
		}
	}
    
}
