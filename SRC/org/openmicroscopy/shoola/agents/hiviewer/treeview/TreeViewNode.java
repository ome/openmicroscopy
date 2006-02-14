/*
 * org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeViewNode
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies


/** 
 * Represents a component in the composite structure used to visualize an
 * image hierarchy.
 * <p>A concrete component can be either a {@link TreeViewImageNode}, to 
 * represent a single image, or an {@link TreeViewImageSet}, to represent a 
 * collection of images.
 * An {@link TreeViewImageSet} can also contain other image sets, 
 * thus leading to a composite structure. This is a tree whose leaf nodes are
 * {@link TreeViewImageNode} objects and internal nodes are
 * {@link TreeViewImageSet} objects.</p>
 * <p>So we have a general purpose, set-based structure we can use to visualize 
 * any image hierarchy: Project/Dataset/Image, Category Group/Category/Image, 
 * or Screen/Plate/Well/Image. The original data hierarchy translates into
 * a visualization tree as follows. Each image object corresponds to an
 * {@link TreeViewImageNode} and an image container, such as Dataset or 
 * Category, corresponds to an {@link TreeViewImageSet}. All 
 * {@link TreeViewImageNode} objects that are created for the images in a given
 * image container are added to the {@link TreeViewImageSet} object created for
 * that image container. Nested containers translate into nested
 * {@link TreeViewImageSet}s. For example, say you have a 
 * Project <code>p_1</code> and two datasets in it, <code>d_1</code> and <code>
 * d_2</code>.  The former contains image <code>i_1</code> and <code>i_2</code>,
 * as the latter only has one image, <code>i_3</code>.  This would translate
 * into three {@link TreeViewImageNode}s <code>in_1, in_2, in_3</code>,
 * respectively for <code>i_1, i_2, i_3</code>. You would then create two
 * {@link TreeViewImageSet}s <code>ds_1, ds_2</code>, respectively for
 * <code>d_1, d_2</code>, and add <code>in_1, in_2</code> to <code>ds_1</code> 
 * and <code>in_3</code> to <code> ds_2</code>. Finally you would create a third
 * {@link TreeViewImageSet} <code>ps_1</code> for the Project and add
 * <code>ds_1, ds_2</code> to it.</p> 
 * <p>Operations on a visualization tree are performed through visitors.  The
 * {@link TreeViewNodeVisitor} interface allows you to define arbitrary 
 * operations that can then be applied to the tree by calling the 
 * {@link #accept(TreeViewNodeVisitor) accept} method, usually on the root node.
 * An example of this is layout management. In fact, an {@link TreeViewImageSet}
 * can contain other nodes &#151; this class inherits from 
 * {@link org.openmicroscopy.shoola.agents.hiviewer.tframe.TinyFrame} and
 * nodes are added to its internal desktop, which has no layout manager. In
 * order to position the contained nodes properly, you can write a layout class
 * that implements the {@link TreeViewNodeVisitor} interface to lay out the
 * contents of every {@link TreeViewImageSet} node in a visualization tree.</p>
 * 
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$Date: )
 * </small>
 * @since OME2.2
 */
