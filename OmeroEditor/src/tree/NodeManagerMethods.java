 /*
 * tree.NodeManagerMethods 
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
package tree;

import java.util.ArrayList;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NodeManagerMethods {

	
	/**
	 * Duplicates the whole tree under old Node, building it anew with
	 * newNode as the root.
	 * 
	 * @param oldNode	The root of the existing tree to duplicate
	 * @param newNode	The root of the duplicated tree. 
	 */
	static void duplicateDataFieldTree(DataFieldNode oldNode, DataFieldNode newNode) {
		
		ArrayList<DataFieldNode> children = oldNode.getChildren();
		if (children.size() == 0) return;
			
			for (DataFieldNode copyThisChild: children){
			 
				DataFieldNode newChild = new DataFieldNode(copyThisChild);
				
				newNode.addChild(newChild);
				
				duplicateDataFieldTree(copyThisChild, newChild);
			}
	}

	/**
	 *  add the newNode as a child of parentNode at the specified index
	 */
	public static void addDataField(DataFieldNode newNode, DataFieldNode parentNode, int indexToInsert) {
		newNode.setParent(parentNode);
		parentNode.addChild(indexToInsert, newNode);
	}

	// copy and add new dataFields
	// used by import, paste, and duplicate functions
	public static void copyAndInsertDataFields(ArrayList<DataFieldNode> dataFieldNodes,DataFieldNode parentNode, int indexToInsert) {
		
		if (dataFieldNodes.isEmpty()) return;
		
		
		//remember the first node added, so all new nodes can be selected when done
		DataFieldNode firstNewNode = null;
		DataFieldNode newNode = null;
		
		for (int i=0; i< dataFieldNodes.size(); i++){
			
			newNode = new DataFieldNode(dataFieldNodes.get(i));
			duplicateDataFieldTree(dataFieldNodes.get(i), newNode);
			
			addDataField(newNode, parentNode, indexToInsert);	
			indexToInsert++;
			
			if (i == 0) firstNewNode = newNode;
		}
		
		newNode.nodeClicked(true);
		firstNewNode.nodeClicked(false);   // will select the range 
	}

	// don't copy DataFields, just insert the same ones
	public static void insertTheseDataFields(ArrayList<DataFieldNode> dataFieldNodes,DataFieldNode parentNode, int indexToInsert) {
		
		if (dataFieldNodes.isEmpty()) return;
		
		//remember the first node added, so all new nodes can be selected when done
		DataFieldNode firstNewNode = null;
		DataFieldNode newNode = null;
		
		for (int i=0; i< dataFieldNodes.size(); i++){
			
			newNode = dataFieldNodes.get(i);
			
			addDataField(newNode, parentNode, indexToInsert);	
			indexToInsert++;
			
			if (i == 0) firstNewNode = newNode;
		}
		
		newNode.nodeClicked(true);
		firstNewNode.nodeClicked(false);   // will select the range 
	}

	public static void demoteDataFields(ArrayList<DataFieldNode> fields) {
			
			if (fields.isEmpty()) return;
			
			// fields need to become children of their preceding sibling (if they have one)
			DataFieldNode firstNode = fields.get(0);
			int indexOfFirstSibling = firstNode.getMyIndexWithinSiblings();
			
			// if no preceding sibling, can't demote
			if (indexOfFirstSibling == 0) {
				throw (new NullPointerException("Can't demote because no preceding sibling"));
			}
			
			DataFieldNode parentNode = firstNode.getParentNode();
			DataFieldNode preceedingSiblingNode = parentNode.getChild(indexOfFirstSibling-1);
			
			// move nodes
			for (DataFieldNode highlightedField: fields) {
				preceedingSiblingNode.addChild(highlightedField);
			}
	//		 delete them from the end (reverse order)
			for (int i=fields.size()-1; i>=0; i--) {
				parentNode.removeChild(fields.get(i));
			}
		}

	public static void promoteDataFields(ArrayList<DataFieldNode> fields) {
		
		if (fields.isEmpty()) return;
		
		DataFieldNode node = fields.get(0);
		DataFieldNode parentNode = node.getParentNode();
		DataFieldNode grandParentNode = parentNode.getParentNode();
		// if parent is root (grandparent null) then can't promote
		if (grandParentNode == null) {
			throw (new NullPointerException("Can't promote because grandparent is null"));
		}
		
		// any fields that are children of the last to be promoted, 
		// must first become children of that node. 
		DataFieldNode lastNode = fields.get(fields.size()-1);
		DataFieldNode lastNodeParent = lastNode.getParentNode();
		
		int indexOfLast = lastNodeParent.indexOfChild(lastNode);
		int numChildren = lastNodeParent.getChildren().size();
		
		// copy children in correct order
		for (int i=indexOfLast+1; i< numChildren; i++) {
			DataFieldNode nodeToCopy = lastNodeParent.getChild(i);
			lastNode.addChild(nodeToCopy);
		}
		// delete them from the end (reverse order)
		for (int i=numChildren-1; i>indexOfLast; i--) {
			lastNodeParent.removeChild(lastNodeParent.getChild(i));
		}
		
		// loop backwards so that the top field is last added, next to parent
		for (int i=fields.size()-1; i >=0; i--) {
			NodeManagerMethods.promoteDataField(fields.get(i));
		}
		
	}

	// promotes a dataField to become a sibling of it's parent
	public static void promoteDataField(DataFieldNode node) {
		
		DataFieldNode parentNode = node.getParentNode();
		DataFieldNode grandParentNode = parentNode.getParentNode();
		
		// if parent is root (grandparent null) then can't promote
		// if (grandParentNode == null) return; 	catch any null pointer exception later
		
		int indexOfParent = grandParentNode.indexOfChild(parentNode);
		
		grandParentNode.addChild(indexOfParent + 1, node);	// adds after parent
		node.setParent(grandParentNode);
		parentNode.removeChild(node);
	}

	public static void moveFieldsUp(ArrayList<DataFieldNode> fields) throws IndexOutOfBoundsException {
		int numFields = fields.size();
	
		DataFieldNode firstNode = fields.get(0);
		int firstNodeIndex = firstNode.getMyIndexWithinSiblings();
		
		DataFieldNode parentNode = firstNode.getParentNode();
		DataFieldNode preceedingNode = parentNode.getChild(firstNodeIndex - 1);
		// add the preceding node after the last node
		parentNode.addChild(firstNodeIndex + numFields, preceedingNode);
		parentNode.removeChild(preceedingNode);
	}

	public static void moveFieldsDown(ArrayList<DataFieldNode> fields) throws IndexOutOfBoundsException {
		
		int numFields = fields.size();
	
		DataFieldNode lastNode = fields.get(numFields-1);
		DataFieldNode parentNode = lastNode.getParentNode();
		
		int lastNodeIndex = lastNode.getMyIndexWithinSiblings();
	
		DataFieldNode succeedingNode = parentNode.getChild(lastNodeIndex + 1);
		// add the succeeding node before the first node
		int indexToMoveTo = lastNodeIndex - numFields + 1;
		parentNode.addChild(indexToMoveTo, succeedingNode);
		// remove the succeeding node (now 1 more position down the list - after inserting above)
		parentNode.removeChild(lastNodeIndex + 2);
		
	}

	//	 delete the highlighted dataFields 
	public static void deleteDataFields(ArrayList<DataFieldNode> fields) {
		for (DataFieldNode node: fields) {
			NodeManagerMethods.deleteDataField(node);
		}
	}

	public static void deleteDataField(DataFieldNode node) {
		DataFieldNode parentNode = node.getParentNode();
		parentNode.removeChild(node);
	}

}
