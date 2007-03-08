/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindResultsPane
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder;



//Java imports
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.util.TreeCellRenderer;

/** 
 * Displays the occurences found.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * after code by
 *          Barry Anderson &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:banderson@computing.dundee.ac.uk">
 *              banderson@computing.dundee.ac.uk</a>
 *              
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class FindResultsPane
    extends JTree
{

    /** The default root's text. */
    private static final String     ROOT = "Results";
    
    /** The text displayed if there is no node. */
    private static final String     EMPTY = "Empty";
    
    /** Reference to the parent of this frame. */
    private FindPane                					model;

    /** The map where the tree nodes are stored. */
    private Map<ImageDisplay, DefaultMutableTreeNode>	identityMap;
    
    /** Initializes the component. */
    private void initialize()
    {
    	setRootVisible(false);
        putClientProperty("JTree.lineStyle", "Angled");
        setCellRenderer(new TreeCellRenderer(false, true));
        getSelectionModel().setSelectionMode(
                                    TreeSelectionModel.SINGLE_TREE_SELECTION);
        //Attach a mouse listener
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
    }
    
    /**
     * Sets the selected node and brings up on screen 
     * the popup menu if the {@link MouseEvent} is the popup menu event 
     * for the platform.
     * 
     * @param me The mouse event to handle.
     */
    private void onClick(MouseEvent me)
    {
        int row = getRowForLocation(me.getX(), me.getY());
        if (row != -1) {
            setSelectionRow(row);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
                                            getLastSelectedPathComponent();
            model.setSelectedNode(node);
            if (me.isPopupTrigger()) {
                Object uo = node.getUserObject();
                if (uo instanceof String) return;
                ImageDisplay  object = (ImageDisplay) uo;
                model.showMenu(this, me.getPoint(), object);
            }
        }
    } 
    
    /** 
     * Controls if the specified child as already been added to the 
     * specified parent. 
     * 
     * @param child     The specified {@link DefaultMutableTreeNode child}.
     * @param parent    The specified {@link DefaultMutableTreeNode parent}.
     * @return  <code>true</code> if the specified child has already been added
     *          to the specified parent, <code>false</code> otherwise.
     */
    private boolean hasBeenAdded(DefaultMutableTreeNode child,
                                DefaultMutableTreeNode parent)
    {
        DefaultTreeModel tm = (DefaultTreeModel) getModel();
        int n = tm.getChildCount(parent);
        for (int i = 0; i < n; i++)
            if (tm.getChild(parent, i) == child) return true;
        return false;
    }
    
    /** 
     * Builds a node corresponding to the specified {@link ImageDisplay}.
     * 
     * @param node  The specified {@link ImageDisplay}.
     */
    private void buildTreeNode(ImageDisplay node)
    {
        DefaultMutableTreeNode dtn;
        DefaultTreeModel tm = (DefaultTreeModel) getModel();
        dtn = identityMap.get(node);
        if (dtn == null) { // create a tree node
            dtn = new DefaultMutableTreeNode(node);
            identityMap.put(node, dtn);
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tm.getRoot();
        if (!(hasBeenAdded(dtn, root)))
            tm.insertNodeInto(dtn, root, root.getChildCount());
        /*
        ImageDisplay parent = node.getParentDisplay();
        if (parent.getParentDisplay() == null) { //i.e. the parent is root
            if (!(hasBeenAdded(dtn, root)))
                tm.insertNodeInto(dtn, root, root.getChildCount());
        } else {
            DefaultMutableTreeNode 
                pdtn = (DefaultMutableTreeNode) identityMap.get(parent);
            if (pdtn == null) { // create a tree node
                pdtn = new DefaultMutableTreeNode(parent);
                identityMap.put(parent, pdtn);
            }
            if (!(hasBeenAdded(dtn, pdtn)))
                tm.insertNodeInto(dtn, pdtn, pdtn.getChildCount());
            buildTreeNode(parent);
        }   
        */
    }
    
    /**
     * Displays the specified collection of nodes. 
     * 
     * @param nodes The collection of nodes.
     */
    private void buildTree(List nodes)
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(ROOT);
        DefaultTreeModel dtm = new DefaultTreeModel(root);
        setModel(dtm);
        setShowsRootHandles(true);
        
        if (nodes.size() == 0) {
            DefaultMutableTreeNode childNode = 
                new DefaultMutableTreeNode(EMPTY);
            DefaultTreeModel tm = (DefaultTreeModel) getModel();
            tm.insertNodeInto(childNode, root, root.getChildCount());
        } else {
            Iterator i = nodes.iterator();
            while (i.hasNext())
                buildTreeNode((ImageDisplay) i.next());
        }
        expandPath(new TreePath(root.getPath()));
        dtm.reload();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model The parent of the is UI component.
     *              Mustn't be <code>null</code>.
     * @param nodes The set of nodes to display.
     */
    FindResultsPane(FindPane model, List nodes)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        if (nodes == null) 
            throw new IllegalArgumentException("No nodes to display.");
        this.model = model;
        identityMap = new HashMap<ImageDisplay, DefaultMutableTreeNode>();
        initialize();
        buildTree(nodes);
    }
    
    /**
     * Returns the number of results found.
     * 
     * @return See above.
     */
    int getSizeResults() { return identityMap.size(); }

    /** Finds the next occurence. */
	void findNext()
	{
		int[] rows = getSelectionRows();
		int index;
		if (rows == null || rows.length == 0) index = 0;
		else index = rows[0];
        int n = getSizeResults()-1;
        if (index < n) index++; //not last element
        else if (index == n) index = 0;
        setSelectionRow(index);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
        getLastSelectedPathComponent();
        model.setSelectedNode(node);
	}
	
	 /** Finds the previous occurence. */
	void findPrevious()
	{
		int[] rows = getSelectionRows();
		int index;
		if (rows == null || rows.length == 0) index = 0;
		else index = rows[0];
        if (index > 0)  index--; //not last element
        else if (index == 0)  index = getSizeResults()-1;;
        setSelectionRow(index);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) 
        getLastSelectedPathComponent();
        model.setSelectedNode(node);
	}
	
}
