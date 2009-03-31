 /*
 * org.openmicroscopy.shoola.agents.editor.browser.actions.AddExpInfoAction 
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
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddExpInfoEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.TreeEdit;

/** 
 * This Action adds (or removes?) the experimental details to a protocol to 
 * create an Experiment. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AddExpInfoAction 
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
	public AddExpInfoAction(UndoableEditSupport undoSupport, Browser model) {
		super(undoSupport, model);
		
		setName("Create Experiment");
		setDescription("Define an Experiment from this Protocol");
		setIcon(IconManager.EXP_NEW_ICON);  
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
	 * Creates a new instance of {@link AddExpInfoEdit} and posts it to
	 * undoSupport. 
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		
		if (treeUI == null) return;
		
		AddExpInfoEdit edit = new AddExpInfoEdit(treeUI);
		edit.doEdit();
		
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Implemented as specified by the {@link AbstractTreeEditAction} class
	 * 
	 * @see AbstractTreeEditAction#canDo()
	 */
	protected boolean canDo() {
		// if we're already editing an experiment, disable. 
		return (! model.isModelExperiment());
	}
}
