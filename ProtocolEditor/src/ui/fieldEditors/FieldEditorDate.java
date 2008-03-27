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

package ui.fieldEditors;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.AttributeEditor;

public class FieldEditorDate extends FieldEditor {
	
	AttributeEditor defaultFieldEditor;
	
	static DateFormat fDateFormat = DateFormat.getDateInstance (DateFormat.MEDIUM);
	
	public FieldEditorDate (IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		
		
		Date now = new Date ();

	    // Format the time string.
	    String defaultDate = fDateFormat.format (now);
	    
	    dataField.setAttribute(DataFieldConstants.DEFAULT, defaultDate, false);
		
		defaultFieldEditor = new AttributeEditor(dataField, "Default: ", DataFieldConstants.DEFAULT, defaultDate);
		// don't allow users to set any other default data!
		defaultFieldEditor.getTextField().setEnabled(false);
		attributeFieldsPanel.add(defaultFieldEditor);
		
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		defaultFieldEditor.setTextFieldText(dataField.getAttribute(DataFieldConstants.DEFAULT));
	}

	/**
	 * Convert the String date eg "Feb 21, 2005" to a Date object.
	 * Returns null if string is null, or incorrectly formatted. 
	 * 
	 * @param formattedDate		String representation of a date, eg "Jan 4, 2008"
	 * @return		A Date object that corresponds to the string.
	 */
	public static Date getDateFromString(String formattedDate) {
		if (formattedDate == null)
			return null;
		try {
			return fDateFormat.parse(formattedDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
