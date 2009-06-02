 /*
 * org.openmicroscopy.shoola.agents.editor.browser.EditableTree 
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a JTree that with Custom cellRenderers, Editors and Selection
 * behaviour. 
 * Nodes are displayed as Panels, and a single node click will allow editing
 * of the data via the panel. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class EditableTree 
	extends JTree 
	implements TreeSelectionListener,
	TreeModelListener
	
{
	/**
	 * This JTree observes selection changes to the navTree
	 */
	private JTree 			navTree;
	
	/** Controller, for determining the editing mode etc. */
	private BrowserControl 	controller;
	
	public EditableTree(BrowserControl controller, JTree navTree) 
	{
		super();
		
		this.controller = controller;
		this.navTree = navTree;
		if (navTree != null) {
			navTree.addTreeSelectionListener(this);
		}
		addTreeSelectionListener(this);
		
		configureTree(controller);
	}
	
	/**
	 * The default UI (BasicTreeUI) is replaced with a subclass
	 * to modify the selection and editing behavior.
	 * 
	 * A custom selection model allows multiple nodes to be selected,
	 * but ensures that they are contiguous and are all siblings.
	 * 
	 * Setting the row height to 0 allows each node to choose it's
	 * own size. The JTree will call getPreferredSize() for each.
	 * 
	 * A custom TreeCellRenderer (extends DefaultTreeCellRenderer)
	 * renders nodes as JPanels.
	 * The field renderer will pass a reference of the controller to 
	 * the fields, so that they can call undo/redo edits etc. 
	 * 
	 * A TreeCellEditor for editing fields.
	 * This merely delegates to the fieldRenderer because the same 
	 * components are used for display and editing of the tree Cells.
	 * 
	 * The DefaultTreeCellEditor (when passed a TreeCellEditor) uses this 
	 * to switch between editing and display. 
	 * 
	 * @param controller		The controller 
	 */
	private void configureTree(BrowserControl controller) 
	{
		setUI(new MyBasicTreeUI());
		
		setSelectionModel(new ContiguousChildSelectionModel());
		
		setRowHeight(0);
		
		// Custom renderer.
		DefaultTreeCellRenderer fieldRenderer = new FieldRenderer(controller);
		setCellRenderer(fieldRenderer);
		
		// Custom editor
		TreeCellEditor fieldEditor = new DefaultFieldEditor(fieldRenderer);
	
	    TreeCellEditor editor = new DefaultTreeCellEditor(this, 
	    		fieldRenderer, fieldEditor);
	    setCellEditor(editor);
	    
	}
	
	/**
	 * Overrides this method of JTree, in order to respond to the 
	 */
	public boolean isEditable()
	{
		if (controller == null)		
			return false;
		
		// editable if file is not locked and we are editing Experiment. 
		boolean editable = ((! controller.isFileLocked()) 
				&& (controller.getEditingMode() == Browser.EDIT_EXPERIMENT));
		return editable;
	}

	
	/**
	 * Every selection change in the nav Tree is mimicked by this tree view,
	 * and vice versa. 
	 * 
	 * @see	TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) 
	{
		Object source = e.getSource();
		
		if (source.equals(navTree)) 
		{
			if (navTree.getSelectionCount() == 0) return;
			
			TreePath[] selPaths = navTree.getSelectionPaths();
			
			/* make sure the node is visible (expand parent) */
			expandPath(selPaths[0].getParentPath());
			
			removeTreeSelectionListener(this);
			setSelectionPaths(selPaths);
			addTreeSelectionListener(this);
			
			scrollPathToVisible(selPaths[0]);
		} 
		else if (source.equals(this)) {
			if (getSelectionCount() == 0) return;
			
			TreePath[] selPaths = getSelectionPaths();
			
			/* make sure the node is visible (expand parent) */
			navTree.expandPath(selPaths[0].getParentPath());
			navTree.setSelectionPaths(selPaths);
			navTree.scrollPathToVisible(selPaths[0]);
		}
	}
	
	/**
	 * Overrides {@link JTree#setModel(TreeModel)} in order to add this class
	 * as a {@link TreeModelListener}
	 * 
	 * @see JTree#setModel(TreeModel)
	 */
	public void setModel(TreeModel treeModel) {
		super.setModel(treeModel);
		
		if (treeModel != null) {
			treeModel.addTreeModelListener(this);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * This is a workaround to ensure that the {@link JTree} is re-rendered 
	 * when the {@link #treeNodesChanged(TreeModelEvent)} method is fired. 
	 * By manually changing the selection, it forces the {@link JTree} to 
	 * re-display the selected nodes. 
	 * Without this workaround, nodes occasionally do not become refreshed 
	 * according to the changes in the model. 
	 * 
	 * @see TreeModelListener#treeNodesChanged(TreeModelEvent)
	 */
	public void treeNodesChanged(TreeModelEvent e) {
		// need selection change to re-render sometimes! 
		TreePath[] selPaths = getSelectionPaths();
		
		removeTreeSelectionListener(this);
		clearSelection();
		
		setSelectionPaths(selPaths);
		addTreeSelectionListener(this);
		
		// if the root node has changed (children == null), update Editable
		// (root node change is posted when Experimental info added/removed). 
		if (e.getChildren() == null) {
			boolean editable = ((! controller.isFileLocked()) 
				&& (controller.getEditingMode() == Browser.EDIT_EXPERIMENT));
			setEditable(editable);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Null implementation in this case.
	 * 
	 * @see TreeModelListener#treeNodesInserted(TreeModelEvent)
	 */
	public void treeNodesInserted(TreeModelEvent e) {}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Null implementation in this case.
	 * 
	 * @see TreeModelListener#treeNodesRemoved(TreeModelEvent)
	 */
	public void treeNodesRemoved(TreeModelEvent e) {}

	/**
	 * Implemented as specified by the {@link TreeModelListener} interface.
	 * Null implementation in this case.
	 * 
	 * @see TreeModelListener#treeStructureChanged(TreeModelEvent)
	 */
	public void treeStructureChanged(TreeModelEvent e) {}

}
