 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddDataRefEdit 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.DataReference;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.Note;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;

/** 
 * This undoable Edit adds a new {@link DataReference} to the field/step.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AddDataRefEdit 
	extends AbstractUndoableEdit
{
	/**
	 * A reference to the {@link IField} that the new {@link DataReference} is
	 * being added to.
	 */
	private IField 				field;
	
	/**
	 * A reference to the new {@link DataReference}, being added.
	 */
	private DataReference 		newDataRef;
	
	/**
	 * A reference to the new {@link DataReference}, being replaced (if any)
	 */
	private DataReference 		oldDataRef;
	
	/**
	 * The {@link JTree} which displays the field being edited. 
	 * If this is not null,
	 * it's {@link TreeModel} will be notified of changes to the node, and 
	 * the JTree selection path will be set to the edited node. 
	 */
	private JTree 				tree; 
	
	/**
	 * The node that contains the field being edited. 
	 * This is used to notify the TreeModel that nodeChanged() and to 
	 * set the selected node in the JTree after editing. 
	 */
	private TreeNode 			node;
	
	/**
	 * The index that the new DataReference was added. 
	 */
	private int 				indexOfRef;
	
	/**
	 * Creates an instance and performs the add.
	 * 
	 * @param field		The field/step to add the data-reference to.
	 * @param tree		The tree that contains the field. Needed to notify edits
	 * @param node		The node in the tree that contains the field to edit.
	 */
	public AddDataRefEdit(IField field, JTree tree, TreeNode node) {
		
		this.field = field;
		this.tree = tree;
		this.node = node;
		
		indexOfRef = field.getDataRefCount();
		
		newDataRef = new DataReference();
		
		redo();
	}
	
	/**
	 * Creates an instance and performs the edit, 
	 * DELETING the specified Data Reference 
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param dataRef	The data reference to delete
	 * @param tree		The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public AddDataRefEdit(IField field, DataReference dataRef, 
			JTree tree, TreeNode node) {
		
		this.field = field;
		this.newDataRef = null;
		this.oldDataRef = dataRef;
		this.tree = tree;
		this.node = node;
		
		redo();
	}
	
	public void undo() {
		// undo of add
		if (newDataRef != null) {
			field.removeDataRef(newDataRef);
		} else {
			// undo of delete
			field.addDataRef(indexOfRef, oldDataRef);
		}
		notifySelectStartEdit();
	}
	
	public void redo() {
		// redo of add
		if (newDataRef != null) {
			field.addDataRef(indexOfRef, newDataRef);
		} else {
			// redo delete
			indexOfRef = field.removeDataRef(oldDataRef);
		}
		notifySelectStartEdit();
	}
	
	public boolean canUndo() {
		return true;
	}
	
	public boolean canRedo() {
		return true;
	}
	
	/**
	 * Overrides this method in {@link AbstractUndoableEdit} to return
	 * "Data Reference"
	 */
	 public String getPresentationName() 
	 {
		 return "Data Reference";
	 }
	
	/**
	 * This should be called after undo or redo.
	 * It notifies listeners to the treeModel that a change has been made,
	 * then selects the edited node in the JTree specified in the constructor. 
	 * Finally, startEditingAtPath(path) to the edited node is called on 
	 * this JTree. This means that after an undo/redo, the edited node is 
	 * "active", so that a user can edit directly, rather than needing a 
	 * click before editing. 
	 */
	private void notifySelectStartEdit() 
	{
		notifyNodeChanged();
		TreeModelMethods.selectNode(node, tree);
		
		DefaultMutableTreeNode dmtNode = (DefaultMutableTreeNode)node;
		TreePath path = new TreePath(dmtNode.getPath());
		if (tree != null)
			tree.startEditingAtPath(path);
	}
	
	/**
	 * This notifies all listeners to the TreeModel of a change to the node
	 * in which the attribute has been edited. 
	 * This will update any JTrees that are currently displaying the model. 
	 */
	private void notifyNodeChanged() 
	{
		if ((tree != null) && (node != null)) {
			DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
			treeModel.nodeChanged(node);
		}
	}
}
