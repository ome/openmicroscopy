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

