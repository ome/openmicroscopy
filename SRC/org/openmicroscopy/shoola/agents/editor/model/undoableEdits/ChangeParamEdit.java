 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.ChangeParamEdit 
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
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;

/** 
 * This is an {@link AbstractUndoableEdit} subclass that changes the 
 * Parameter of field for a new Parameter. eg Change a Check-box to a 
 * Number parameter, OR delete a parameter (newParam == null).
 * References to the old and new Parameters are kept, to allow undo & redo.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ChangeParamEdit 
	extends UndoableTreeEdit 
{
	/**
	 * The node that contains the {@link IField} that is being edited
	 */
	private TreeNode 		node;
	
	/**
	 * The field that is being edited (change parameter). 
	 */
	private IField 			field;
	
	/**
	 * The old parameter (null if none is being removed from the field)
	 */
	private IFieldContent 	newParam;
	
	/**
	 * The new Parameter (null if not adding a parameter)
	 */
	private IFieldContent 	oldParam;
	
	/**
	 * Need to copy name of parameter from old to new. 
	 */
	private String 			oldParamName;
	
	/**
	 * The index of the parameter to change. 
	 */
	private int 			paramIndex;
	
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
		tree.startEditingAtPath(new TreePath(dmtNode.getPath()));
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
	
	
	/**
	 * Creates an instance.
	 * 
	 * @param newParam		The new parameter to add
	 * @param oldParam		The old parameter to replace
	 * @param field			The field to change parameters
	 * @param tree			The tree in which the field exists.
	 * @param node			The treeNode in which the field exists.
	 */
	public ChangeParamEdit(IParam newParam, IParam oldParam, IField field, 
			JTree tree, TreeNode node) 
	{
		super(tree);
		
		this.field = field;
		
		this.newParam = newParam;
		
		this.node = node;
		
		this.oldParam = oldParam;
		
		doEdit();
	}
	
	
	/**
	 * Creates an instance.
	 * 
	 * @param newParam		The new parameter to add
	 * @param field			The field to change parameters
	 * @param index			The index of the parameter to change
	 * @param tree			The tree in which the field exists.
	 * @param node			The treeNode in which the field exists.
	 */
	public ChangeParamEdit(IParam newParam, IField field, int index, 
			JTree tree, TreeNode node) 
	{
		super(tree);
		
		this.field = field;
		
		this.newParam = newParam;
		
		this.paramIndex = index;
		
		this.node = node;
		
		if (field.getContentCount() > paramIndex) {
			oldParam = field.getContentAt(paramIndex);
		} else {
			throw new RuntimeException("ChangeParam edit index out of bounds");
		}
		
		doEdit();
	}

	/**
	 * Checks that field is not null. 
	 * 
	 * @return		false if field is null
	 */
	public boolean canDo() {
		
		if (field == null) return false;
		
		return true;
	}

	/**
	 * Implemented as specified by the {@link UndoableTreeEdit} interface.
	 * Performs the delete fields operation.
	 * Stores a list of the deleted fields for undo.
	 * 
	 * @see UndoableTreeEdit#doEdit()
	 */
	@Override
	public void doEdit() 
	{
		if (!canDo()) return;
		
		if (oldParam != null) {
			oldParamName = oldParam.getAttribute(AbstractParam.PARAM_NAME); 
			paramIndex = field.removeContent(oldParam);	
		}
		
		if (newParam != null) {
			newParam.setAttribute(AbstractParam.PARAM_NAME, oldParamName);
			field.addContent(paramIndex, newParam);
		}
		
		notifySelectStartEdit();
	}
	
	/**
	 * Implemented as specified by the {@link AbstractUndoableEdit}.
	 * Removes the {@link #newParam} and adds the {@link #oldParam}
	 * 
	 * @see AbstractUndoableEdit#undo()
	 */
	public void undo() {
		if (!canDo()) return;
		
		if (newParam != null) {
			field.removeContent(newParam);
		}
		if (oldParam != null) {
			field.addContent(paramIndex, oldParam);
		}
		
		notifySelectStartEdit();
	}
	
	/**
	 * Implemented as specified by the {@link AbstractUndoableEdit}.
	 * Removes the {@link #oldParam} and adds the {@link #newParam}
	 * 
	 * @see AbstractUndoableEdit#redo()
	 */
	public void redo() {
		if (!canDo()) return;
		
		if (oldParam != null) {
			field.removeContent(oldParam);
		}
		if (newParam != null) {
			field.addContent(paramIndex, newParam);
		}
		
		notifySelectStartEdit();
	}
	
	/**
	 * Presentation name.
	 * Implemented as specified by the {@link AbstractUndoableEdit}
	 * interface. 
	 * 
	 * @see AbstractUndoableEdit#getPresentationName()
	 */
	public String getPresentationName() {
		if (newParam == null) {
			return "Delete Paramter";
		}
		
		return "Change Parameter";
	}
	
	/**
	 * Can always undo.
	 * 
	 * Implemented as specified by the {@link AbstractUndoableEdit}
	 * interface. 
	 * 
	 * @see AbstractUndoableEdit#canUndo()
	 */
	 public boolean canUndo() {
	         return true;
	  }

	 /**
	 * Can always redo.
	 * 
	 * Implemented as specified by the {@link AbstractUndoableEdit}
	 * interface. 
	 * 
	 * @see AbstractUndoableEdit#canRedo()
	 */
	  public boolean canRedo() {
	         return true;
	  }

}
