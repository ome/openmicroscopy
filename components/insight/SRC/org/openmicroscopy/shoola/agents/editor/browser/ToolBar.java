 /*
 * org.openmicroscopy.shoola.agents.editor.browser.ToolBar 
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

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.actions.AbstractTreeEditAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.CopyFieldsAction;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.TreeEdit;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;

/** 
 * A toolBar for the browser. 
 * 
 * The toolBar must be created with a {@link Browser} and a {@link JTree},
 * so that the Editing actions can be assigned to the JTree's model. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ToolBar 
	extends JPanel {
	
	/**
	 * The controller.
	 */
	private BrowserControl 			controller;

	/**
	 * The JTree that this tool-bar actions will edit. 
	 * Actions listen to selection changes etc. to modify enabled state, and
	 * editing actions are applied to this tree's treeModel.
	 */
	private JTree 					treeUI;
	
	/**
	 * Builds the UI. 
	 */
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		bar.setRollover(true);
		bar.setBorder(null);
		
		// add(createButton(BrowserControl.EDIT)); Toggle editing. 
		bar.add(createButton(BrowserControl.ADD_FIELD_ACTION));
		bar.add(createButton(BrowserControl.ADD_TEXTBOX_FIELD_ACTION));
		bar.add(createButton(BrowserControl.DELETE_FIELD_ACTION));
		bar.add(createButton(BrowserControl.INDENT_LEFT_ACTION));
		bar.add(createButton(BrowserControl.INDENT_RIGHT_ACTION));
		bar.add(createButton(BrowserControl.MOVE_UP_ACTION));
		bar.add(createButton(BrowserControl.MOVE_DOWN_ACTION));
		
		bar.add(new JSeparator(SwingConstants.VERTICAL));
		bar.add(createButton(BrowserControl.UNDO_ACTION));
		bar.add(createButton(BrowserControl.REDO_ACTION));
		
		bar.add(new JSeparator(SwingConstants.VERTICAL));
		bar.add(createButton(BrowserControl.ADD_EXP_INFO_ACTION));
		bar.add(createButton(BrowserControl.CLEAR_VALUES_ACTION));
		add(bar);
		
		bar.add(new JSeparator(SwingConstants.VERTICAL));
		bar.add(createButton(BrowserControl.COPY_FIELDS_ACTION));
		bar.add(createButton(BrowserControl.PASTE_FIELDS_ACTION));
		
	}
	
	/**
	 * Convenience method for creating buttons. 
	 * If Actions are {@link TreeEdit}s, call the setTree() method to 
	 * pass a reference to the JTree in the UI. 
	 * 
	 * @param actionID		The ID of the action, retrieved from controller.
	 * @return			A button displaying the specified action.
	 */
	private JButton createButton(int actionID)
	{
		Action action = controller.getAction(actionID);
		if (action instanceof TreeEdit) {
			((TreeEdit)action).setTree(treeUI);
		}
		JButton b = new CustomButton(action);
		b.setText("");
		return b;
	}
	
	/**
	 * Creates an instance.
	 * 
	 * @param controller		The controller as a source of Actions.
	 */
	ToolBar(BrowserControl controller, JTree tree)
	{
		if (controller == null) 
            throw new NullPointerException("No controller.");
		if (tree == null) 
            throw new NullPointerException("No JTree.");
        this.controller = controller;
        this.treeUI = tree;
        buildUI();
	}
}
