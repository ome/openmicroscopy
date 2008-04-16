package actions;

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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import tree.DataFieldConstants;
import tree.Tree.Actions;
import ui.IModel;

/**
 * This Action class uses the editCurrentTree() method to pass an Enumerated instance of 
 * Tree.Actions to the Tree, via the model. 
 * The Tree will perform the stated action on the currently highlighted fields, or on all fields,
 * depending on the Action. 
 * 
 * @author will
 *
 */
public class ClearFieldsAllAction extends ProtocolEditorAction {
	
	public static final String TOOL_TIP_TEXT = "Clear the parameter values for all fields in the current file";
	
	public ClearFieldsAllAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Clear Values for All fields");
		putValue(Action.SHORT_DESCRIPTION, TOOL_TIP_TEXT);
		//putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.LOAD_DEFAULTS_ICON)); 
	}
	
	/**
	 * delegates to the model, which delegates to the Tree
	 */
	public void actionPerformed(ActionEvent e) {
		model.editCurrentTree(Actions.CLEAR_FIELDS);
	}
	
	
	/**
	 * Action is disabled if no files are open
	 */
	public void stateChanged(ChangeEvent e) {
		
		/*
		 * This action should only be enabled if a file is open etc and
		 *  NO fields are locked. 
		 */
		boolean enabled = filesOpen();
		if (enabled) {
			String lockLevel = model.getMaxLockingLevel();
			if (lockLevel != null) {
				enabled = (lockLevel.equals(DataFieldConstants.LOCKED_TEMPLATE));
			}
		}
		setEnabled(enabled);
	}
}
