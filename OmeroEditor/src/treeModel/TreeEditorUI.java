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
package treeModel;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.Border;

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
public class TreeEditorUI 
	extends JPanel {
	
	private TreeEditorModel model;
	
	private TreeEditorControl controller;
	
	
	private TreeUI treeUI;
	
	
	public void initialise(TreeEditorModel model, TreeEditorControl controller) {
		
		this.model = model;
		
		this.controller = controller;
		
		buildUI();
	}
	
	
	private void buildUI() {
		
		this.setLayout(new BorderLayout());
		
		treeUI = new TreeUI(model);
		
		Box toolBarBox = Box.createHorizontalBox();
		
		addActionButton(toolBarBox, TreeEditorControl.ADD_FIELD_ACTION);
		addActionButton(toolBarBox, TreeEditorControl.DUPLICATE_FIELDS_ACTION);
		addActionButton(toolBarBox, TreeEditorControl.DELETE_FIELD_ACTION);
		addActionButton(toolBarBox, TreeEditorControl.UNDO_ACTION);
		addActionButton(toolBarBox, TreeEditorControl.REDO_ACTION);
		
		add(toolBarBox, BorderLayout.NORTH);
		add(treeUI, BorderLayout.CENTER);
	}
	
	public void addActionButton(JComponent comp, int index) {
		Action newAction = controller.getAction(index);
		if (newAction instanceof AbstractEditorAction) {
			((AbstractEditorAction)newAction).setTree(treeUI.getJTree());
		}
		JButton newButton = new JButton(newAction);
		Border emptyBorder = BorderFactory.createEmptyBorder(4,4,4,4);
		newButton.setText(null);
		newButton.setBorder(emptyBorder);
		newButton.setBackground(null);
		comp.add(newButton);
	}

}
