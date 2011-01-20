 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddStepNoteEdit 
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

import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.Note;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;

/** 
 * An Undoable Edit for adding (or deleting) a Note to a Step, 
 * as a comment on an experiment.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AddStepNoteEdit 
	extends AbstractUndoableEdit {

	/**
	 * A reference to the {@link IField} that the new {@link Note} is
	 * being added to.
	 */
	private IField 				field;
	
	/**
	 * A reference to the new {@link Note}, being added.
	 */
	private Note 				newNote;
	
	/**
	 * A reference to the new {@link Note}, being replaced (null if adding note)
	 */
	private Note 				oldNote;
	
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
	 * The index that the new content was added. 
	 */
	private int 				indexOfNote;
	

	/**
	 * Creates an instance and performs the edit, 
	 * creating and adding the note to field
	 * 
	 * @param field		The field to add a new note to.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public AddStepNoteEdit(IField field, JTree tree, TreeNode node) {
		
		this.field = field;
		this.newNote = new Note();
		this.tree = tree;
		this.node = node;

		indexOfNote = field.getNoteCount();	// always add at end of list
		redo();
	}
	
	/**
	 * Creates an instance and performs the edit, 
	 * DELETING the specified Note at the specified index. 
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param note		The note to delete. 
	 * @param tree		The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public AddStepNoteEdit(IField field, Note note, 
			JTree tree, TreeNode node) {
		
		this.field = field;
		this.newNote = null;
		this.oldNote = note;
		this.tree = tree;
		this.node = node;
		
		redo();
	}
	
	public void undo() {
		// undo of add
		if (newNote != null) {
			field.removeNote(newNote);
		} else {
			// undo of delete
			field.addNote(oldNote, indexOfNote);
		}
		notifySelectStartEdit();
	}
	
	public void redo() {
		// redo of add
		if (newNote != null)
			field.addNote(newNote, indexOfNote);
		else
			// redo delete
			indexOfNote = field.removeNote(oldNote);
		
		notifySelectStartEdit();
	}
	
	public boolean canUndo() {
		return true;
	}
	
	public boolean canRedo() {
		return true;
	}
	
	
	public String getPresentationName() {
		if (newNote == null)
		return "Delete Note";
		
		return "Add Note";
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
