/*
 * org.openmicroscopy.shoola.util.ui.clsf.TreeCheckNode
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies

/** 
 * A {@link DefaultMutableTreeNode} node hosting the icon associated to the 
 * node.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeCheckNode
    extends DefaultMutableTreeNode
{
    
    /** The icon associated to that node. */
    private Icon            nodeIcon;
    
    /** Flag to indicate if the node is selected. */
    private boolean         selected;
    
    /** 
     * The set of nodes that have been added to this node.
     * Will always be empty for a leaf node. 
     */
    private Set             childrenDisplay;
    
    /** 
     * Back pointer to the parent node or <code>null</code> if this is the root.
     */
    private TreeCheckNode   parentDisplay;
    
    /** The name of the node. Might be <code>null</code>. */
    private String          name;
    
    /** Indicates if the specified node as to be considered as a leaf node. */
    private boolean         leafNode;
    
    /**
     * Creates a new instance.
     * 
     * @param hierarchyObject   The hierarchy object associated to this node.
     * @param nodeIcon          The icon associated to this node.
     */
    public TreeCheckNode(Object hierarchyObject, Icon nodeIcon)
    {
       this(hierarchyObject, nodeIcon, null, false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param hierarchyObject   The hierarchy object associated to this node.
     * @param nodeIcon          The icon associated to this node.
     * @param name              The name of the node.
     * @param leafNode          Passed <code>true</code> if the node is a leaf,
     *                          <code>false</code> otherwise.
     */
    public TreeCheckNode(Object hierarchyObject, Icon nodeIcon, String name, 
                        boolean leafNode)
    {
        if (hierarchyObject == null)
            throw new IllegalArgumentException("No hierachyObject.");
        setUserObject(hierarchyObject);
        childrenDisplay = new HashSet();
        this.nodeIcon = nodeIcon;
        this.name = name;
        this.leafNode = leafNode;
    }
    
    /**
     * Returns <code>true</code> if the node is considered as a leaf node,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isLeafNode() { return leafNode; }
    
    /**
     * Returns the parent node or <code>null</code> if this is the root.
     * 
     * @return See above.
     */
    public TreeCheckNode getParentDisplay() { return parentDisplay; }
    
    /**
     * Returns all the child nodes to this node in the visualization tree.
     * Note that, although never <code>null</code>, the returned set may be
     * empty. In particular, this is always the case for a leaf node &#151;
     * that is an {@link TreeCheckNode}.
     * 
     * @return A <i>read-only</i> set containing all the child nodes.
     */
    public Set getChildrenDisplay() 
    { 
        return Collections.unmodifiableSet(childrenDisplay);
    }
    
    /**
     * Returns <code>true</code> if the nodes contain children,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean hasChildrenDisplay()
    {
        return (childrenDisplay.size() != 0);
    }
    
    /**
     * Adds a node to the visualization tree as a child of this node.
     * The node is added to the internal desktop of this node, but you
     * will have to set its bounds for it to show up &#151; this is a
     * consequence of the fact that a desktop has no layout manager.
     * The <code>child</code>'s parent is set to be this node.  If <code>
     * child</code> is currently a child to another node <code>n</code>, 
     * then <code>child</code> is first 
     * {@link #removeChildDisplay(TreeCheckNode) removed} from <code>n</code>
     * and then added to this node. 
     * 
     * @param child The node to add. Mustn't be <code>null</code>.
     * @see DefaultMutableTreeNode
     */
    public void addChildDisplay(TreeCheckNode child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) return;
        if (child.parentDisplay != null)  //Was the child of another node.
            child.parentDisplay.removeChildDisplay(child);
        child.parentDisplay = this;
        childrenDisplay.add(child);
    }
    
    /**
     * Removes the specified <code>child</code> node.
     * If <code>child</code> is not among the children of this node, no action
     * is taken. Otherwise, it is removed from the children set and orphaned.
     * That is, its parent (which is this node) is set to <code>null</code>.
     * 
     * @param child The node to remove. Mustn't be <code>null</code>.
     */
    public void removeChildDisplay(TreeCheckNode child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenDisplay.contains(child)) {
            //NOTE: parentDisplay != null b/c child has been added through
            //the add method.
            child.parentDisplay.childrenDisplay.remove(child);
            child.parentDisplay = null;
        }
    }
    
    /**
     * Returns the icon associated to that node. 
     * 
     * @return See above.
     */
    public Icon getNodeIcon() { return nodeIcon; }
    
    /**
     * Returns <code>true</code> if the node is selected, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean isSelected() { return selected; }
    
    /**
     * Sets <code>true</code> if the node is selected, <code>false</code>
     * otherwise.
     * 
     * @param selected  The flag to set.
     */
    public void setSelected(boolean selected) { this.selected = selected; }
    
    /**
     * Overriden to return the name of the node.
     * @see DefaultMutableTreeNode#toString()
     */
    public String toString()
    {
        if (name != null) return name;
        return getUserObject().toString();
    }
    
}
