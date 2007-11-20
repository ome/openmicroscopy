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

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JTextField;

import tree.DataField;
import ui.FormField.FormPanelMouseListener;

public class FieldEditorNumber extends FieldEditor {
	
	private AttributeEditor defaultFieldEditor;
	private AttributeEditor unitsFieldEditor;
	
	public FieldEditorNumber (DataField dataField) {
		
		super(dataField);
		
		String defaultValueString = dataField.getAttribute(DataField.DEFAULT);
		String units = dataField.getAttribute(DataField.UNITS);
		
		defaultFieldEditor = new AttributeEditor("Default: ", DataField.DEFAULT, defaultValueString);
		defaultFieldEditor.getTextField().addFocusListener(new NumberCheckerListener());
		attributeFieldsPanel.add(defaultFieldEditor);
		
		unitsFieldEditor = new AttributeEditor("Units: ", DataField.UNITS, units);
		attributeFieldsPanel.add(unitsFieldEditor);
	}

	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		defaultFieldEditor.setTextFieldText(dataField.getAttribute(DataField.DEFAULT));
		checkForNumber();
		unitsFieldEditor.setTextFieldText(dataField.getAttribute(DataField.UNITS));
	}
	
	public class NumberCheckerListener implements FocusListener {
		public void focusLost(FocusEvent event){	
			checkForNumber();
		}
		public void focusGained(FocusEvent event){	}
	}
	
	private void checkForNumber() {
		String number = defaultFieldEditor.getTextFieldText();
		try {
			if (number.length() > 0) {
				float value = Float.parseFloat(number);
				defaultFieldEditor.getTextField().setBackground(Color.WHITE);
			}
		}catch (Exception ex) {
			defaultFieldEditor.getTextField().setBackground(Color.RED);
		}
	}

}
