package actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.ChangeEvent;

import tree.Tree.Actions;
import ui.IModel;
import util.ImageFactory;

/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

/** 
 *  Add a new Node/Field to the current Tree, either after the last currently highlighted field,
 *  or at the "end" of the Tree. 
 */

public class AddFieldAction 
	extends ProtocolEditorAction {
	
	public AddFieldAction(IModel model) {

		super(model);
	
		putValue(Action.NAME, "Add Field");
		putValue(Action.SHORT_DESCRIPTION, "Add a field to the template");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.ADD_ICON)); 
	}
	
	public void actionPerformed(ActionEvent e) {
		// add dataField after last selected one
		model.editCurrentTree(Actions.ADD_NEW_FIELD);
	}
	
	
	
	public void stateChanged(ChangeEvent e) {
		
		/*
		 * This action should only be enabled if a file is open and the
		 * currently highlighted fields are unlocked. 
		 */
		setEnabled(fieldTemplatesEditable());
	
	}

}
