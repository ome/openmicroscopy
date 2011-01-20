 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.ClearValues 
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
import java.util.HashMap;
import java.util.Iterator;
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
import org.openmicroscopy.shoola.agents.editor.model.TreeIterator;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;

/** 
 * This Edit iterates through the tree, making a map of all the parameters
 * with their values. Redo sets all values to null and Undo re-sets them. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ClearValuesEdit extends AbstractUndoableEdit 
	implements TreeEdit
{
	/**
	 * The root node. Start here to iterate through tree for fields to clear. 
	 */
	private DefaultMutableTreeNode 		root;
	
	/**
	 * The model we are editing. Used to notify changes. 
	 */
	DefaultTreeModel 					treeModel;
	
	/**
	 * A map of all the parameters we are clearing, with a list of values
	 */
	private	HashMap<IParam, List<Object>> 	clearedParams;
	
	/**
	 * A map of all the parameters we are clearing, with the node 
	 * that holds each, so that node-changes can be notified with undo/redo
	 */
	private	HashMap<IParam, DefaultMutableTreeNode> 	paramNodes;
	
	/**
	 * Iterate through the tree model and make a map of all parameters that 
	 * have any values to delete, along with a list of their values.
	 * Also make a map of their corresponding nodes.
	 */
	private void getClearedParams() 
	{
		// make a map of all the parameters and list of values. 
		clearedParams  = new HashMap<IParam, List<Object>>();
		paramNodes = new HashMap<IParam, DefaultMutableTreeNode>();
		
		TreeNode tn;
		IField f;
		Object userOb;
		DefaultMutableTreeNode node;
		
		
		Iterator<TreeNode> iterator = new TreeIterator(root);
		
		IFieldContent content;
		List<Object> values;
		
		while (iterator.hasNext()) {
			tn = iterator.next();
			if (!(tn instanceof DefaultMutableTreeNode)) continue;
			node = (DefaultMutableTreeNode)tn;
			userOb = node.getUserObject();
			if (!(userOb instanceof IField)) continue;
			f = (IField)userOb;
			
			int paramCount = f.getContentCount();
			if (paramCount == 0) continue;
			
			// for each parameter...
			for (int i=0; i<paramCount; i++) {
				content = f.getContentAt(i);
				if (! (content instanceof IParam))	continue;
				IParam param = (IParam)content;
				// ...copy values list
				int valueCount = param.getValueCount();
				if (valueCount == 0)	continue;
				
				values = new ArrayList<Object>();
				for (int v=0; v<valueCount; v++) {
					values.add(param.getValueAt(v));
				}
				// add to map of cleared parameters
				clearedParams.put(param, values);
				paramNodes.put(param, node);
			}
		}
	}
	
	/**
	 * Creates an instance and performs the delete of values.
	 * 
	 * @param tree		The tree that wraps the Tree Model to edit. 
	 */
	public ClearValuesEdit(JTree tree) {
		setTree(tree);
		doEdit();
	}
	
	/**
	 * Makes a map of the parameters to clear, then calls {@link #redo}
	 */
	private void doEdit() {
		if (treeModel == null)	return;
		root = (DefaultMutableTreeNode)treeModel.getRoot();
		
		getClearedParams();
		
		redo();
	}
	
	public void undo() {
		// re-set the values of each parameter
		Iterator<IParam> i = clearedParams.keySet().iterator();
		List<Object> values;
		IParam p;
		TreeNode n;
		while(i.hasNext()) {
			p = i.next();
			values = clearedParams.get(p);
			for (int v=0; v<values.size(); v++) {
				p.setValueAt(v, values.get(v));
			}
			// get the node from the other map, and notify model of change
			n = paramNodes.get(p);
			notifyNodeChanged(n);
		}
	}
	
	public void redo() {
		// set the values of each parameter to null
		Iterator<IParam> i = clearedParams.keySet().iterator();
		List<Object> values;
		IParam p;
		TreeNode n;
		while(i.hasNext()) {
			p = i.next();
			values = clearedParams.get(p);
			for (int v=0; v<values.size(); v++) {
				p.removeValueAt(0);
			}
			// get the node from the other map, and notify model of change
			n = paramNodes.get(p);
			notifyNodeChanged(n);
		}
	}
	
	public boolean canUndo() {
		return true;
	}
	
	public boolean canRedo() {
		return true;
	}
	
	/**
	 * Overrides this method in {@link AbstractUndoableEdit} to return
	 * "Experiment Info"
	 */
	 public String getPresentationName() 
	 {
		 return "Delete Experiment Info";
	 }
	
	/**
	 * This notifies all listeners to the TreeModel of a change to the node
	 * in which the attribute has been edited. 
	 * This will update any JTrees that are currently displaying the model. 
	 * 
	 * @param node		The node that has changed
	 */
	private void notifyNodeChanged(TreeNode node) 
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
