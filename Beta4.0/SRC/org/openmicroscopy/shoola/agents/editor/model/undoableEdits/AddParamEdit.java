 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddParamEdit 
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
import org.openmicroscopy.shoola.agents.editor.model.TextContent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;

/** 
 * This is an undoable edit for adding a Parameter to a Field. 
 * This occurs in the constructor. 
 * The class also keeps a reference to the JTree and node in which the
 * edit occurred, so that they can be highlighted / refreshed. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AddParamEdit 
	extends AbstractUndoableEdit {

	/**
	 * A reference to the {@link IField} that the new {@link IParam} is
	 * being added to.
	 */
	private IField 				field;
	
	/**
	 * A reference to the new {@link IParam}, being added.
	 */
	private IFieldContent 		param;
	
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
	private int 				indexOfParam;
	
	/**
	 * Initialises variables and does the adding edit. 
	 * @param field
	 * @param content
	 * @param index
	 * @param tree
	 * @param node
	 */
	private void initialise(IField field, IFieldContent content, int index, 
			JTree tree, TreeNode node) 
	{
		this.field = field;
		this.param = content;
		this.tree = tree;
		this.node = node;
		
		if ((index > 0) && (index <= field.getContentCount()))
			field.addContent(index, param);
		else 
			field.addContent(param);
		notifySelectStartEdit();
	}
	
	private void initialise(IField field, IFieldContent content, 
			JTree tree, TreeNode node) 
	{
		int index = field.getContentCount();
		initialise (field, content, index, tree, node);
	}
	
	/**
	 * Creates an instance and performs the edit, 
	 * creating and adding the parameter to field
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param paramType		A string defining the type of parameter to add.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public AddParamEdit(IField field, String paramType, 
			JTree tree, TreeNode node) {
		IParam param = FieldParamsFactory.getFieldParam(paramType);
		
		initialise(field, param, tree, node);
	}
	
	/**
	 * Creates an instance and performs the edit, 
	 * creating and adding the parameter to field
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param paramType		A string defining the type of parameter to add.
	 * @param index			The index to add the new parameter
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public AddParamEdit(IField field, String paramType, int index, 
			JTree tree, TreeNode node) {
		IParam param = FieldParamsFactory.getFieldParam(paramType);
		
		initialise(field, param, index, tree, node);
	}
	
	/**
	 * Creates an instance and performs the edit, 
	 * Adding a textContent to the field.
	 * 
	 * @param textContent	The text to add as a {@link TextContent}
	 * @param field		The field to add a new parameter to.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public AddParamEdit(String textContent, IField field, 
			JTree tree, TreeNode node) {
		
		IFieldContent text = new TextContent(textContent);
		initialise(field, text, tree, node);
	}
	
	
	public void undo() {
		indexOfParam = field.removeContent(param);
		notifySelectStartEdit();
	}
	
	public void redo() {
		field.addContent(indexOfParam, param);
		notifySelectStartEdit();
	}
	
	public boolean canUndo() {
		return true;
	}
	
	public boolean canRedo() {
		return true;
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
