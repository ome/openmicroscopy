/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.TreeLayout
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

package org.openmicroscopy.shoola.agents.hiviewer.layout;


//Java imports
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.util.TreeCellRenderer;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;

/** 
 * Lays out all the container nodes in a tree.
 * Node containing images are collapsed, so images are not showing.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class TreeLayout
    implements Layout
{

    /** Textual description of this layout. */
    static final String         DESCRIPTION = "Lays out all the container " +
                                                "nodes in a tree.";
    
    /** The tree representation of the display. */
    private JTree                   treeDisplay;
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter            sorter;
    
    /**
     * The component observing the changes in the display.
     */
    private Browser                 observer;
    
    /**
     * Reacts to click events on a specified node.
     * 
     * @param me The {@link MouseEvent} to handle.
     */
    private void onClick(MouseEvent me)
    {
        int row = treeDisplay.getRowForLocation(me.getX(), me.getY());
        if (row != -1) {
            treeDisplay.setSelectionRow(row);
            
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) 
                        treeDisplay.getLastSelectedPathComponent();
            ImageDisplay selectedDisplay = (ImageDisplay) n.getUserObject();
            observer.setSelectedDisplay(selectedDisplay);
            
            if (me.isPopupTrigger()) observer.setPopupPoint(me.getPoint());
            treeDisplay.getCellRenderer().getTreeCellRendererComponent(
                        treeDisplay, n, 
                        treeDisplay.isPathSelected(new TreePath(n.getPath())),
                        false, true, 0, false);
        }
        
    }
    
    /**
     * Creates the tree hosting the display.
     * 
     * @return The root node of the tree.
     */
    private DefaultMutableTreeNode createTree()
    {
        treeDisplay = new JTree();
        treeDisplay.setShowsRootHandles(true);
        treeDisplay.setCellRenderer(new TreeCellRenderer(true, true));
        treeDisplay.putClientProperty("JTree.lineStyle", "Angled");
        treeDisplay.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
        DefaultTreeModel dtm = new DefaultTreeModel(root);
        treeDisplay.setModel(dtm);
        
        treeDisplay.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
        return root;
    }
    
    /** 
     * Builds a node corresponding to the specified {@link ImageDisplay}.
     * 
     * @param parent    The specified {@link ImageDisplay}.
     * @param nodes     The list of nodes to add to the specified parent.
     */
    private void buildTreeNode(DefaultMutableTreeNode parent, List nodes)
    {
        DefaultTreeModel tm = (DefaultTreeModel) treeDisplay.getModel();
        Iterator i = nodes.iterator();
        DefaultMutableTreeNode dtn = null;
        ImageDisplay imageDisplay;
        Set children;
        while (i.hasNext()) {
            imageDisplay = (ImageDisplay) i.next();
            dtn = new DefaultMutableTreeNode(imageDisplay); 
            tm.insertNodeInto(dtn, parent, parent.getChildCount());
            children = imageDisplay.getChildrenDisplay();
            if (children.size() != 0)
                buildTreeNode(dtn, sorter.sort(children));
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tm.getRoot();
        if (parent == root && dtn != null) {
            treeDisplay.collapsePath(new TreePath(dtn.getPath()));
            treeDisplay.setRootVisible(false);
        }  
    }
    
    /**
     * Package constructor so that objects can only be created by the
     * {@link LayoutFactory}.
     */
    TreeLayout()
    {
        //if (observer == null) 
        //    throw new IllegalArgumentException("No observer.");
        //this.observer = observer;
        sorter = new ViewerSorter();
    }

    /**
     * Lays out the current container display.
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
        if (node.getParentDisplay() != null) return;
        if (node.getChildrenDisplay().size() == 0) return;
        if (treeDisplay == null) {
            DefaultMutableTreeNode root = createTree();
            buildTreeNode(root, sorter.sort(node.getChildrenDisplay()));
        }
        //observer.setTreeDisplay(treeDisplay);
    }

    /**
     * No-op implementation, as we only layout container displays.
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node) {}

    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getDescription()
     */
    public String getDescription() { return DESCRIPTION; }
    
    /**
     * Implemented as specified by the {@link Layout} interface.
     * @see Layout#getIndex()
     */
    public int getIndex() { return LayoutFactory.TREE_LAYOUT; }
    
}
