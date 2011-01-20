/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

//Third-party libraries

//Application-internal dependencies

import javax.swing.tree.TreeNode;

/** 
* An {@link Iterator} for iterating through the {@link TreeNode}s of a 
* {@link TreeModel}.
* Returns nodes in the same order as they would appear in a JTree
* (top to bottom).
* 
* @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
public class TreeIterator 
	implements Iterator<TreeNode> 
{ 
	/**
	 * A stack to store recursively generated instances of this class. 
	 */
	 private Stack<Iterator<TreeNode>> stack; 
	 
	 /**
	  * Creates an instance
	  * 
	  * @param treeNode		The node to start iterating
	  */
	 public TreeIterator(TreeNode treeNode) { 
		 stack = new Stack<Iterator<TreeNode>>(); 
		 List<TreeNode> list = new ArrayList<TreeNode>(); 
		 list.add(treeNode); 
		 stack.push(list.iterator()); 
	 } 
	 
	 /**
	  * Implemented as specified by the {@link Iterator} interface
	  */
	 public boolean hasNext() { 
	  while (!stack.isEmpty()) { 
	   Iterator<TreeNode> iter = stack.peek(); 
	   if (iter.hasNext()) 
	    return true; 
	   stack.pop(); 
	  } 
	  return false; 
	 } 
	 
	 /**
	  * Implemented as specified by the {@link Iterator} interface
	  */
	 public TreeNode next() { 
	  if (!hasNext()) // in case next is called without hasNext 
	   return null; 
	  Iterator<TreeNode> iter = stack.peek(); 
	  TreeNode node = iter.next(); 
	  stack.push(new ChildIterator(node)); 
	  return node; 
	 } 
	 
	 /**
	  * Implemented as specified by the {@link Iterator} interface
	  * Null implementation. Will throw {@link UnsupportedOperationException};
	  */
	 public void remove() {
		 throw new UnsupportedOperationException();
	 }
	} 

