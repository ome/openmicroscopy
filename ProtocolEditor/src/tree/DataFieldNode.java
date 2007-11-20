/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

import org.w3c.dom.Element;


public class DataFieldNode {
	
	DataField dataField;
	
	boolean highlighted = false; 	// is this node/dataField selected (displayed blue)
	
	Tree tree;		// class that manages tree structure (takes click commands)
	ArrayList<DataFieldNode> children;
	DataFieldNode parent;
	Box childBox;	// swing component that holds all subtree. Hide when collapse
	
	// constructor 
	public DataFieldNode(LinkedHashMap<String, String> allAttributesMap, DataFieldNode parent, Tree tree) {
		this.parent = parent;
		this.tree = tree;
		children = new ArrayList<DataFieldNode>();
		dataField = new DataField(allAttributesMap, this);
	}
	
	// this constructor used for root node (no parent)
	public DataFieldNode(LinkedHashMap<String, String> allAttributesMap,  Tree tree) {
		this.parent = null;
		this.tree = tree;
		children = new ArrayList<DataFieldNode>();
		dataField = new DataField(allAttributesMap, this);
	}
	// this constructor used for blank root node (no parent)
	public DataFieldNode( Tree tree) {
		children = new ArrayList<DataFieldNode>();
		dataField = new DataField(this);
		this.parent = null;
		this.tree = tree;
	}
	
	// constructor to make a copy of existing Node
	// retuns duplicate node with no parent
	public DataFieldNode(DataFieldNode copyThisNode,  Tree tree) {
		
		children = new ArrayList<DataFieldNode>();
		this.tree = tree;
	
		dataField = new DataField(copyThisNode.getDataField(), this);
	}
	
	// constructor to make a copy of existing Node
	// retuns duplicate node with no parent
	public DataFieldNode(DataFieldNode copyThisNode) {
		
		children = new ArrayList<DataFieldNode>();
		// get ref to tree from parent (when setParent is called)
	
		dataField = new DataField(copyThisNode.getDataField(), this);
	}
	
	public int getMyIndexWithinSiblings() {
		if (parent == null)
			throw (new NullPointerException("Can't getMyIndexWithinSiblings because parent == null"));
			
		return parent.indexOfChild(this);
	}
	
	public void setParent(DataFieldNode parent) {
		this.parent = parent;
		if (tree == null) tree = parent.getTree();
	}
	
	// iterator code, from http://www.cs.bc.edu/~sciore/courses/cs353/coverage.html  chapter 23
	public Iterator<DataFieldNode> childIterator() {
		return children.iterator();
	}
	public Iterator<DataFieldNode> iterator() {
		return new TreeIterator(this);
	}
	
	public void addChild(DataFieldNode dataFieldNode) {
		children.add(dataFieldNode);
	}
	public void addChild(int index, DataFieldNode dataFieldNode) {
		children.add(index, dataFieldNode);
	}
	public void removeChild(DataFieldNode child) {
		children.remove(child);
	}
	public void removeChild(int index){
		children.remove(index);
	}
	public int indexOfChild(DataFieldNode child) {
		return children.indexOf(child);
	}
	public DataFieldNode getChild(int index) {
		return children.get(index);
	}

	public DataField getDataField() {
		return dataField;
	}
	public JPanel getFieldEditor() {
		return dataField.getFieldEditor();
	}
	public JPanel getFormField() {
		return dataField.getFormField();
	}
	public String getName() {
		if (dataField == null) return "No field";
		return dataField.getName();
	}
	public DataFieldNode getParentNode() {
		return parent;
	}
	
	public ArrayList<DataFieldNode> getChildren() {
		return children;
	}
	public void setChildBox(Box childBox) {
		this.childBox = childBox;
	}
	public Box getChildBox() {
		return childBox;
	}
	
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
		dataField.setHighlighted(highlighted);
	}
	public boolean getHighlighted() {
		return highlighted;
	}
	public void nodeClicked(boolean clearOthers) {
		getTree().nodeSelected(this, clearOthers);
	}
	// let the tree know that a dataField has changed - and pass reference for undo() action
	public void dataFieldUpdated(AbstractUndoableEdit  undoDataFieldAction) {
		getTree().dataFieldUpdated(undoDataFieldAction);
	}
	// notification that UI needs updating. eg. due to dataField inputType change
	public void xmlUpdated() {
		getTree().xmlUpdated();
	}
	public void hideChildren(boolean hidden) {
		if (childBox != null)	// sometimes visibility of children is set before UI is fully built
			childBox.setVisible(!hidden);
	}

	public void collapseAllChildren(boolean collapse) {
		getTree().collapseAllChildren(collapse);
	}
	
	// expandAllAncestors() 	used to show a field that may be hidden
	public void expandAllAncestors() {
		dataField.collapseChildren(false);
		if (parent != null) {
			parent.expandAllAncestors();
		}
	}
	
	public Tree getTree() {
		if ((tree == null) && (parent != null)) tree = parent.getTree();
		return tree;
	}
}
