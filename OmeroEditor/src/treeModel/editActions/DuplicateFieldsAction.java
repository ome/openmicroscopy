 /*
 * treeModel.editActions.DuplicateFieldsAction 
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
package treeModel.editActions;

//Java imports

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.undo.UndoableEditSupport;

//Third-party libraries

//Application-internal dependencies

import treeModel.undoableTreeEdits.AddFieldEdit;
import treeModel.undoableTreeEdits.DuplicateFieldsEdit;
import treeModel.undoableTreeEdits.UndoableTreeEdit;
import util.ImageFactory;


/** 
 * This Action is used to duplicate nodes in a JTree, adding the 
 * new nodes after the last selected node. 
 * The setTree(JTree) method must be called before this Action can 
 * be used.
 * This class wraps an instance of the UndoableEdit subclass 
 * DuplicateFieldsEdit.
 * On actionPerformed(), it creates a new instance of this class and
 * posts it to the undo/redo queue specified in the constructor.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class DuplicateFieldsAction 
	extends AbstractEditorAction {
	
	/**
	 * Creates an instance of this class.
	 * setTree(JTree tree) needs to be called before this edit can be used.
	 * This class wraps an instance of DuplicateFieldsEdit, to which the 
	 * actionPerformed() method delegates. 
	 * 
	 * @see	{#link AbstractEditorAction}
	 * 
	 * @param undoSupport	The UndoableSupport to post edits to the undo/redo queue
	 */
	public DuplicateFieldsAction(UndoableEditSupport undoSupport) {
		super(undoSupport);
		
		/*
		 * treeUI is null at this point.
		 */
		undoableTreeEdit = new DuplicateFieldsEdit(treeUI);
		
		putValue(Action.NAME, "Duplicate Fields");
		putValue(Action.SHORT_DESCRIPTION, "Add a new field");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(
				ImageFactory.DUPLICATE_ICON)); 
	}

	/**
	 * This creates a new instance of DuplicateFieldsEdit, calls it's doEdit()
	 * method and posts it to the undo/redo queue. 
	 */
	public void actionPerformed(ActionEvent e) {
		UndoableTreeEdit edit = new DuplicateFieldsEdit(treeUI);
		
		edit.doEdit();
		
		undoSupport.postEdit(edit);
	}

	

}
