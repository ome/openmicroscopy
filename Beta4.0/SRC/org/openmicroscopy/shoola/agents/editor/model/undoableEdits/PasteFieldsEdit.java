 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.PasteFieldsEdit 
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
import javax.swing.tree.TreePath;
import javax.swing.undo.AbstractUndoableEdit;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.view.EditorFactory;
import org.openmicroscopy.shoola.env.ui.UserNotifier;

/** 
 * This Undoable-Edit pastes copied data from the {@link EditorFactory}, in 
 * the form of any nodes, into the current file, after currently selected nodes. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class PasteFieldsEdit 
	extends UndoableTreeEdit
{

	/**
	 * The new nodes, pasted in.
	 * This is needed so that when undo() has removed new fields, it can 
	 * be added back to the parent.
	 */
	private List<DefaultMutableTreeNode>		newNodes;
	
	/**
	 * The child index of the last selected field (before adding new fields). 
	 * This is required so that when undo() has removed new field, it can 
	 * be added back at the right index.
	 */
	private int 								indexOfNewField;
	
	/**
	 * The parent of the new fields.
	 * This is needed so that when undo() has removed new field, it can 
	 * be added back to the right parent.
	 */
	private DefaultMutableTreeNode 				parentNode;

	/**
	 * Creates an instance of this class. 
	 * If JTree tree is null, it is necessary to call 
	 * setTree(JTree) before this Edit can be used. 
	 * 
	 * @param tree		The JTree to edit (paste fields)
	 */
	public PasteFieldsEdit(JTree tree) {
		super(tree);
	}

	/**
	 * 
	 */
	public static boolean canDo() 
	{
		if (EditorFactory.getCopiedData() == null) return false;
		
		return true;
		
	}

	/**
	 *  Implemented as specified by the {@link UndoableTreeEdit} abstract class. 
	 * Performs the paste field operation.
	 * Sets references to the new node, it's parent and index. 
	 * 
	 * @see UndoableTreeEdit#doEdit()
	 */
	public void doEdit() 
	{	
		if (! canDo() ) {
			
			UserNotifier un = EditorAgent.getRegistry().getUserNotifier();
			un.notifyInfo("No steps to paste", 
					"Can't paste: No steps have been copied to the clipboard.");
			return;
		}
		
		Object data = EditorFactory.getCopiedData();
		if (! (data instanceof Object[])) return;
		
		Object copiedData[] = (Object[])data;
		
		 // make a list of nodes pasted in.
		newNodes = new ArrayList<DefaultMutableTreeNode>();
		
		// duplicate the list of nodes, so that if you paste twice, you are 
		// not pasting the same nodes each time. 
		Object item;
		DefaultMutableTreeNode pastedNode;
		for (int i = 0; i < copiedData.length; i++) {
			item = copiedData[i];
			if (item instanceof DefaultMutableTreeNode) {
				pastedNode = (DefaultMutableTreeNode)item;
				newNodes.add(TreeModelMethods.duplicateNode(pastedNode));
			}
		}
		
		TreePath[] selectedPaths = tree.getSelectionPaths();
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
				// ...otherwise add new field after the last selected field
				indexOfNewField = indexOfLastHighlightedField +1;
				parentNode = (DefaultMutableTreeNode)lastField.getParent();
			}
		} 
		else {
			//If no fields selected, want to add as the last child of root
			parentNode = (DefaultMutableTreeNode)treeModel.getRoot();
			indexOfNewField = parentNode.getChildCount();
		}
		
		redo();
		
	}
	
	/**
	 * Deletes the new fields
	 * 
	 * @see AbstractUndoableEdit#undo()
	 */
	public void undo() {
		for (DefaultMutableTreeNode newNode : newNodes) {
			treeModel.removeNodeFromParent(newNode);
		}
	}
	
	/**
	 * Re-inserts the new Nodes
	 * 
	 * @see AbstractUndoableEdit#redo()
	 */
	public void redo() {
		TreePath[] paths = new TreePath[newNodes.size()];
		int i=0;
		int indexToAdd = indexOfNewField;
		for (DefaultMutableTreeNode newNode : newNodes) {
			treeModel.insertNodeInto(newNode, parentNode, indexToAdd++);
			paths[i++] = new TreePath(newNode.getPath());
		}
		
		// Select the new nodes
		tree.setSelectionPaths(paths);
	}
	
	/**
	 * Presentation name is "Paste Fields"
	 * 
	 * @see AbstractUndoableEdit#getPresentationName()
	 */
	public String getPresentationName() {
		     return "Paste Fields";
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
