 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddExpInfoEdit 
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
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.ExperimentInfo;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.ProtocolRootField;

/** 
 * This edit Adds experimental info to the protocol root node/step
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AddExpInfoEdit 
	extends AbstractUndoableEdit
	implements TreeEdit
{
	/**
	 * A reference to the root {@link IField} that the new {@link ExperimentInfo}
	 * is being added to.
	 */
	private ProtocolRootField 			field;
	
	/**
	 * A reference to the new {@link ExperimentInfo}, being added.
	 */
	private IAttributes 				expInfo;
	
	/**
	 * A reference to the old {@link ExperimentInfo}, being replaced.
	 */
	private IAttributes 				oldExpInfo;
	
	/**
	 * The root node (that contains the field being edited).
	 * This is used to notify the TreeModel that nodeChanged()
	 */
	private DefaultMutableTreeNode 		node;
	
	/**
	 * 
	 */
	DefaultTreeModel 					treeModel;
	
	/**
	 * Creates an instance and performs the add.
	 * 
	 * @param field		The field/step to add the data-reference to.
	 * @param tree		The tree that contains the field. Needed to notify edits
	 * @param node		The node in the tree that contains the field to edit.
	 */
	public AddExpInfoEdit(JTree tree) {
		setTree(tree);
	}
	
	public void doEdit() {
		if (treeModel == null)	return;
		node = (DefaultMutableTreeNode)treeModel.getRoot();
		
		Object ob = node.getUserObject();
		if (! (ob instanceof ProtocolRootField))	return;
		field = (ProtocolRootField)ob;
		
		oldExpInfo = field.getExpInfo();
		expInfo = new ExperimentInfo();
		
		redo();
	}
	
	public void undo() {
		field.setExpInfo(oldExpInfo);
		notifyNodeChanged();
	}
	
	public void redo() {
		field.setExpInfo(expInfo);
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
	 * "Add Experiment Info"
	 */
	 public String getPresentationName() 
	 {
		 return "Add Experiment Info";
	 }
	
	/**
	 * This notifies all listeners to the TreeModel of a change to the node
	 * in which the attribute has been edited. 
	 * This will update any JTrees that are currently displaying the model,
	 * including the Enabled status of the Editable Tree (main display).  
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
