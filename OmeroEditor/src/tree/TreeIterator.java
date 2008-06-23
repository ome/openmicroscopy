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

package tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

class TreeIterator implements Iterator<DataFieldNode> { 
	 Stack<Iterator<DataFieldNode>> stack = new Stack<Iterator<DataFieldNode>>(); 
	 
	 public TreeIterator(DataFieldNode dataFieldNode) { 
	  List<DataFieldNode> list = new ArrayList<DataFieldNode>(); 
	  list.add(dataFieldNode); 
	  stack.push(list.iterator()); 
	 } 
	 
	 
	 public boolean hasNext() { 
	  while (!stack.isEmpty()) { 
	   Iterator<DataFieldNode> iter = stack.peek(); 
	   if (iter.hasNext()) 
	    return true; 
	   stack.pop(); 
	  } 
	  return false; 
	 } 
	 
	 
	 public DataFieldNode next() { 
	  if (!hasNext()) // in case next is called without hasNext 
	   return null; 
	  Iterator<DataFieldNode> iter = stack.peek(); 
	  DataFieldNode node = iter.next(); 
	  stack.push(node.childIterator()); 
	  return node; 
	 } 
	 
	 public void remove() {
		 throw new UnsupportedOperationException();
	 }
	} 

