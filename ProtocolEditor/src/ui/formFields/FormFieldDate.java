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

package ui.formFields;

import java.awt.Color;

import javax.swing.JTextField;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;

public class FormFieldDate extends FormField {
	
	JTextField textInput;
	
	
	public FormFieldDate(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		String date = dataField.getAttribute(DataFieldConstants.VALUE);
		
		textInput = new JTextField(date);
		visibleAttributes.add(textInput);
		textInput.addFocusListener(componentFocusListener);
		textInput.setName(DataFieldConstants.VALUE);
		textInput.addFocusListener(focusChangedListener);
		textInput.addKeyListener(textChangedListener);
		horizontalBox.add(textInput);
		
		// enable or disable components based on the locked status of this field
		refreshLockedStatus();
	}
	
	/**
	 * This simply enables or disables all the editable components of the 
	 * FormField.
	 * Gets called (via refreshLockedStatus() ) from dataFieldUpdated()
	 * 
	 * @param enabled
	 */
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);	
		
		if (textInput != null)
			textInput.setEnabled(enabled);
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		textInput.setText(dataField.getAttribute(DataFieldConstants.VALUE));
	}
	
	
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		// if the user highlighted this field by clicking the field (not the textBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (!textInput.hasFocus()))
			textInput.requestFocusInWindow();
	}
	
}

