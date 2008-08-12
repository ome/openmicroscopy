 /*
 * treeModel.TreeUI 
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

//Java imports

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;

import treeModel.TreeEditorControl;
import treeModel.TreeEditorModel;

//Third-party libraries

//Application-internal dependencies



/** 
 * This UI class displays a JTree in a scroll pane. 
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
public class TreeUI 
	extends JPanel {
	
	private JTree tree;
	
	public static TreeUI createInstance(TreeEditorModel model, 
			TreeEditorControl controller,
			int treeType) {
		
		switch(treeType) {
		
		case TreeEditorUI.EDITABLE_TREE: {
			JTree t = new EditableJTree(model, controller);
			return new TreeUI(t);
		}
		case TreeEditorUI.NON_EDITABLE_TREE: {
			JTree t = new NonEditableTree(model, controller);
			return new TreeUI(t);
		}
		}
		return new TreeUI(new JTree(model));
	}
	
	public TreeUI(JTree tree) {
		
		super(new BorderLayout());
		
		this.tree = tree;
		
		/*
		 * Place the JTree in a ScrollPane and add it to this panel. 
		 */
		JScrollPane treeScroller = new JScrollPane(tree);
		add(treeScroller, BorderLayout.CENTER);
	}

	public JTree getJTree() {
		return tree;
	}
	
}
