/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui;

import java.text.DateFormat;
import java.util.Date;

import tree.DataField;

public class FieldEditorDate extends FieldEditor {
	
	AttributeEditor defaultFieldEditor;
	
	public FieldEditorDate (DataField dataField) {
		
		super(dataField);
		
		DateFormat fDateFormat = DateFormat.getDateInstance (DateFormat.MEDIUM);
		
		Date now = new Date ();

	    // Format the time string.
	    String defaultDate = fDateFormat.format (now);
	    
	    dataField.setAttribute(DataField.DEFAULT, defaultDate, false);
		
		defaultFieldEditor = new AttributeEditor(dataField, "Default: ", DataField.DEFAULT, defaultDate);
		// don't allow users to set any other default data!
		defaultFieldEditor.setEnabled(false);
		attributeFieldsPanel.add(defaultFieldEditor);
		
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		defaultFieldEditor.setTextFieldText(dataField.getAttribute(DataField.DEFAULT));
	}

}
