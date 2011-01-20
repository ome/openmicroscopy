 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddFieldEdit 
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

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.FieldNode;
import org.openmicroscopy.shoola.agents.editor.model.IField;

/** 
* This is the UndoableEdit for adding fields to a JTree.
* The constructor takes an instance of the JTree to be edited.
* Fields will be added after the last selected field of JTree,
* or as the last child of root, if none selected. 
*
* @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since OME3.0
*/
public class AddFieldEdit 
	extends UndoableTreeEdit
{
	/**
	 * The new Field/Step object to add. 
	 * This can be specified when this class is created, although it will
	 * not be used until the {@link #doEdit()} method is called. 
	 */
	IField 								field;
	
	/**
	 * The new node.
	 * This is needed so that when undo() has removed new field, it can 
	 * be added back to the parent.
	 */
	private DefaultMutableTreeNode 		newNode;
	
	/**
	 * The child index of the last selected field (before adding new field). 
	 * This is required so that when undo() has removed new field, it can 
	 * be added back at the right index.
	 */
	private int 						indexOfNewField;
	
	/**
	 * The parent of the new fields.
	 * This is needed so that when undo() has removed new field, it can 
	 * be added back to the right parent.
	 */
	private DefaultMutableTreeNode 		parentNode;

	/**
	 * Creates an instance of this class. 
	 * If JTree tree is null, it is necessary to call 
	 * setTree(JTree) before this Edit can be used. 
	 * 
	 * @param tree		The JTree to edit (add fields)
	 */
	public AddFieldEdit(JTree tree) {
		super(tree);
		
		 // Create a new field
		 field = new Field();
	}

	/**
	 * Can always add fields (doesn't depend on selected fields)
	 */
	public static boolean canDo(TreePath[] paths)
	{	
		return true;
	}

	/**
	 * Performs the add field operation.
	 * Sets references to the new node, it's parent and index. 
	 */
	@Override
	public void doEdit() 
	{	
		if (tree == null)	return;
		TreePath[] selectedPaths = tree.getSelectionPaths();
		if (! canDo(selectedPaths) ) return;
		
		if (field == null) {
			field = new Field();
		}
		
		 // Place this in a new Node
		newNode = new FieldNode(field);
		
		if ((selectedPaths != null) && (selectedPaths.length > 0)) {
			// Get the last selected node...
			DefaultMutableTreeNode lastField = (DefaultMutableTreeNode)
					selectedPaths[selectedPaths.length-1].getLastPathComponent();
			
			// if this is root, add as last child 
			if (lastField.isRoot()) {
				parentNode = (DefaultMutableTreeNode)treeModel.getRoot();
				indexOfNewField = parentNode.getChildCount();
			} else {
			
				int indexOfLastHighlightedField = lastField.getParent().
					getIndex(lastField);
				//...otherwise add new field after the last selected field
				indexOfNewField = indexOfLastHighlightedField +1;
				parentNode = (DefaultMutableTreeNode)lastField.getParent();
			}
		} 
		else {
			// If no fields selected, want to add as the last child of root
			parentNode = (DefaultMutableTreeNode)treeModel.getRoot();
			indexOfNewField = parentNode.getChildCount();
		}
		
		treeModel.insertNodeInto(newNode, parentNode, indexOfNewField);
		// Select the new node.
		TreePath path = new TreePath(newNode.getPath());
		tree.setSelectionPath(path);
		
	}
	
	/**
	 * Deletes the new field
	 */
	public void undo() {
		treeModel.removeNodeFromParent(newNode);
	}
	
	/**
	 * Re-inserts the new Node  
	 */
	public void redo() {
		treeModel.insertNodeInto(newNode, parentNode, indexOfNewField);
		// Select the new node.
		TreePath path = new TreePath(newNode.getPath());
		tree.setSelectionPath(path);
	}
	
	/**
	 * Presentation name is "Add Step"
	 */
	public String getPresentationName() {
		     return "Add Step";
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
