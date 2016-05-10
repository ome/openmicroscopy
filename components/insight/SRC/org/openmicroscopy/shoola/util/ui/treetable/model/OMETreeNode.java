/*
 * org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.treetable.model;


//Java imports
import java.util.List;
import java.util.Stack;
import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

//Application-internal dependencies

/** 
 * A node of an <code>OMETreeTable</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class OMETreeNode
	extends DefaultMutableTreeTableNode
{	
	
	/** Flag used to control if the node is expanded in the view.*/
	private boolean expanded;

    /** Creates a new instance. */
	public OMETreeNode()
	{
		this(null);
	}
	
	/**
	 * Creates a new instance with user object.
	 * 
	 * @param object The user object.
	 */
	public OMETreeNode(Object object)
	{
		this(object, true);
	}

	/**
	 * Creates instance with userObject object and option to allow children.
	 * 
	 * @param object 		The user object.
	 * @param allowChildren Pass <code>true</code> to allow children,
	 * 						<code>false</code> otherwise.
	 */
	public OMETreeNode(Object object, boolean allowChildren)
	{
		super(object, allowChildren);
		setExpanded(false);
	}
	
	/**
	 * Returns <code>true</code> if the node is expanded, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isExpanded() { return expanded; }
	
    /**
     * Return <code>true</code> if this node is a root node
     * 
     * @return See above.
     */
    public boolean isRoot() {
        return getPath().getPathCount() == 1;
    }
	
	/**
	 * Sets the current node to expanded.
	 * 
	 * @param expanded 	Pass <code>true</code> if the node is expanded, 
	 * 					<code>false</code> otherwise.
	 */
	public void setExpanded(boolean expanded) { this.expanded = expanded; }
	
	/**
	 * Returns the parent of the current Node.
	 * 
	 * @return See above.
	 */
	public OMETreeNode getParent()
	{
		return (OMETreeNode) super.getParent();
	}
	
	/**
	 * Returns the path of the node in the model.
	 * 
	 * @return See above.
	 */
	public TreePath getPath()
	{
		OMETreeNode node = this;
		Stack<OMETreeNode> stack = new Stack<OMETreeNode>();
		while (node != null) {
			stack.push(node);
			node = node.getParent();
		}
		Object[] pathList = new Object[stack.size()];
		int count = 0;
		while (!stack.empty())
			pathList[count++] = stack.pop();
		return new TreePath(pathList);
	}

	/** 
	 * Returns the child list of the node. Used to allow for(iterator)
	 * 
	 * @return See above.
	 */
	public List<MutableTreeTableNode> getChildList() { return children; }
	
	/**
	 * Overridden so that the node cannot be edited.
	 * @see DefaultMutableTreeTableNode#isEditable(int)
	 */
	public boolean isEditable(int column) { return false; }
	
}


