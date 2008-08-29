 /*
 *  org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods 
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
package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

/** 
 * This class contains a number of static methods that are useful for 
 * JTree manipulation and node selection etc. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TreeModelMethods {
	
	/**
	 * This method adds the nodes specified as children of the parent node,
	 * starting at the index specified. 
	 * 
	 * @param treeModel		The treeModel being edited. Will notify JTree of update
	 * @param nodes		The list of nodes to add as children
	 * @param parent	The parent to accept the nodes as children
	 * @param index		The index to begin adding the children
	 */
	public static void insertNodesInto(DefaultTreeModel treeModel,
			List<MutableTreeNode> nodes, 
			MutableTreeNode parent,
			int index) {
		
		// If the index = childCount, nodes will be added after the last child.
		// Make sure that the index is not greater (will cause Exception)
		if(index > parent.getChildCount()) {
			index = parent.getChildCount();
		}
		// Go through the list of nodes, adding them at incremental index position
		for (MutableTreeNode node: nodes) {
			treeModel.insertNodeInto(node, parent, index);
			index++;
		}
	}
	
	/**
	 * This method will remove the List of nodes specified from their parent.
	 * 
	 * @param treeModel		The TreeModel being edited. Will notify JTree of update
	 * @param nodes		The nodes to be removed from their parent (don't 
	 * 				need to have the same parent)
	 */
	public static void removeNodesFromParent(DefaultTreeModel treeModel,
			List<MutableTreeNode> nodes) 
	{	
		for (MutableTreeNode node: nodes) {
			treeModel.removeNodeFromParent(node);
		}
	}
	
	/**
	 * Set the selected nodes in the JTree.
	 * This builds an array of TreePath[] and calls setSelectedPaths on JTree
	 * It will then call scrollPathToVisible(path) on the first path, so
	 * as to display it if it's in a ScrollPane. 
	 * NB. nodes need to be instances of DefaultMutableTreeNode for this
	 * method to work. Nodes that cannot be cast to DefaultMutableTreeNodes
	 * will be ignored.
	 * 
	 * @param nodes		The List of nodes to select	
	 * @param tree		The JTree in which these nodes exist
	 */
	public static void selectNodes(List<MutableTreeNode> nodes, JTree tree) 
	{
		if (tree == null) return;
		
		TreePath[] paths = new TreePath[nodes.size()];
		int index = 0;
		for (TreeNode node : nodes) {
			if (node instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode dnode = (DefaultMutableTreeNode)node;
				paths[index++] = new TreePath(dnode.getPath());
			}
		}
		tree.setSelectionPaths(paths);
		
		if (paths.length > 0) {
			tree.scrollPathToVisible(paths[0]);
		}
	}
	
	
	/**
	 * Set the selected node in the JTree, and scroll to visible.
	 * This method delegates to selectNodes(nodes, tree).
	 * 
	 * @see selectNodes(List<TreeNode> nodes, JTree tree)
	 * 
	 * @param node		The node to select	
	 * @param tree		The JTree in which these nodes exist
	 */
	public static void selectNode(TreeNode node, JTree tree) 
	{
		List<MutableTreeNode> nodes = new ArrayList<MutableTreeNode>();
		if (node instanceof MutableTreeNode) {
			nodes.add((MutableTreeNode)node);
			selectNodes(nodes, tree);
		}
	}
	
	
	/**
	 * Duplicates the child of oldNode and adds them to newNode.
	 * Also recursively calls this method for child nodes to copy
	 * the entire sub-tree of oldNode to newNode. 
	 * This method casts the User Object from each node to a 
	 * Field and duplicates this object using Field.clone() before
	 * placing this in a new node. 
	 * 
	 * @param oldNode	The existing node with children to copy
	 * @param newNode	The new node, with no children added yet.
	 */
	public static void duplicateNode(DefaultMutableTreeNode oldNode,
			DefaultMutableTreeNode newNode) 
	{
		
		for(int i=0; i<oldNode.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode)
				oldNode.getChildAt(i);
			Field oldField = (Field)child.getUserObject();
			Field newField = (Field)oldField.clone();
			
			DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(newField);
			newNode.add(newChild);
			
			duplicateNode(child, newChild);
		}
	}
	
}
