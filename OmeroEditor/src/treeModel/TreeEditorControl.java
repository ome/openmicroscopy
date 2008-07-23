 /*
 * treeModel.TreeEditorControl 
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
package treeModel;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import treeModel.editActions.DeleteFieldsAction;
import treeModel.editActions.RedoEditAction;
import treeModel.editActions.UndoEditAction;
import treeModel.undoableTreeEdits.ObservableUndoManager;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TreeEditorControl {

	private ITreeEditor model;
	
	private TreeEditorUI view;
	
	private Map<Integer, Action> actions;
	
	UndoManager undoManager;
	
	UndoableEditSupport undoSupport;
	
	public static final Integer DELETE_FIELD_ACTION = new Integer(1);
	
	public static final Integer UNDO_ACTION = new Integer(2);
	
	public static final Integer REDO_ACTION = new Integer(3);
	
	public void initialise(ITreeEditor model, TreeEditorUI view) {
		
		this.model = model;
		
		this.view = view;
		
		// initialize the undo.redo system
	      undoManager = new ObservableUndoManager();	// implements UndoRedoObservable
	      undoSupport = new UndoableEditSupport();
	      undoSupport.addUndoableEditListener(new UndoAdapter());
		
		createActions();
	}
	
	private void createActions() {
		
		actions = new HashMap<Integer, Action>();
		
		actions.put(DELETE_FIELD_ACTION, new DeleteFieldsAction(undoSupport));
		actions.put(UNDO_ACTION, new UndoEditAction(undoManager, undoSupport));
		actions.put(REDO_ACTION, new RedoEditAction(undoManager, undoSupport));
		
	}
	
	public Action getAction(int actionIndex) {
		
		return actions.get(actionIndex);
	}
	
	/**
	  * An undo/redo adpater. The adpater is notified when
	  * an undo edit occur(e.g. add or remove from the list)
	  * The adptor extract the edit from the event, add it
	  * to the UndoManager, and refresh the GUI
	  * http://www.javaworld.com/javaworld/jw-06-1998/jw-06-undoredo.html
	  */
	private class UndoAdapter implements UndoableEditListener {
	     public void undoableEditHappened (UndoableEditEvent evt) {
	     	UndoableEdit edit = evt.getEdit();
	     	undoManager.addEdit( edit );
	     	//refreshUndoRedo();
	     }
	  }
}
