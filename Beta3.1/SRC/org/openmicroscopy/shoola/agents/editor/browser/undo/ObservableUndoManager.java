 /*
 * treeModel.undoableTreeEdits.MyUndoManager 
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
package org.openmicroscopy.shoola.agents.editor.browser.undo;

//Java imports

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.UndoManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * A subclass of UndoManager that allows UndoRedoListeners to register for
 * notification of undo() or redo() events. 
 * This allows UndoRedoListeners to update their state, based on the 
 * undo/redo queue. Eg Redo button should become enabled when the Undo
 * action is performed. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ObservableUndoManager 
	extends UndoManager 
	implements UndoRedoObservable 
{
	
	/**
	 * A list of UndoRedoListeners that will be notified of undo or redo events
	 */
	private List<UndoRedoListener> listeners = new ArrayList<UndoRedoListener>();

	/**
	 * Register as a listener.
	 * Implemented as specified by 
	 * {@link UndoRedoObservable#addUndoRedoListener(UndoRedoListener)}
	 */
	public void addUndoRedoListener(UndoRedoListener listener) 
	{
		listeners.add(listener);
	}
	
	/**
	 * Remove a listener
	 * Implemented as specified by 
	 * {@link UndoRedoObservable#removeUndoRedoListener(UndoRedoListener)}
	 */
	public void removeUndoRedoListener(UndoRedoListener listener) 
	{
		listeners.remove(listener);
	}
	
	/**
	 * This delegates undo() to the superclass, then calls notifyListeners()
	 * 
	 * @see UndoManager#undo();
	 */
	public void undo() {
		super.undo();
		notifyListeners();
	}
	
	/**
	 * This delegates redo() to the superclass, then calls notifyListeners()
	 * 
	 * @see UndoManager#redo();
	 */
	public void redo() 
	{
		super.redo();
		notifyListeners();
	}
	
	/**
	 * This calls the undoRedoPerformed() method for all the UndoRedoListeners
	 * in the listeners list. 
	 * Implemented as specified by 
	 * {@link UndoRedoObservable#notifyListeners()}
	 */
	public void notifyListeners() {
		for (UndoRedoListener listener : listeners) {
			listener.undoRedoPerformed();
		}
	}
}
