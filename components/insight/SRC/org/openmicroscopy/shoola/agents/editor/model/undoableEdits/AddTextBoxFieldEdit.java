 /*
 * org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddTextBoxFieldEdit 
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

import javax.swing.JTree;

import org.openmicroscopy.shoola.agents.editor.model.TextBoxStep;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Adds a Field/Step that contains a single parameter. 
 * Extends the {@link AddFieldEdit} to instantiate a different type of
 * field. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class AddTextBoxFieldEdit 
	extends AddFieldEdit {

	public AddTextBoxFieldEdit(JTree tree) {
		super(tree);
		
		// pass a string to create a Text-Box parameter with no text set.
		field = new TextBoxStep("");
	}
	
	/**
	 * Presentation name is "Add Comment Step"
	 */
	public String getPresentationName() {
		     return "Add Comment Step";
	}

}
