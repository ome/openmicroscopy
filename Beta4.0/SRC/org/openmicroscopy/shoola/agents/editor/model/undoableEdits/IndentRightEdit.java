 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.IndentRightEdit 
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

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.browser.ContiguousChildSelectionModel;

/** 
 * This Undoable edit indents the currently selected nodes of the 
 * {@link JTree} to the right, so that they become children of
 * their preceding sibling. 
 * If they do not have a preceding sibling then {@link #canDo()} will
 * return false; 
 * 
 * NB: This edit should be used in conjunction with the 
 * {@link ContiguousChildSelectionModel}, so that all the nodes to be indented
 * are siblings. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class IndentRightEdit
	extends UndoableTreeEdit
{

	/**
	 * The nodes to be indented.
	 * Keep a reference for undo / redo. 
	 */
	private List <DefaultMutableTreeNode> 		movedNodes;
	
	/**
	 * Creates an instance of this class. 
	 * If JTree tree is null, it is necessary to call 
	 * setTree(JTree) before this Edit can be used. 
	 * 
	 * @param tree		The JTree to edit (indent fields)
	 */
	public IndentRightEdit(JTree tree) {
		super(tree);
		
		movedNodes = new ArrayList<DefaultMutableTreeNode>();
	}

	/**
	 * Can indent fields as long as the JTree is not null, there
	 * are some fields selected and they have 
	 */
	public static boolean canDo(TreePath[] paths)
	{	
		if (paths == null) return false;
		if (paths.length == 0)
			return false;
		
		// Need to check the first node has a previous sibling
		TreePath selectedPath = paths[0];
		DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode)
										selectedPath.getLastPathComponent();
	
		DefaultMutableTreeNode previousSibling = firstNode.getPreviousSibling();
		
		// if no previous sibling, can't indent
		return (! (previousSibling == null));
	}

	/**
	 * Performs the indent field operation.
	 * Makes a list of the selected nodes, then calls {@link #redo()}. 
	 */
	@Override
	public void doEdit() 
	{	
		TreePath[] selectedPaths = tree.getSelectionPaths();
		if (! canDo(selectedPaths) ) return;
		
		if (selectedPaths.length > 0) {
			
			// Make a list of the Nodes to be indented
			DefaultMutableTreeNode currentNode;
			for (int i=0; i<selectedPaths.length; i++) {
				currentNode = (DefaultMutableTreeNode)
						selectedPaths[i].getLastPathComponent();
				movedNodes.add(currentNode);
			}
		}
		
		redo();		
	}
	
	/**
	 * Indents the fields back to the left. 
	 */
	public void undo() {
		TreeModelMethods.indentNodesLeft(movedNodes, treeModel);
		
		// need to expand parent and highlight fields
		TreeModelMethods.selectDNodes(movedNodes, tree);
	}
	
	/**
	 * Re-indents the fields to the right.
	 */
	public void redo() {
		TreeModelMethods.indentNodesRight(movedNodes, treeModel);
		
		// need to expand parent and highlight fields
		TreeModelMethods.selectDNodes(movedNodes, tree);
	}
	
	/**
	 * Presentation name is "Indent Fields to Right"
	 */
	public String getPresentationName() {
		     return "Indent Fields to Right";
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
