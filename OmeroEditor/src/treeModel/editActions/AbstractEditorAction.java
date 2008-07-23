 /*
 * treeModel.editActions.AbstractEditorAction 
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

import javax.swing.AbstractAction;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.undo.UndoableEditSupport;

//Third-party libraries

//Application-internal dependencies

import treeModel.undoableTreeEdits.UndoableTreeEdit;


/** 
 * The superclass of Actions used for editing the TreeModel. 
 * This class needs a reference to a JTree before it can be
 * used for editing.
 * This class is a wrapper for an instance of UndoableTreeEdit,
 * which is an UndoableEdit that also has a canDo() method, based 
 * on the currently selected paths of the JTree.
 * This Action listens to selection changes to the JTree and refreshes the
 * enabled status of the Action according to the new selection.
 * The ActionPerformed() method of this action needs to be 
 * defined by subclasses. They should instantiate a new instance of
 * the UndoableEdit and post it to the undoSupport. A new instance is
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
public abstract class AbstractEditorAction 
	extends AbstractAction 
	implements TreeSelectionListener {
	
	/**
	 * The JTree that will be edited by subclasses of this Action.
	 * This is also used as a source of selection paths. 
	 */
	protected JTree treeUI;
	
	/**
	 * The UndoableEditSupport where new instances of UndoableEdit should
	 * be posted by the actionPerformed() method.
	 */
	protected UndoableEditSupport undoSupport;
	
	/**
	 * An instance of UndoableTreeEdit which is used to set the enabled 
	 * status of this Action, using the canDo() method, based on the
	 * currently selected paths of the JTree. 
	 */
	protected UndoableTreeEdit undoableTreeEdit;
	
	/**
	 * Creates an instance of this class.
	 * Following instantiation, you need to call setTree(JTree) on
	 * the new instance before it can be used to edit a JTree. 
	 * 
	 * @param undoSupport	The UndoableEditSupport used to post edits,
	 * 						allowing the action to be undone. 
	 */
	public AbstractEditorAction(UndoableEditSupport undoSupport) {
		
		this.undoSupport = undoSupport;
	}
	
	/**
	 * This method is called when the selection changes on the JTree.
	 * It refreshes the enabled status of this Action based on the
	 * currently selected paths of the JTree.
	 */
	public void valueChanged(TreeSelectionEvent e) {
		setEnabled(undoableTreeEdit.canDo());
	}
	
	/**
	 * This sets the JTree to be edited and used for getting selected paths. 
	 * This must be set before the Action becomes enabled and can be used
	 * for editing. 
	 * 
	 * @param tree
	 */
	public void setTree(JTree tree) {
		/*
		 * If the JTree has previously been set, need to remove this
		 * as a listener. 
		 */
		if (treeUI != null) {
			treeUI.removeTreeSelectionListener(this);
		}
		
		/*
		 * Set the tree, and add this as a selectionListener
		 */
		treeUI = tree;
		treeUI.addTreeSelectionListener(this);
		
		/*
		 * Pass on the JTree to the UndoableEdit, so that it can
		 * be used for the canDo() method, based on selected paths...
		 */
		undoableTreeEdit.setTree(treeUI);
		
		/*
		 *...and refresh the enabled status of this action. 
		 */
		setEnabled(undoableTreeEdit.canDo());
	}

}
