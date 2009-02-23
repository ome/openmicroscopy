 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.MoveFieldsUpEdit 
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

/** 
 * Moves the fields up in order. Only do-able if the first selected node 
 * has a previous sibling. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class MoveUpEdit 
	extends UndoableTreeEdit
{

	/**
	 * The nodes to be moved.
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
	public MoveUpEdit(JTree tree) {
		super(tree);
		
		movedNodes = new ArrayList<DefaultMutableTreeNode>();
	}

	/**
	 * Can move fields as long as there
	 * are some fields selected and the first node has a previous sibling
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
		
		// if no previous sibling, can't move up
		return (! (previousSibling == null));
	}

	/**
	 * Moves the fields up. 
	 * Makes a list of the selected nodes, then calls {@link #redo()}. 
	 */
	@Override
	public void doEdit() 
	{	
		if (tree == null) return;
		
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
	 * Moves the fields down.
	 */
	public void undo() {
		
		TreeModelMethods.moveFieldsDown(movedNodes, treeModel);
		
		// need to expand parent and highlight fields
		TreeModelMethods.selectDNodes(movedNodes, tree);
	}
	
	/**
	 * Moves the fields up. 
	 */
	public void redo() {
		TreeModelMethods.moveFieldsUp(movedNodes, treeModel);
		
		// need to expand parent and highlight fields
		TreeModelMethods.selectDNodes(movedNodes, tree);
	}
	
	/**
	 * Presentation name is "Move Fields Up"
	 */
	public String getPresentationName() {
		     return "Move Fields Up";
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
