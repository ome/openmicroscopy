 /*
 * treeModel.undoableTreeEdits.DuplicateFieldsEdit 
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
package treeModel.undoableTreeEdits;


//Java imports

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import fields.Field;

//Third-party libraries

//Application-internal dependencies

/** 
* This is the UndoableEdit for duplicating fields in a JTree.
* The constructor takes an instance of the JTree to be edited.
* Selected fields will be duplicated and added after the last selected 
* field of JTree.
*
* @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
public class DuplicateFieldsEdit 
	extends UndoableTreeEdit {

	/**
	 * The new nodes.
	 * This is needed so that when undo() has removed new field, it can 
	 * be added back to the parent.
	 */
	List<MutableTreeNode> newNodes;
	
	/**
	 * The child index of the last selected field (before adding new fields). 
	 * This is required so that when undo() has removed new fields, they can 
	 * be added back at the right index.
	 */
	int indexOfNewField;
	
	/**
	 * The parent of the new fields.
	 * This is needed so that when undo() has removed new fields, they can 
	 * be added back to the right parent.
	 */
	MutableTreeNode parentNode;

	/**
	 * Creates an instance of this class. 
	 * If JTree tree is null, it is necessary to call 
	 * setTree(JTree) before this Edit can be used. 
	 * 
	 * @param tree		The JTree to edit (duplicate fields)
	 */
	public DuplicateFieldsEdit(JTree tree) {
		super(tree);
		
		newNodes = new ArrayList<MutableTreeNode>();
	}

	/**
	 * Can duplicate fields as long as the JTree is not null (doesn't depend
	 * on selected fields) and at least one field is selected, 
	 * but not root! (can't duplicate root).
	 */
	@Override
	public boolean canDo() {
		
		if (tree == null) return false;
		
		if (tree.getSelectionCount() == 0)
			return false;
		/*
		 * Need to check the root node is not selected
		 */
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)
			tree.getLastSelectedPathComponent();
		// if the root is selected, can't delete it. Return false
		return(! selectedNode.isRoot());
		
	}

	/**
	 * Performs the duplicate fields operation.
	 * Sets references to the new nodes, their parent and index. 
	 */
	@Override
	public void doEdit() {
		
		if (! canDo() ) return;
		
		TreePath[] selectedPaths = tree.getSelectionPaths();
		
		/*
		 * Clone the selected nodes, adding them to a new list
		 */
		DefaultMutableTreeNode node;
		DefaultMutableTreeNode newNode;
		Field oldField;
		Object newField;
		for (int i=0; i<selectedPaths.length; i++) {
			node = (DefaultMutableTreeNode)selectedPaths[i].getLastPathComponent();
			oldField = (Field)node.getUserObject();
			
			newField = (Field)oldField.clone();
			newNode = new DefaultMutableTreeNode(newField);
			TreeModelMethods.duplicateNode(node, newNode);
			newNodes.add(newNode);
			
		}
		
		/*
		 * Get the last selected node...
		 */
		DefaultMutableTreeNode lastField = (DefaultMutableTreeNode)
				selectedPaths[selectedPaths.length-1].getLastPathComponent();
		
		int indexOfLastHighlightedField = lastField.getParent().
			getIndex(lastField);
		/*
		 * ...and add new field after the last selected field
		 */
		indexOfNewField = indexOfLastHighlightedField +1;
		parentNode = (MutableTreeNode)lastField.getParent();
			
		TreeModelMethods.insertNodesInto(treeModel, newNodes, 
				parentNode, indexOfNewField);
		
		/*
		 * Select the new nodes
		 */
		TreeModelMethods.selectNodes(newNodes, tree);
	}
	
	/**
	 * Deletes the new fields
	 */
	public void undo() {
		TreeModelMethods.removeNodesFromParent(treeModel, newNodes);
	}
	
	/**
	 * Re-inserts the new Node  
	 */
	public void redo() {
		TreeModelMethods.insertNodesInto(treeModel, newNodes, 
				parentNode, indexOfNewField);
		/*
		 * Select the new nodes.
		 */
		TreeModelMethods.selectNodes(newNodes, tree);
	}
	
	/**
	 * Presentation name is "Duplicate Fields"
	 */
	public String getPresentationName() {
		     return "Duplicate Fields";
	}
	
	/**
	 * Can always undo
	 */
	 public boolean canUndo() {
	         return true;
	  }

	 /**
	  * Can always redo
	  */
	  public boolean canRedo() {
	         return true;
	  }
}
