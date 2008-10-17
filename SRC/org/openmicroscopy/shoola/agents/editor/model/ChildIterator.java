 /*
 * org.openmicroscopy.shoola.agents.editor.model.ChildIterator 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

import java.util.Iterator;

import javax.swing.tree.TreeNode;

//Third-party libraries

//Application-internal dependencies

/** 
 *  An {@link Iterator} for iterating through the children of a {@link TreeNode}
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ChildIterator 
	implements Iterator<TreeNode>
{
	/**
	 * A pointer to the current index. 
	 */
	private int 				index;
	
	/**
	 * The node that has the children we're iterating through
	 */
	private TreeNode 			treeNode;
	
	/**
	 * Creates an instance
	 * 
	 * @param parent	The node that has the children we're iterating through
	 */
	public ChildIterator(TreeNode parent) {
		treeNode = parent;
		index = 0;
	}

	/**
	  * Implemented as specified by the {@link Iterator} interface
	  */
	public boolean hasNext() {
		if (index < treeNode.getChildCount())
			return true;
		return false;
	}

	/**
	  * Implemented as specified by the {@link Iterator} interface
	  */
	public TreeNode next() {
		if (hasNext()) {
			TreeNode next = treeNode.getChildAt(index);
			index++;
			return next;
		}
		return null;
	}

	/**
	  * Implemented as specified by the {@link Iterator} interface
	  * Null implementation. Will throw {@link UnsupportedOperationException};
	  */
	public void remove() {
		throw new UnsupportedOperationException("Can't remove.");
	}

}
