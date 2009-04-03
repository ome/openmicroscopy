 /*
 * treeModel.editActions.UndoAction 
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

import util.ImageFactory;

//Third-party libraries

//Application-internal dependencies



/** 
 * This Action performs an Undo on the UndoManager specified in the 
 * constructor. 
 * It listens both to the posting of UndoableEdits to the undoSupport
 * and to the undo and redo actions of the undoManager.
 * In each case, the Action is enabled depending on the canUndo() method
 * of the undoManager and the Description of this Action is refreshed.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class UndoEditAction 
	extends AbstractAction 
	implements UndoableEditListener,
	UndoRedoListener {
	
	UndoManager undoManager;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param undoManager		The undoManager for undo()
	 * @param undoSupport		The UndoableEditSupport, for listening to 
	 * 							posting of new edits. 
	 */
	public UndoEditAction(UndoManager undoManager, UndoableEditSupport undoSupport) {
		this.undoManager = undoManager;
		undoSupport.addUndoableEditListener(this);
		if (undoManager instanceof UndoRedoObservable) {
			((UndoRedoObservable)undoManager).addUndoRedoListener(this);
		}
		
		putValue(Action.NAME, "Undo");
		putValue(Action.SMALL_ICON, ImageFactory.getInstance().getIcon(ImageFactory.UNDO_ICON)); 
		
		refreshStatus(); 	// sets description, enabled etc.
	}

	/**
	 * Performs the Undo action.
	 * This will also cause the undoRedoPerformed() method to be called.
	 */
	public void actionPerformed(ActionEvent e) {
		if (undoManager.canUndo())
			undoManager.undo();
	}

	/**
	 * This method is called when an UndoableEdit is posted to the undo/redo
	 * queue.
	 * The Action's enabled status and description is refreshed.
	 */
	public void undoableEditHappened(UndoableEditEvent e) {
		refreshStatus();
	}
	
	/**
	 * This method is called when undo or redo occurs at the undoManager. 
	 * The Action's enabled status and description is refreshed.
	 */
	public void undoRedoPerformed() {
		refreshStatus();
	}
	
	private void refreshStatus() {
		boolean undo = undoManager.canUndo();
		setEnabled(undo);
		putValue(Action.SHORT_DESCRIPTION, 
				undo ? undoManager.getUndoPresentationName() : "Can't Undo");
	}

}
