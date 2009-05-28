 /*
 * org.openmicroscopy.shoola.agents.editor.browser.actions.AbstractEditorAction 
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
package org.openmicroscopy.shoola.agents.editor.browser.actions;

//Java imports

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.undo.UndoableEditSupport;

import org.openmicroscopy.shoola.agents.editor.browser.Browser;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.TreeEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.UndoableTreeEdit;

//Third-party libraries

//Application-internal dependencies

/** 
 * The superclass of Actions used for editing the TreeModel. 
 * This class needs a reference to a JTree before it can be
 * used for editing.
 * This class is a wrapper for an instance of {@link UndoableTreeEdit},
 * which is an {@link UndoableEdit} that also has a canDo() method, based 
 * on the currently selected paths of the {@link JTree}.
 * This Action listens to selection changes to the JTree and refreshes the
 * enabled status of the Action according to the new selection.
 * It also extends {@link BrowserAction} and it's enabled status is 
 * updated with changes to the {@link Browser} status.
 * 
 * The {@link ActionListener#actionPerformed(ActionEvent)} method of this 
 * action needs to be defined by subclasses. 
 * They should instantiate a new instance of the {@link UndoableEdit} 
 * and post it to the {@link #undoSupport}. A new instance is
 * needed so that multiple instances of the UndoableEdit can be
 * added to the same undo/redo queue. 
 * 
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public abstract class AbstractTreeEditAction 
	extends BrowserAction 
	implements TreeSelectionListener,
	TreeEdit
{
	
	/**
	 * The JTree that will be edited by subclasses of this Action.
	 * This is also used as a source of selection paths. 
	 */
	protected JTree 				treeUI;
	
	/**
	 * The UndoableEditSupport where new instances of UndoableEdit should
	 * be posted by the actionPerformed() method.
	 */
	protected UndoableEditSupport 	undoSupport;
	
	
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
		if (model.isFileLocked() || state == Browser.EDIT_EXPERIMENT) {
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
	
	protected abstract boolean canDo();

	/**
	 * This method is called when the state of the {@link Browser} changes.
	 * Calls {@link #refreshState()}
	 * 
	 * @see #refreshState()
	 */
	protected void onStateChange() { refreshState(); }

	/**
	 * Creates an instance of this class.
	 * Following instantiation, you need to call setTree(JTree) on
	 * the new instance before it can be used to edit a JTree. 
	 * 
	 * @param undoSupport	The UndoableEditSupport used to post edits,
	 * 						allowing the action to be undone. 
	 */
	public AbstractTreeEditAction(UndoableEditSupport undoSupport, Browser model) 
	{
		super(model);
		this.undoSupport = undoSupport;
	}
	
	/**
	 * This sets the JTree to be edited and used for getting selected paths. 
	 * This must be set before the Action becomes enabled and can be used
	 * for editing. 
	 * 
	 * @param tree
	 */
	public void setTree(JTree tree) 
	{
		if (tree == null) return;
		
		// If the JTree has previously been set, need to remove this
		// as a listener (don't want to listen to more than one tree)
		if (treeUI != null) {
			treeUI.removeTreeSelectionListener(this);
		}
		
		// Set the tree, and add this as a selectionListener
		treeUI = tree;
		treeUI.addTreeSelectionListener(this);
		
		//...and refresh the enabled status of this action. 
		refreshState();
	}

	/**
	 * This method is called when the selection changes on the JTree.
	 * It refreshes the enabled status of this Action based on the
	 * currently selected paths of the JTree.
	 * 
	 * @see TreeSelectionListener#valueChanged(TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent e) { refreshState(); }

}
