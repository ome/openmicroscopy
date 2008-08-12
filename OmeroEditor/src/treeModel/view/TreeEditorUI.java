 /*
 * treeModel.TreeEditorUI 
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

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.border.Border;

import treeModel.TreeEditorControl;
import treeModel.TreeEditorModel;
import treeModel.editActions.AbstractEditorAction;

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
public class TreeEditorUI {
	
	private TreeEditorModel model;
	
	private TreeEditorControl controller;
	
	public static final int EDITABLE_TREE = 0;
	
	public static final int NON_EDITABLE_TREE = 1;
	
	private TreeUI treeUI;
	
	
	public void initialise(TreeEditorModel model, TreeEditorControl controller) {
		
		this.model = model;
		
		this.controller = controller;
	}
	
	public JComponent getUI() {
		
		JPanel fullUI = new JPanel(new BorderLayout());

		TreeUI treeUI = getTreeUI(EDITABLE_TREE);
		JTree tree = treeUI.getJTree();
		
		fullUI.add(treeUI, BorderLayout.CENTER);
		fullUI.add(getFieldEditor(tree), BorderLayout.EAST);
		fullUI.add(getToolBar(tree), BorderLayout.NORTH);
		
		return fullUI;
	}
	
	public TreeUI getTreeUI(int treeType) {
		return TreeUI.createInstance(model, controller, treeType);
	}
	
	public JComponent getFieldEditor(JTree tree) {
		return new FieldEditorDisplay(tree);
	}
	
	public JComponent getToolBar(JTree tree) {
		
		Box toolBarBox = Box.createHorizontalBox();
		
		addActionButton(toolBarBox, TreeEditorControl.ADD_FIELD_ACTION, tree);
		addActionButton(toolBarBox, TreeEditorControl.DUPLICATE_FIELDS_ACTION, tree);
		addActionButton(toolBarBox, TreeEditorControl.DELETE_FIELD_ACTION, tree);
		addActionButton(toolBarBox, TreeEditorControl.UNDO_ACTION, tree);
		addActionButton(toolBarBox, TreeEditorControl.REDO_ACTION, tree);
		
		return toolBarBox;
	}
	
	public void addActionButton(JComponent comp, int index, JTree tr) {
		Action newAction = controller.getAction(index);
		if (newAction instanceof AbstractEditorAction) {
			((AbstractEditorAction)newAction).setTree(tr);
		}
		JButton newButton = new JButton(newAction);
		Border emptyBorder = BorderFactory.createEmptyBorder(4,4,4,4);
		newButton.setText(null);
		newButton.setBorder(emptyBorder);
		newButton.setBackground(null);
		comp.add(newButton);
	}

}
