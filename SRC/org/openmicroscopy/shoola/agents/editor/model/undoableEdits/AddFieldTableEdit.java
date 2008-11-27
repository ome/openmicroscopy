 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddFieldTableEdit 
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
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.tables.FieldTableModelAdaptor;
import org.openmicroscopy.shoola.agents.editor.model.tables.TableModelFactory;

/** 
 * This Edit adds a new {@link FieldTableModelAdaptor} to the field, so that
 * multiple values for the field's parameters can be displayed in a table. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class AddFieldTableEdit 

extends AbstractUndoableEdit {
	
	/**
	 * A reference to the {@link IField} that the new {@link IParam} is
	 * being added to.
	 */
	private IField 				field;
	
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
	 * The newly-created table data
	 */
	private TableModel			tableData;
	
	/**
	 * The old table data, for undo (will be null if none existed). 
	 */
	private TableModel			oldTableData;
	
	
	/**
	 * Initialises variables and does the adding edit. 
	 * @param field
	 * @param content
	 * @param index
	 * @param tree
	 * @param node
	 */
	private void initialise(IField field, JTree tree, TreeNode node,
													TableModel tableData) 
	{
		this.field = field;
		this.tree = tree;
		this.node = node;
		this.tableData = tableData;
		oldTableData = field.getTableData();
		
		redo();
	}
	
	/**
	 * Creates an instance and performs the edit, 
	 * Adding a tableModel to the field.
	 * 
	 * @param field		The field to add a new tableModel to.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public AddFieldTableEdit(IField field, JTree tree, TreeNode node) {
		
		TableModel tm = TableModelFactory.getFieldTable(field);
		
		initialise(field, tree, node, tm);
	}
	
	/**
	 * This method allow you to test whether a field should be allowed to 
	 * have a table of parameter values. 
	 * 
	 * @param newField
	 * @return
	 */
	public static boolean canDo(IField newField) 
	{	
		// can't have parameter data if no parameters! 
		if (newField.getAtomicParams().size() == 0) {
			return false;
		}
		// don't want to add table if it already exists!
		if (newField.getTableData() != null)
			return false;
		
		return true;
	}
	
	public void undo() {
		
		field.setTableData(oldTableData);
		notifySelectStartEdit();
	}
	
	public void redo() {
		
		field.setTableData(tableData);
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
			
			DefaultMutableTreeNode dmtNode = (DefaultMutableTreeNode)node;
			// tree.startEditingAtPath(new TreePath(dmtNode.getPath()));
			
			//tree.clearSelection();
			
			DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
			treeModel.nodeChanged(node);
		}
	}
}
