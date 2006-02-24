/*
 * org.openmicroscopy.shoola.util.ui.clsf.TreeCheck
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

package org.openmicroscopy.shoola.util.ui.clsf;

//Java imports
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays an hierarchical data. Each node added to the structure
 * is an instance of {@link TreeCheckNode} i.e. a selection box to select the 
 * node is added to the node if the parameter <code>leafOnly</code> is set to 
 * <code>false</code>. If the parameter <code>leafOnly</code> is set to 
 * <code>true</code>, only the leaf nodes can be selected.
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
public class TreeCheck
    extends JTree
{

    /** 
     * Flag to indicate that only one child can be selected in a given parent.
     * If <code>true</code> only one child can be selected at a time.
     * If <code>false</code> multiple children can be selected.
     */
    private boolean singleSelectionInParent;
    
    /**
     * Selects the specified node and deselects the other siblings.
     * 
     * @param node The node to select.
     */
    private void handleSingleSelection(TreeCheckNode node)
    {
        TreeCheckNode parent = node.getParentDisplay();
        if (parent == null) return;
        if (node.isSelected()) return; //node already selected.
        Set nodes = parent.getChildrenDisplay();
        Iterator i = nodes.iterator();
        TreeCheckNode child;
        while (i.hasNext()) {
            child = (TreeCheckNode) i.next();
            child.setSelected(child.equals(node));
        }
    }
    
    /**
     * Initializes the tree i.e. set the model, the selection model, etc.
     * 
     * @param root      The root node of the tree.
     * @param leafOnly  Passed <code>true</code> to allow leaves selection only
     *                  <code>false</code> otherwise.
     */
    private void initialize(TreeCheckNode root, boolean leafOnly)
    {
        setCellRenderer(new TreeCheckRenderer(leafOnly));
        getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        putClientProperty("JTree.lineStyle", "Angled");
        setShowsRootHandles(true);
        super.setModel(new TreeCheckModel(root));
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                int row = getRowForLocation(me.getX(), me.getY());
                TreePath path = getPathForRow(row);
                if (path == null) return;
                Object o = path.getLastPathComponent();
                if (!(o instanceof TreeCheckNode)) return;
                TreeCheckNode node = (TreeCheckNode) o;
                if (singleSelectionInParent) handleSingleSelection(node);
                else node.setSelected(!node.isSelected());
                    
                ((DefaultTreeModel) getModel()).nodeChanged(node);
                if (row == 0) {
                    revalidate();
                    repaint();
                }
            }
        });
    }
    
    /**
     * Creates a new instance.
     * 
     * @param rootObject                The object hosted by the root node.
     * @param rootIcon                  The icon of the root node.
     */
    public TreeCheck(Object rootObject, Icon rootIcon)
    {
        initialize(new TreeCheckNode(rootObject, rootIcon), true);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param root The root node.
     */
    public TreeCheck(TreeCheckNode root)
    {
        initialize(root, true);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param rootObject                The object hosted by the root node.
     * @param rootIcon                  The icon of the root node.
     * @param leafOnly                  Passed <code>true</code> to allow 
     *                                  leaves selection only <code>false</code>
     *                                  otherwise.               
     */
    public TreeCheck(Object rootObject, Icon rootIcon, boolean leafOnly)
    {
        initialize(new TreeCheckNode(rootObject, rootIcon), leafOnly);
    }
    
    /**
     * Sets the {@link #singleSelectionInParent} value.
     * 
     * @param b The value to set.
     */
    public void setSingleSelectionInParent(boolean b)
    {
        singleSelectionInParent = b;
        TreeCheckRenderer rnd = (TreeCheckRenderer) getCellRenderer();
        if (singleSelectionInParent) rnd.initToggleButton(JRadioButton.class);
        else rnd.initToggleButton(JCheckBox.class);
    }
    
    /**
     * Returns <code>true</code> if for a given parent, only one child
     * can be selected at a time, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isSingleSelectionInParent()
    { 
        return singleSelectionInParent;
    }
    
    /**
     * Returns a collection of selected {@link TreeCheckNode}s.
     * 
     * @return See above.
     */
    public Set getSelectedNodes()
    {
        HashSet set = new HashSet();
        DefaultTreeModel dtm = (DefaultTreeModel) getModel();
        TreeCheckNode root = (TreeCheckNode) dtm.getRoot();
        Enumeration nodes = root.breadthFirstEnumeration();
        TreeCheckNode node;
        while (nodes.hasMoreElements()) {
            node = (TreeCheckNode) nodes.nextElement();
            if (node.isSelected()) set.add(node);
        }
        return Collections.unmodifiableSet(set);
    }
    
    /**
     * Overriden to make sure that the root node is a {@link TreeCheckNode}.
     * @see JTree#setModel(TreeModel)
     */
    public void setModel(TreeModel newModel) {}
    
}
