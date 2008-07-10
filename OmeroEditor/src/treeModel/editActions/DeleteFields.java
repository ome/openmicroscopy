 /*
 * treeModel.editActions.DeleteFields 
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
package treeModel.editActions;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;


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
public class DeleteFields 
extends AbstractUndoableEdit {
	
	ArrayList<MutableTreeNode> deletedFields;
	int indexOfFirstHighlightedField;
	DefaultMutableTreeNode parentNode;
	
	DefaultTreeModel treeModel;
	
	public DeleteFields (DefaultTreeModel treeModel, TreePath[] selectedPaths) {
		
		this.treeModel = treeModel;
		
		deletedFields = new ArrayList<MutableTreeNode>();
		
		if (selectedPaths.length > 0) {
			DefaultMutableTreeNode firstField = (DefaultMutableTreeNode)
					selectedPaths[0].getLastPathComponent();
			indexOfFirstHighlightedField = firstField.getParent().getIndex(firstField);
			
			parentNode = (DefaultMutableTreeNode)firstField.getParent();
			
			/*
			 * Keep a list of references to the Nodes to be deleted
			 */
			for (int i=0; i<selectedPaths.length; i++) {
				DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
						selectedPaths[i].getLastPathComponent();
				MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
	            if (parent != null) {
	                treeModel.removeNodeFromParent(currentNode);
	            }
				deletedFields.add(currentNode);
			}
		}
		
	}
	
	public static boolean canDo(TreePath[] selectedPaths) { 
		return (selectedPaths.length > 0);
	}
	
	public void undo() {
		TreeModelMethods.insertNodesInto(treeModel, deletedFields, parentNode,
				indexOfFirstHighlightedField);
		}
	public void redo() {
		TreeModelMethods.removeNodesFromParent(treeModel, deletedFields);
		
		System.out.println("EditDeleteField redo()");
	}
	
	public String getPresentationName() {
		     return "Delete Fields";
	}
	
	 public boolean canUndo() {
	         return true;
	  }

	  public boolean canRedo() {
	         return true;
	  }
}
