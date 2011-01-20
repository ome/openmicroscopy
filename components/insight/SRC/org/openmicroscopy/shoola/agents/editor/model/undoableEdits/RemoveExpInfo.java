 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.RemoveExpInfo 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.ExperimentInfo;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.Note;
import org.openmicroscopy.shoola.agents.editor.model.ProtocolRootField;
import org.openmicroscopy.shoola.agents.editor.model.TreeIterator;

/** 
 * This Edit Removes the Experimental Info from an experiment to create a 
 * protocol. It should also remove all the Step Notes, which are only 
 * relevant to an experiment. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RemoveExpInfo 
	extends AbstractUndoableEdit 
	implements TreeEdit
{
	/**
	 * A reference to the root {@link IField} that the new {@link ExperimentInfo}
	 * is being removed from
	 */
	private ProtocolRootField 			field;
	
	/**
	 * A reference to the new {@link ExperimentInfo}, being removed.
	 */
	private IAttributes 				expInfo;
	
	/**
	 * The root node (that contains the field being edited).
	 * This is used to notify the TreeModel that nodeChanged()
	 */
	private DefaultMutableTreeNode 		node;
	
	/**
	 * The model we are editing the root node of. Used to notify changes. 
	 */
	DefaultTreeModel 					treeModel;
	
	/**
	 * A map of all the step notes we are deleting, according to their step 
	 */
	private	HashMap<IField, List<Note>> 	stepNotes;
	
	/**
	 * Iterate through the tree model and add all the step notes to the
	 * map. 
	 */
	private void getStepNotes() 
	{
		// make a map of all the step notes and the fields they come from
		stepNotes  = new HashMap<IField, List<Note>>();
		
		TreeNode tn;
		IField f;
		Object userOb;
		DefaultMutableTreeNode node;
		
		Object r = treeModel.getRoot();
		if (! (r instanceof TreeNode)) 		return;
		TreeNode root = (TreeNode)r;
		
		Iterator<TreeNode> iterator = new TreeIterator(root);
		
		List<Note> notes;
		
		while (iterator.hasNext()) {
			tn = iterator.next();
			if (!(tn instanceof DefaultMutableTreeNode)) continue;
			node = (DefaultMutableTreeNode)tn;
			userOb = node.getUserObject();
			if (!(userOb instanceof IField)) continue;
			f = (IField)userOb;
			
			int noteCount = f.getNoteCount();
			if (noteCount == 0) continue;
			
			// a list of the notes from this field/step
			notes = new ArrayList<Note>();
			for (int i=0; i<noteCount; i++) {
				notes.add(f.getNoteAt(i));
			}
			stepNotes.put(f, notes);
		}
	}
	
	/**
	 * Creates an instance and performs the add.
	 * 
	 * @param tree		The tree that holds the model to edit
	 */
	public RemoveExpInfo(JTree tree) {
		setTree(tree);
		doEdit();
	}
	
	/**
	 * Creates an instance and performs the add.
	 * 
	 * @param tree		The model to edit
	 */
	public RemoveExpInfo(TreeModel treeModel) {
		this.treeModel = (DefaultTreeModel)treeModel;
		doEdit();
	}
	
	public void doEdit() {
		if (treeModel == null)	return;
		node = (DefaultMutableTreeNode)treeModel.getRoot();
		
		Object ob = node.getUserObject();
		if (! (ob instanceof ProtocolRootField))	return;
		field = (ProtocolRootField)ob;
		
		expInfo = field.getExpInfo();
		
		getStepNotes();
		
		redo();
	}
	
	public void undo() {
		// add all notes to their fields. 
		Iterator<IField> i = stepNotes.keySet().iterator();
		List<Note> notes;
		IField f;
		while(i.hasNext()) {
			f = i.next();
			notes = stepNotes.get(f);
			for (Note note : notes) {
				f.addNote(note);
			}
		}
		
		field.setExpInfo(expInfo);
		notifyNodeChanged();
	}
	
	public void redo() {
		// remove all notes from their fields. 
		Iterator<IField> i = stepNotes.keySet().iterator();
		List<Note> notes;
		IField f;
		while(i.hasNext()) {
			f = i.next();
			notes = stepNotes.get(f);
			for (Note note : notes) {
				f.removeNote(note);
			}
		}
			
		field.setExpInfo(null);
		notifyNodeChanged();
	}
	
	public boolean canUndo() {
		return true;
	}
	
	public boolean canRedo() {
		return true;
	}
	
	/**
	 * Overrides this method in {@link AbstractUndoableEdit} to return
	 * "Experiment Info"
	 */
	 public String getPresentationName() 
	 {
		 return "Delete Experiment Info";
	 }
	
	/**
	 * This notifies all listeners to the TreeModel of a change to the node
	 * in which the attribute has been edited. 
	 * This will update any JTrees that are currently displaying the model. 
	 */
	private void notifyNodeChanged() 
	{
		if (treeModel != null) {
			treeModel.nodeChanged(node);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeEdit} interface.
	 * 
	 * @see TreeEdit#setTree(JTree)
	 */
	public void setTree(JTree tree) {
		if (tree != null)
			treeModel = (DefaultTreeModel)tree.getModel();
	}
}
