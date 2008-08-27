 /*
 * treeModel.view.NonEditableTree 
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
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * This class extends JTree, with a cell renderer that displays the 
 * Fields of the tree as Panels. 
 * This tree is not editable. Can browse only. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class NonEditableTree 
	extends JTree {
	
	/**
	 * Creates an instance of the tree.
	 * 
	 * @param model		The treeModel to display in the tree.
	 */
	public NonEditableTree(TreeModel model) {
		super(model);
		
		configureTree();
	}
	
	/**
	 * Creates an instance of the tree.
	 * Need to call setModel(treeModel)..
	 */
	public NonEditableTree() {
		super();
		
		configureTree();
	}
	
	/**
	 * Sets the renderer and selectionMode.
	 */
	public void configureTree() {
		
		this.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		
		/*
		 * Setting the row height to 0 allows each node to choose it's
		 * own size. The JTree will call getPreferredSize() for each.
		 */
		setRowHeight(0);
		
		/*
		 * A custom TreeCellRenderer (extends DefaultTreeCellRenderer)
		 * renders nodes as JPanels.
		 * The field renderer will pass a null reference (for controller) to 
		 * the fields, so they won't be able to edit fields,
		 *  undo/redo edits etc. 
		 *  But that is OK, since this is a non-editable tree! 
		 */
		DefaultTreeCellRenderer fieldRenderer = new FieldRenderer(null);
		setCellRenderer(fieldRenderer);
		
		
		
	}

}
