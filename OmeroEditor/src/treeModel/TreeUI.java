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
package treeModel;

//Java imports

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;

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
	
	public TreeUI(TreeEditorModel model) {
		
		super(new BorderLayout());

		
		tree = new JTree(model);
		/*
		 * The default UI (BasicTreeUI) is replaced with a subclass
		 * to modify the selection and editing behavior.
		 */
		tree.setUI(new MyBasicTreeUI());
		
		/*
		 * A custom selection model allows multiple nodes to be selected,
		 * but ensures that they are contiguous and are all siblings.
		 */
		tree.setSelectionModel(new ContiguousChildSelectionModel());
		
		/*
		 * Setting the row height to 0 allows each node to choose it's
		 * own size. The JTree will call getPreferredSize() for each.
		 */
		tree.setRowHeight(0);
		
		/*
		 * A custom TreeCellRenderer (extends DefaultTreeCellRenderer)
		 * renders nodes as JPanels.
		 */
		DefaultTreeCellRenderer fieldRenderer = new FieldRenderer();
		tree.setCellRenderer(fieldRenderer);
		
		/*
		 * A TreeCellEditor for editing fields.
		 * This merely delegates to the fieldRenderer because the same 
		 * components are used for display and editing of the tree Cells.
		 */
		TreeCellEditor fieldEditor = new DefaultFieldEditor(fieldRenderer);
		/*
		 * The DefaultTreeCellEditor (when passed a TreeCellEditor) uses this 
		 * to switch between editing and display. 
		 */
	    TreeCellEditor editor = new DefaultTreeCellEditor(tree, fieldRenderer, fieldEditor);
	    tree.setCellEditor(editor);
		
		tree.setEditable(true);
		
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
