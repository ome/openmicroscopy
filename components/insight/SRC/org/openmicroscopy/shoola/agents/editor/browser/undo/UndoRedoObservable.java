 /*
 * treeModel.undoableTreeEdits.UndoRedoObservable 
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

//Third-party libraries

//Application-internal dependencies

/** 
 * This Interface should be implemented by an UndoManager that wants to 
 * notify listeners of undo() and redo() events. 
 * Implemented by {#link ObservableUndoManager}.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface UndoRedoObservable {
	
	/**
	 * Allows UndoRedoListeners to register for undo() or redo() notification
	 * 
	 * @param listener	The UndoRedoListener to register
	 */
	public void addUndoRedoListener(UndoRedoListener listener);
	
	/**
	 * Allows UndoRedoListeners to un-register for undo() or redo() notification
	 * 
	 * @param listener	The UndoRedoListener to un-register
	 */
	public void removeUndoRedoListener(UndoRedoListener listener);
	
	/**
	 * Calls the undoRedoPerformed() method of listeners. 
	 */
	public void notifyListeners();

}
