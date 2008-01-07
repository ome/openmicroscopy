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

import tree.DataField;
import ui.components.AttributeMemoEditor;


public class FieldEditorMemo extends FieldEditor {
	
private AttributeMemoEditor defaultFieldEditor;
	
	public FieldEditorMemo(DataField dataField) {
		
		super(dataField);
		
		String defaultValue = dataField.getAttribute(DataField.DEFAULT);
		if (defaultValue == null) defaultValue = "";
		
		defaultFieldEditor = new AttributeMemoEditor
			(dataField, "Default Text: ", DataField.DEFAULT, defaultValue);
		defaultFieldEditor.setTextAreaRows(3);
		attributeFieldsPanel.add(defaultFieldEditor);
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		defaultFieldEditor.setTextAreaText(dataField.getAttribute(DataField.DEFAULT));
	}

}
