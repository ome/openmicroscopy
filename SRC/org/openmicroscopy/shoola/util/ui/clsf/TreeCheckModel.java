/*
 * org.openmicroscopy.shoola.util.ui.clsf.TreeCheckModel
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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

//Third-party libraries

//Application-internal dependencies

/** 
 * A simple tree data model that uses {@link TreeCheckNode}s.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class TreeCheckModel
    extends DefaultTreeModel
{

    /**
     * Creates a new instance.
     * 
     * @param root The root node of the tree. 
     */
    public TreeCheckModel(TreeCheckNode root)
    {
        super(root);
    }

    /**
     * Overriden to make sure that the inserted nodes are instance of 
     * <code>TreeCheckNode</code>.
     * @see DefaultTreeModel#insertNodeInto(MutableTreeNode, MutableTreeNode,
     *                                      int)
     */
    public void insertNodeInto(MutableTreeNode newChild,
                                MutableTreeNode parent, int index)
    {
        if (!(newChild instanceof TreeCheckNode))
            throw new IllegalArgumentException("Node must be an instance of " +
                    "TreeCheckNode");
        if (!(parent instanceof TreeCheckNode))
            throw new IllegalArgumentException("Node must be an instance of " +
                    "TreeCheckNode");
        super.insertNodeInto(newChild, parent, index);
    }
    
}
