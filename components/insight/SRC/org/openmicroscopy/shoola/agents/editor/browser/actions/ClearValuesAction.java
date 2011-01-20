 /*
 * org.openmicroscopy.shoola.agents.editor.browser.actions.ClearValuesAction 
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

import javax.swing.JTree;
import javax.swing.undo.UndoableEditSupport;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.ClearValuesEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.TreeEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.UndoableTreeEdit;

/** 
 * This Action clears the values of parameters. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ClearValuesAction 
extends AbstractTreeEditAction 
implements TreeEdit
{
	
	/**
	 * Creates an instance of this class.
	 * setTree(JTree tree) needs to be called before this edit can be used.
	 * 
	 * @see	{#link BrowserAction}
	 * 
	 * @param model		Reference to the Model. Mustn't be <code>null</code>.
	 */
	public ClearValuesAction(UndoableEditSupport undoSupport, Browser model) {
		super(undoSupport, model);
		
		setName("Clear Experiment Values");
		setDescription("Delete all experimental values.");
		setIcon(IconManager.CLEAR_VALUES_ICON);  
	}
	
	/**
	 * Override this method since we don't need to add this class as an 
	 * TreeSelectionListener, and don't have an instance of UndoAbleTreeEdit
	 * to call setTree() on. 
	 */
	public void setTree(JTree tree) 
	{
		treeUI = tree;
	}
	
	/**
	 * Creates a new instance of {@link ClearValuesEdit} and posts it to
	 * undoSupport. 
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		
		if (treeUI == null) return;
		
		ClearValuesEdit edit = new ClearValuesEdit(treeUI);
		
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Implemented as specified by the {@link AbstractTreeEditAction} class
	 * 
	 * @see AbstractTreeEditAction#canDo()
	 */
	protected boolean canDo() {
		return true;
	}
	
	/**
	 * Refreshes the enabled state of the Action, based on the current 
	 * state of the {@link Browser}, and the result of 
	 * {@link UndoableTreeEdit#canDo()}, which is based on the current
	 * JTree selection paths. 
	 * 
	 * @see UndoableTreeEdit#canDo()
	 */
	protected void refreshState() 
	{
		int state = model.getEditingMode();
		if (model.isFileLocked() || state == Browser.EDIT_PROTOCOL) {
			setEnabled(false);
		}
		else {
			if (treeUI == null) {
				setEnabled(false);
			} else {
				setEnabled(canDo());
			}
		}
	}
}
