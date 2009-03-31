 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.DeleteFields 
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
package org.openmicroscopy.shoola.agents.editor.model.undoableEdits;

//Java imports

import java.util.ArrayList;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;

/** 
 * This is the UndoableEdit for deleting fields from a JTree.
 * The constructor takes an instance of the JTree to be edited. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DeleteFieldsEdit 
	extends UndoableTreeEdit 
{

	/**
	 * A list of the fields that are deleted.
	 * A reference to these fields must be kept in order that they 
	 * can be undeleted. 
	 */
	private ArrayList<MutableTreeNode> 		deletedFields;
	
	/**
	 * The child index of the first deleted field. 
	 * This is required so that when fields are undeleted, they can be 
	 * added back to their parent in the right order, with respect to
	 * siblings that weren't deleted.  
	 */
	private int 							indexOfFirstHighlightedField;
	
	/**
	 * The parent of the deleted fields.
	 * This is needed so that deleted fields can be added back to this parent.
	 */
	private DefaultMutableTreeNode 			parentNode;

	/**
	 * Creates an instance of this class. 
	 * If JTree tree is null, it is necessary to call 
	 * setTree(JTree) before this Edit can be used. 
	 * 
	 * @param tree		The JTree to edit (delete fields)
	 */
	public DeleteFieldsEdit(JTree tree) 
	{
		super(tree);
		
		deletedFields = new ArrayList<MutableTreeNode>();
	}

	/**
	 * Can only delete fields if the number of selected fields is not zero
	 * and the selected field is not the root. 
	 */
	public static boolean canDo(TreePath[] paths) {
		
		if (paths == null) return false;
		if (paths.length == 0)
			return false;
		/*
		 * Need to check the root node is not selected
		 */
		TreePath p = paths[0];
		DefaultMutableTreeNode selectedNode = 
						(DefaultMutableTreeNode)p.getLastPathComponent();
		// if the root is selected, can't delete it. Return false
		return(! selectedNode.isRoot());
	}

	/**
	 * Performs the delete fields operation.
	 * Stores a list of the deleted fields for undo.
	 */
	@Override
	public void doEdit() 
	{
		if (tree == null)	return;
		TreePath[] selectedPaths = tree.getSelectionPaths();
		if (! canDo(selectedPaths) ) return;
		
		if (selectedPaths.length > 0) {
			DefaultMutableTreeNode firstField = (DefaultMutableTreeNode)
					selectedPaths[0].getLastPathComponent();
			indexOfFirstHighlightedField = firstField.getParent().getIndex(firstField);
			
			parentNode = (DefaultMutableTreeNode)firstField.getParent();
			
			/*
			 * Keep a list of references to the Nodes to be deleted
			 */
			DefaultMutableTreeNode currentNode;
			MutableTreeNode parent;
			for (int i=0; i<selectedPaths.length; i++) {
				currentNode = (DefaultMutableTreeNode)
						selectedPaths[i].getLastPathComponent();
				parent = (MutableTreeNode)(currentNode.getParent());
	            if (parent != null) {
	                treeModel.removeNodeFromParent(currentNode);
	            }
				deletedFields.add(currentNode);
			}
		}
		
	}
	
	/**
	 * Adds the deleted fields back to their parent, then selects them,
	 * so they are highlighted. 
	 */
	public void undo() {
		TreeModelMethods.insertNodesInto(treeModel, deletedFields, parentNode,
				indexOfFirstHighlightedField);
		
		TreeModelMethods.selectNodes(deletedFields, tree);
	}
	
	/**
	 * Re-deletes the fields. 
	 * No need to change selection, as the JTree will remove them from 
	 * the selected paths. 
	 */
	public void redo() {
		TreeModelMethods.removeNodesFromParent(treeModel, deletedFields);
		tree.clearSelection();
	}
	
	/**
	 * Presentation name is "Delete Fields"
	 */
	public String getPresentationName() {
		     return "Delete Fields";
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