public abstract class TreeViewNode
	extends DefaultMutableTreeNode
{

    /** 
     * Back pointer to the parent node or <code>null</code> if this is the root.
     */
    private TreeViewNode	parentNode;
    
    /** 
     * The set of nodes that have been added to this node.
     * Will always be empty for a leaf node. 
     */
    private Set				childrenNode;
    
    /**
     * Checks if the algorithm to visit the tree is one of the constants
     * defined by {@link TreeViewNodeVisitor}.
     * 
     * @param type The algorithm type.
     */
    private void checkAlgoType(int type)
    {
        switch (type) {
            case TreeViewNodeVisitor.IMAGE_NODE_ONLY:
            case TreeViewNodeVisitor.IMAGE_SET_ONLY:    
            case TreeViewNodeVisitor.ALL_NODES:
                return;
            default:
                throw new IllegalArgumentException("Algorithm not supported.");
        }
    }
    
    /**
     * Constructor used by subclasses.
     * 
     * @param userObject 	The original object in the image hierarchy which
     *                   	is visualized by this node.  
     * 						Never pass <code>null</code>. 
     */
    protected TreeViewNode(Object userObject)
    {
        if (userObject == null) 
            throw new NullPointerException("No hierarchy object.");
        setUserObject(userObject);
        childrenNode = new HashSet();
    }
    
    /**
     * Returns the parent node to this node in the visualization tree.
     * 
     * @return 	The parent node or <code>null</code> if this node has no parent.
     *          This can happen if this node hasn't been linked yet or if it's
     *          the root node.
     */
    public TreeViewNode getParentNode() { return parentNode; }
    
    /**
     * Returns all the child nodes to this node in the visualization tree.
     * Note that, although never <code>null</code>, the returned set may be
     * empty.  In particular, this is always the case for a leaf node &#151;
     * that is an {@link TreeViewImageNode}.
     * 
     * @return A <i>read-only</i> set containing all the child nodes.
     */
    public Set getChildrenNode() 
    { 
        return Collections.unmodifiableSet(childrenNode);
    }
    
    /**
     * Adds a node to the visualization tree as a child of this node.
     * The node is added to the internal desktop of this node, but you
     * will have to set its bounds for it to show up &#151; this is a
     * consequence of the fact that a desktop has no layout manager.
     * The <code>child</code>'s parent is set to be this node.  If <code>
     * child</code> is currently a child to another node <code>n</code>, 
     * then <code>child</code> is first 
     * {@link #removeChildNode(TreeViewNode) removed} from <code>n</code>
     * and then added to this node. 
     * 
     * @param child The node to add. Mustn't be <code>null</code>.
     * @see DefaultMutableTreeNode
     */
    public void addChildNode(TreeViewNode child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenNode.contains(child)) return;
        if (child.parentNode != null)  //Was the child of another node.
            child.parentNode.removeChildNode(child);
        child.parentNode = this;
        childrenNode.add(child);
    }
    
    /**
     * Removes the specified <code>child</code> node.
     * If <code>child</code> is not among the children of this node, no action
     * is taken. Otherwise, it is removed from the children set and orphaned.
     * That is, its parent (which is this node) is set to <code>null</code>.
     * 
     * @param child The node to remove. Mustn't be <code>null</code>.
     */
    public void removeChildNode(TreeViewNode child)
    {
        if (child == null) throw new NullPointerException("No child.");
        if (childrenNode.contains(child)) {
            //NOTE: parentDisplay != null b/c child has been added through
            //the add method.
            child.parentNode.childrenNode.remove(child);
            child.parentNode = null;
        }
    }
    
    /** Removes all <code>children</code> nodes from the children set. */
    public void removeAllChildrenNode()
    {
        Iterator i = childrenNode.iterator();
        Set toRemove = new HashSet(childrenNode.size());
        while (i.hasNext())
            toRemove.add(i.next());
        i = toRemove.iterator();
        while (i.hasNext())
            removeChildNode((TreeViewNode) i.next());
    }
    
    /**
     * Has the specified object visit this node and all nodes below this one
     * in the visualization tree.
     * For each node, the <code>visit</code> method is called passing in the
     * node being visited.
     * 
     * @param visitor The visitor. Mustn't be <code>null</code>.
     * @see TreeViewNodeVisitor
     */
    public void accept(TreeViewNodeVisitor visitor)
    {
        if (visitor == null) throw new NullPointerException("No visitor.");
        accept(visitor, TreeViewNodeVisitor.ALL_NODES);
    }
    
    /**
     * Has the specified object visit this node and all nodes below this one
     * in the visualization tree.
     * According to the specified <code>algoType</code>,
     * the <code>visit</code> method is called passing in the
     * node being visited. 
     * 
     * @param visitor   The visitor. Mustn't be <code>null</code>.
     * @param algoType  The algorithm selected. Must be one of the constants
     *                  defined by {@link TreeViewNodeVisitor}.
     * @see TreeViewNodeVisitor
     */
    public void accept(TreeViewNodeVisitor visitor, int algoType)
    {
        if (visitor == null) throw new NullPointerException("No visitor.");
        checkAlgoType(algoType);
        Iterator i = childrenNode.iterator();
        TreeViewNode child;
        switch (algoType) {
            case TreeViewNodeVisitor.IMAGE_NODE_ONLY:
                while (i.hasNext()) {
                    child = (TreeViewNode) i.next();
                    child.accept(visitor, algoType);
                }
                if (this instanceof TreeViewImageNode) doAccept(visitor);
                break;
            case TreeViewNodeVisitor.IMAGE_SET_ONLY:
                while (i.hasNext()) {
                    child = (TreeViewNode) i.next();
                    if (child instanceof TreeViewImageSet)
                        child.accept(visitor, algoType);
                }
                if (this instanceof TreeViewImageSet) doAccept(visitor);
                break;
            case TreeViewNodeVisitor.ALL_NODES:
                while (i.hasNext()) {
                    child = (TreeViewNode) i.next();
                    child.accept(visitor, algoType);
                }
                doAccept(visitor);
                break;
        }
    }
    
    /**
     * Made final to ensure objects are compared by reference so that the
     * {@link #addChildNode(TreeViewNode) addChildDisplay} and
     * {@link #removeChildNode(TreeViewNode) removeChildDisplay} methods
     * will work fine.
     * @see #equals(Object)
     */
    public final boolean equals(Object x) { return (this == x); }
    
    /**
     * Implemented by subclasses to call the right version of the <code>visit
     * </code> method on the specified <code>visitor</code>.
     * This method is called by {@link #accept(TreeViewNodeVisitor)} during
     * the nodes iteration.  Subclasses will just call the <code>visit</code>
     * method passing a reference to <code>this</code>.
     * 
     * @param visitor The visitor. Will never be <code>null</code>.
     */
    protected abstract void doAccept(TreeViewNodeVisitor visitor);
    
    /**
     * Tells if the children of this node are {@link TreeViewNode}s.
     * 
     * @return <code>true</code> if there's at least one {@link TreeViewNode} 
     *          child, <code>false</code> otherwise.
     */
    public abstract boolean containsImages();
    
}
