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
import java.util.Stack;
import java.util.Vector;

import javax.swing.tree.TreePath;

//Third-party libraries
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

//Application-internal dependencies

/** 
 * 
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
	 /** is the node expanded in the view.*/
	private boolean expanded;

    /**
     * Create instance.
     */
	public OMETreeNode()
	{
		this(null);
	}
	
	/**
	 * Create instance with userobject-- object.
	 * @param object see above.
	 */
	public OMETreeNode(Object object)
	{
		this(object, true);
	}

	/**
	 * Create instance with userObject object and option to allow children.
	 * @param object see above.
	 * @param allowChildren true if object can allowChildren.
	 */
	public OMETreeNode(Object object, boolean allowChildren)
	{
		super(object, allowChildren);
		setExpanded(false);
	}
	
	/**
	 * Is the node expanded in the view.
	 * @return see above.
	 */
	public boolean isExpanded()
	{
		return expanded;
	}
	
	/**
	 * Set the current node to expanded.
	 * @param expanded true if the node is expanded.
	 */
	public void setExpanded(boolean expanded)
	{
		this.expanded = expanded;
	}
	
	/**
	 * Get the parent of the current Node.
	 * @return see above.
	 */
	public OMETreeNode getParent()
	{
		return (OMETreeNode)super.getParent();
	}
	
	/**
	 * Get the path of the node in the model.
	 * @return see above.
	 */
	public TreePath getPath()
	{
		OMETreeNode node = this;
		Stack<OMETreeNode> stack = new Stack<OMETreeNode>();
		while(node != null)
		{
			stack.push(node);
			node = node.getParent();
		}
		Object[] pathList = new Object[stack.size()];
		int count = 0;
		while(!stack.empty())
			pathList[count++] = stack.pop();
		return new TreePath(pathList);
	}
	

	/**
	 * Overrides {@link DefaultMutableTreeTableNode#isEditable(int)}
	 */
	public boolean isEditable(int column)
	{
		return false;
	}
	
	/** 
	 * Get the child list of the node. Used to allow for(iterator)
	 * @return see above.
	 */
	public Vector<MutableTreeTableNode> getChildList()
	{
		return children;
	}
}


