/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeViewUI
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

package org.openmicroscopy.shoola.agents.hiviewer.treeview;

//Java imports
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageNode;
import org.openmicroscopy.shoola.agents.hiviewer.util.TreeCellRenderer;
import org.openmicroscopy.shoola.env.ui.ViewerSorter;


/** 
 * The UI delegate of the {@link TreeView}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
class TreeViewUI
{
    
    /** 
     * A {@link ViewerSorter sorter} to order nodes in ascending 
     * alphabetical order.
     */
    private ViewerSorter	sorter;
    
    /** The component hosting the visualization tree. */
    private JTree			tree;
    
    /** The tool bar. */
    private JToolBar        toolBar;
    
    /** The menu bar. */
    private JPanel			menuBar;
    
    /** The number of visible images displayed in the tree. */
    private int				numberImages;
    
    /** Reference to the Model this component is for. */
    private TreeView		model;
    
    /** Helper method to create the {@link #toolBar}. */
    private void createToolBar()
    {
        toolBar = new JToolBar();
        toolBar.setRollover(true);
        toolBar.setFloatable(false);
        toolBar.add(new JSeparator(SwingConstants.VERTICAL));
        JButton button = new CollapseButton(model);
        toolBar.add(button);
        button = new CloseButton(model);
        toolBar.add(button);
    }
    
    /** Helper method to create the {@link #menuBar}. */
    private void createMenuBar()
    { 
        menuBar = new JPanel();
        menuBar.setBorder(BorderFactory.createEtchedBorder());
        menuBar.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(3, 3, 3, 3);
        JLabel label = new JLabel();
        String s = " image";
        if (numberImages > 1) s+="s";
        label.setText("Contains "+numberImages+s+".");
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        p.add(label);
        p.add(Box.createRigidArea(new Dimension(5, label.getSize().height)));
        p.add(toolBar);
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;  
        menuBar.add(label, c);
        label.setLabelFor(p);
        c.gridx = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        menuBar.add(p, c);
    }
    
    /** 
     * Builds a {@link TreeViewNode} for each {@link ImageDisplay} 
     * items of the specified collection. The newly created node is then
     * added to the specified parent.
     * 
     * @param parent	The specified {@link TreeViewNode parent}.
     * @param nodes		The nodes to add to the specified parent.		
     */
    private void buildTreeNode(TreeViewNode parent, List nodes)
    {
        DefaultTreeModel tm = (DefaultTreeModel) tree.getModel();
        Iterator i = nodes.iterator();
        TreeViewNode dtn = null;
        ImageDisplay imageDisplay;
        Set children;
        while (i.hasNext()) {
            imageDisplay = (ImageDisplay) i.next();
            if (imageDisplay instanceof ImageNode) {
                numberImages++;
                dtn = new TreeViewImageNode(imageDisplay);
            } else dtn = new TreeViewImageSet(imageDisplay);
            parent.addChildNode(dtn); 
            tm.insertNodeInto(dtn, parent, parent.getChildCount());
            children = imageDisplay.getChildrenDisplay();
            if (children.size() != 0)
                buildTreeNode(dtn, sorter.sort(children));
        }
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tm.getRoot();
        if (parent == root && dtn != null)
            tree.collapsePath(new TreePath(dtn.getPath()));
    }
    
    /** 
     * Creates the tree hosting the display. 
     * 
     * @param node The root the data displayed in the <code>Browser</code>.
     * @return The root node of the <code>JTree</code>.
     */
    private TreeViewImageSet createTree(ImageDisplay node)
    {
        tree = new JTree();
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new TreeCellRenderer(true, false));
        tree.putClientProperty("JTree.lineStyle", "Angled");
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        TreeViewImageSet root = new TreeViewImageSet(node);
        tree.setModel(new DefaultTreeModel(root));
        tree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { onClick(e); }
            public void mouseReleased(MouseEvent e) { onClick(e); }
        });
        return root;
    }
    
    /**
     * Reacts to click events on a specified node.
     * 
     * @param me The {@link MouseEvent} to handle.
     */
    private void onClick(MouseEvent me)
    {
        int row = tree.getRowForLocation(me.getX(), me.getY());
        if (row != -1) {
            //tree.setSelectionRow(row);
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) 
                        tree.getLastSelectedPathComponent();
            model.setSelectedDisplay((ImageDisplay) n.getUserObject());
            if (me.isPopupTrigger()) model.setPopupPoint(me.getPoint());
            tree.getCellRenderer().getTreeCellRendererComponent(tree, n, 
                	tree.isPathSelected(new TreePath(n.getPath())),
                	false, true, 0, false); 
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model	Reference to the Model. Mustn't be <code>null</code>.
     * @param rootNode The root node. Mustn't be <code>null</code>.
     */
    TreeViewUI(TreeView model, ImageDisplay rootNode)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        if (rootNode == null) 
            throw new IllegalArgumentException("No root node.");
        this.model = model;
        sorter = new ViewerSorter();
        numberImages = 0;
        TreeViewImageSet root = createTree(rootNode);
        Set nodes = rootNode.getChildrenDisplay();
        buildTreeNode(root, sorter.sort(nodes));
        createToolBar();
        createMenuBar();  
        model.setPreferredSize(menuBar.getPreferredSize());
    }
    
    /**
     * Returns the component hosting the visualization tree.
     * 
     * @return See above.
     */
    JTree getTree() { return tree; }
    
    /**
     * Returns the component hosting the controls.
     * 
     * @return See above.
     */
    JPanel getMenuBar() { return menuBar; }
    
    /**
     * Repaints the specified nodes i.e. the previously selected node if 
     * not <code>null</code> and the newly selected one.
     * 
     * @param oldNode The previously selected node.
     * @param newNode The selected node.
     */
    void selectNodes(TreeViewNode oldNode, TreeViewNode newNode)
    {
        TreeCellRenderer renderer = (TreeCellRenderer) tree.getCellRenderer();
        if (oldNode != null)
            renderer.getTreeCellRendererComponent(tree, oldNode, 
                		tree.isPathSelected(new TreePath(oldNode.getPath())),
                		false, true, 0, false);
        if (newNode != null) {
            TreePath path = new TreePath(newNode.getPath());
            tree.setSelectionPath(path);
            renderer.getTreeCellRendererComponent(tree, newNode, 
                    	tree.isPathSelected(path), false, true, 0, false);
        }   
    }
    
}
