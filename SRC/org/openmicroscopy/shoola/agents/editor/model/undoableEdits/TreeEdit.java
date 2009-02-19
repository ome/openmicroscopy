 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.TreeEdit 
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
package org.openmicroscopy.shoola.agents.editor.model.undoableEdits;

//Java imports

import javax.swing.JTree;

//Third-party libraries

//Application-internal dependencies

/** 
 * This interface is implemented by Undoable-Edits (and their corresponding
 * Actions) that require an instance of JTree to act on. 
 * Typically these actions are created by the controller, before the 
 * Tree Model and JTree are created. 
 * Most of these edits/ actions depend on the selection of JTree to determine
 * their canDo/ enabled state, while others simply need a reference to the
 * Tree Model (obtained via the JTree - e.g. Copy and AddExpInfo). 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public interface TreeEdit {

	public void setTree(JTree tree);
}
