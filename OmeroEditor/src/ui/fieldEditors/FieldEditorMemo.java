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

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.AttributeMemoEditor;


public class FieldEditorMemo extends FieldEditor {
	
private AttributeMemoEditor defaultFieldEditor;
	
	public FieldEditorMemo(IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		String defaultValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		if (defaultValue == null) defaultValue = "";
		
		defaultFieldEditor = new AttributeMemoEditor
			(dataField, "Default Text: ", DataFieldConstants.DEFAULT, defaultValue);
		defaultFieldEditor.setTextAreaRows(3);
		attributeFieldsPanel.add(defaultFieldEditor);
		
		// this is called by the super() constructor, but at that time
		// not all components will have been instantiated. Calls enableEditing();
		refreshLockedStatus();
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		defaultFieldEditor.setTextAreaText(dataField.getAttribute(DataFieldConstants.DEFAULT));
	}
	
	/**
	 * This is called by the superclass FieldEditor.dataFieldUpdated().
	 * Need to refresh the enabled status of additional components in this subclass. 
	 */
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);
		
		// need to check != null because this is called by the super() constructor
		// before all subclass components have been instantiated. 
		if (defaultFieldEditor != null) {
			defaultFieldEditor.getTextArea().setEnabled(enabled);
		}
	}

}
