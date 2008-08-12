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
package treeModel.view;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeSelectionModel;

import treeModel.TreeEditorControl;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
	
	public NonEditableTree(TreeModel model, TreeEditorControl controller) {
		super(model);
		
		configureTree(controller);
	}
	
	public void configureTree(TreeEditorControl controller) {
		
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
		 * The field renderer will pass a reference of the controller to 
		 * the fields, so that they can call undo/redo edits etc. 
		 */
		DefaultTreeCellRenderer fieldRenderer = new FieldRenderer(controller);
		setCellRenderer(fieldRenderer);
		
		
		
	}

}
