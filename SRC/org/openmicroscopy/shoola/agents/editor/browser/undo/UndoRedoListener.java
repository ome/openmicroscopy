 /*
 * treeModel.undoableTreeEdits.UndoRedoListener 
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
 * A listener interface for undo() or redo() events.
 * Has a single method, undoRedoPerformed().
 * Implemented by {#link RedoEditAction} and {#link UndoEditAction}
 * so that (for example) the Redo Action becomes enabled following
 * an undo().
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface UndoRedoListener {
	
	/**
	 * Should be called following an undo() or a redo() event. 
	 * Listeners can therefore update their status based on the new 
	 * undo/redo queue. 
	 */
	public void undoRedoPerformed();

}
