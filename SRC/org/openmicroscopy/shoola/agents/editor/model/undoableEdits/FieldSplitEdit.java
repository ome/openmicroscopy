 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.FieldSplitEdit 
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
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.FieldNode;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;

/** 
 * This {@link AbstractUndoableEdit} performs the 'splitting' of a field/step,
 * so that a new step is created and added before the original step as a sibling
 * in the tree model.
 * (Added before, since adding after would come after child steps). 
 * The original step's name and content should be specified, as well as the 
 * content of the new step. The actual splitting of the step content is 
 * handled elsewhere, so that this class can merely place the content in the
 * correct steps/fields. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FieldSplitEdit 
	extends AbstractUndoableEdit 
{

	/**
	 * A reference to the {@link IField} that the we are splitting the content 
	 * of, to create 2 daughter fields.
	 */
	private IField 					field;
	
	/**
	 * This is a reference to the new daughter field. 
	 */
	private IField					newField;
	
	/**
	 * A reference to the new list of {@link IFieldContent}, being added to
	 * the first of 2 daughters
	 */
	private List<IFieldContent>		newContent1;
	
	/**
	 * A reference to the new list of {@link IFieldContent}, being added to 
	 * the second of 2 daughters
	 */
	private List<IFieldContent>		newContent2;
	
	/**
	 * A reference to the old list of {@link IFieldContent}, of the parent
	 * field being split.
	 * Used for undo.
	 */
	private List<IFieldContent>		oldContent;
	
	/**	The old name of the old parent field */
	private String 					oldName;
	
	/**	The new name of the first daughter field (second daughter has no name) */
	private String 					newName;
	
	/** The attribute for the name of the field */
	private static String			NAME = Field.FIELD_NAME;				
	
	/**
	 * The {@link JTree} which displays the field being split. 
	 * If this is not null,
	 * it's {@link TreeModel} will be notified of changes to the node, and 
	 * the JTree selection path will be set to the edited node. 
	 */
	private JTree 					tree; 
	
	/**
	 * The node that contains the field being split. 
	 * This is used to notify the TreeModel that nodeChanged() and to 
	 * set the selected node in the JTree after editing. 
	 */
	private TreeNode 				node;
	
	/**
	 * The new Node created to hold the new daughter field/step.
	 */
	private DefaultMutableTreeNode	newNode;
	
	/**
	 * The parent node of the field that is being split. 
	 */
	private DefaultMutableTreeNode	parentNode;
	
	/**
	 * A reference to the tree model we're editing. Provides methods for adding
	 * new node, etc. 
	 */
	private DefaultTreeModel		treeModel;
	
	/**
	 * Creates an instance and performs the edit, to split the field into 2
	 * daughter fields. 
	 * 
	 * @param field			The field to split
	 * @param fieldName		The name of the first daughter
	 * @param content1		The new content of the first daughter
	 * @param content2		The new content of the second daughter
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node			The node to highlight / refresh with undo/redo. 
	 */
	private void initialise(IField field, String fieldName, 
			List<IFieldContent> content1, List<IFieldContent> content2, 
			JTree tree, TreeNode node) 
	{
		this.newName = fieldName;
		this.field = field;
		this.newContent1 = content1;
		this.newContent2 = content2;
		this.tree = tree;
		this.node = node;
		this.parentNode = (DefaultMutableTreeNode)node.getParent();
		this.treeModel = (DefaultTreeModel)tree.getModel();
		
		oldName = field.getAttribute(NAME);
		oldContent = new ArrayList<IFieldContent>();
		for (int i=0; i< field.getContentCount(); i++) {
			oldContent.add(field.getContentAt(i));
		}
		
		// create the new daughter
		newField = new Field();
		 // Place this in a new Node
		newNode = new FieldNode(newField);
		
		redo();		// perform the edit
	}
	
	/**
	 * Creates an instance and performs the edit, to split the field into 2
	 * daughter fields. 
	 * 
	 * @param field			The field to split
	 * @param content1		The new content of the first daughter
	 * @param content2		The new content of the second daughter
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node			The node to highlight / refresh with undo/redo. 
	 */
	public FieldSplitEdit(IField field, List<IFieldContent> content1, 
			List<IFieldContent> content2, JTree tree, TreeNode node) {
		
		// if a new name has not been set, use the old one. 
		String newFieldName = field.getAttribute(NAME);
		initialise(field, newFieldName, content1, content2, tree, node);
	}
	
	/**
	 * Creates an instance and performs the edit, of the field contents and 
	 * field name. 
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param fieldName		The new name for this field.
	 * @param paramType		A string defining the type of parameter to add.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public FieldSplitEdit(IField field, String fieldName, 
			List<IFieldContent> content1, List<IFieldContent> content2, 
			JTree tree, TreeNode node) {
		
		initialise(field, fieldName, content1, content2, tree, node);
	}
	
	/**
	 * Implemented as specified by the {@link UndoableTreeEdit} abstract class.
	 * Sets the name to {@link #oldName}, and the content to {@link #oldContent}
	 * 
	 * @see UndoableTreeEdit#undo()
	 */
	public void undo() {
		
		if (parentNode == null) 	return;		// can't split root
		
		// remove the new daughter node
		treeModel.removeNodeFromParent(newNode);
		
		// reset the name of the parent
		field.setAttribute(NAME, oldName);
		
		// remove the content from parent
		for(int i=field.getContentCount()-1; i>=0; i--) {
			field.removeContent(i);
		}
		
		// remove the content from daughter
		for(int i=newField.getContentCount()-1; i>=0; i--) {
			newField.removeContent(i);
		}
		
		// add content back to parent
		for(IFieldContent newItem : oldContent) {
			field.addContent(newItem);
		}
		
		notifyNodeChanged();
		
		tree.clearSelection();
		// re-select the node (which was selected before the tree was modified)
		TreePath path = new TreePath(((DefaultMutableTreeNode)node).getPath());
		tree.setSelectionPath(path);
	}
	
	/**
	 * Implemented as specified by the {@link UndoableTreeEdit} abstract class.
	 * Sets the name to {@link #newName}, and the content to {@link #newContent1}
	 * 
	 * @see UndoableTreeEdit#redo()
	 */
	public void redo() {
		
		if (parentNode == null) 	return;		// can't split root
		
		// set the new name of the daughter - to go before parent
		newField.setAttribute(NAME, newName);
		// the name should be removed from the old parent.
		field.setAttribute(NAME, null);
		
		// remove old content from parent field
		for(int i=field.getContentCount()-1; i>=0; i--) {
			field.removeContent(i);
		}
		
		// set new content for parent
		for(IFieldContent newItem : newContent2) {
			field.addContent(newItem);
		}
		
		// set new content for daughter (to be added before parent)
		for(IFieldContent newItem : newContent1) {
			newField.addContent(newItem);
		}
		
		// now add the new node (containing the new field) before the original 
		int indexToAdd = parentNode.getIndex(node);
		treeModel.insertNodeInto(newNode, parentNode, indexToAdd);
		
		// need to clear selection so that the re-select has the desired effect
		// otherwise, doesn't behave as desired. 
		tree.clearSelection();
		
		// re-select the node (which was selected before the tree was modified)
		TreePath path = new TreePath(((DefaultMutableTreeNode)node).getPath());
		tree.setSelectionPath(path);
	}
	
	/**
	 * Overrides {@link AbstractUndoableEdit#getPresentationName()} to 
	 * return "Split Step"
	 */
	public String getPresentationName()
	{
		return "Split Step";
	}
	
	public boolean canUndo() {
		return true;
	}
	
	public boolean canRedo() {
		return true;
	}
	
	/**
	 * This notifies all listeners to the TreeModel of a change to the node
	 * in which the content has been edited. 
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
