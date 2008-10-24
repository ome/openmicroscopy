 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.FieldContentEdit 
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
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;

/** 
 * This edit affects the content of a field. 
 * Used by the text-view editing, where the user may potentially edit several 
 * text content (descriptions) and may remove or add parameters
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldContentEdit 
	extends AbstractUndoableEdit 
{

	/**
	 * A reference to the {@link IField} that the we are editing the content of
	 */
	private IField 					field;
	
	/**
	 * A reference to the new list of {@link IFieldContent}, being added.
	 */
	private List<IFieldContent>		newContent;
	
	/**
	 * A reference to the old list of {@link IFieldContent}, being replaced.
	 * Used for undo.
	 */
	private List<IFieldContent>		oldContent;
	
	/**
	 * The {@link JTree} which displays the field being edited. 
	 * If this is not null,
	 * it's {@link TreeModel} will be notified of changes to the node, and 
	 * the JTree selection path will be set to the edited node. 
	 */
	private JTree 					tree; 
	
	/**
	 * The node that contains the field being edited. 
	 * This is used to notify the TreeModel that nodeChanged() and to 
	 * set the selected node in the JTree after editing. 
	 */
	private TreeNode 				node;
	
	/**
	 * Initialises variables and does the adding edit. 
	 * @param field		The field we're editing	
	 * @param content	The content to replace old content of the field
	 * @param tree		JTree for selection
	 * @param node		Node to highlight for undo/redo. 
	 */
	private void initialise(IField field, List<IFieldContent> content, 
			JTree tree, TreeNode node) 
	{
		this.field = field;
		this.newContent = content;
		this.tree = tree;
		this.node = node;
		
		oldContent = new ArrayList<IFieldContent>();
		for (int i=0; i< field.getContentCount(); i++) {
			oldContent.add(field.getContentAt(i));
		}
		redo();
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
	public FieldContentEdit(IField field, List<IFieldContent> content, 
			JTree tree, TreeNode node) {
		
		initialise(field, content, tree, node);
	}
	
	
	public void undo() {
		for(int i=field.getContentCount()-1; i>=0; i--) {
			field.removeContent(i);
		}
		
		for(IFieldContent newItem : oldContent) {
			field.addContent(newItem);
		}
		
		notifySelect();
	}
	
	public void redo() {
		for(int i=field.getContentCount()-1; i>=0; i--) {
			field.removeContent(i);
		}
		
		for(IFieldContent newItem : newContent) {
			field.addContent(newItem);
		}
		
		notifySelect();
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
	private void notifySelect() 
	{
		notifyNodeChanged();
		TreeModelMethods.selectNode(node, tree);
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
