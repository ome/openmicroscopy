 /*
 * org.openmicroscopy.shoola.agents.editor.browser.actions.AddTextBoxFieldAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.editor.browser.actions;

//Java imports

import java.awt.event.ActionEvent;

import javax.swing.undo.UndoableEditSupport;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddFieldEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddTextBoxFieldEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.DeleteFieldsEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.UndoableTreeEdit;

/** 
 * This adds a new "Text-Box" field/step below the last highlighted step.
 * This is a Step that contains a single text-box parameter.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AddTextBoxFieldAction 
	extends AbstractTreeEditAction 
{
	
	/**
	 * Creates an instance of this class.
	 * setTree(JTree tree) needs to be called before this edit can be used.
	 * This class wraps an instance of {@link AddTextBoxFieldEdit}, which is
	 * created and posted to the undo/redo queue by the 
	 * {@link #actionPerformed(ActionEvent)} method. 
	 * 
	 * @see	{#link AbstractEditorAction}
	 * 
	 * @param undoSupport	The UndoableSupport to post edits to undo/redo queue
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 */
	public AddTextBoxFieldAction(UndoableEditSupport undoSupport, Browser model) 
	{
		super(undoSupport, model);
		
		setName("Add Comment Step");
		setDescription("Adds a step for recording a comment");
		setIcon(IconManager.ADD_TEXTBOX_ICON); 
		
		refreshState();
	}

	/**
	 * This creates a new instance of {@link AddFieldEdit}, calls it's doEdit()
	 * method and posts it to the undo/redo queue. 
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		UndoableTreeEdit edit = new AddTextBoxFieldEdit(treeUI);
		
		edit.doEdit();
		
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Implemented as specified by the {@link AbstractTreeEditAction} class
	 * 
	 * @see AbstractTreeEditAction#canDo()
	 */
	protected boolean canDo() {
		if (treeUI == null) return false; 
		return AddTextBoxFieldEdit.canDo(treeUI.getSelectionPaths());
	}
}