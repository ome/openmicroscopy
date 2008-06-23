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

public class FormFieldText extends FormField {
	
	JTextField textInput;
	
	public FormFieldText(IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		String value = dataField.getAttribute(DataFieldConstants.VALUE);
		
		textInput = new JTextField(value);
		visibleAttributes.add(textInput);
		textInput.addFocusListener(componentFocusListener);		// to highlight field when textBox gets focus
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
		
		if (textInput != null)	// just in case!
			textInput.setEnabled(enabled);
	}
	
	/**
	 * Gets the names of the attributes where this field stores its "value"s.
	 * This is used eg. (if a single value is returned)
	 * as the destination to copy the default value when defaults are loaded.
	 * Also used by EditClearFields to set all values back to null. 
	 * Mostly this is DataFieldConstants.VALUE, but this method should be over-ridden by 
	 * subclasses if they want to store their values under a different attributes (eg "seconds" for TimeField)
	 * 
	 * @return	the name of the attribute that holds the "value" of this field
	 */
	public String[] getValueAttributes() {
		return new String[] {DataFieldConstants.VALUE};
	}
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 *  
	 * @see FormField.isFieldFilled()
	 * @return	True if the field has been filled out by user (Required values are not null)
	 */
	public boolean isFieldFilled() {
		String value = dataField.getAttribute(DataFieldConstants.VALUE);
		return ((value != null) && (value.length() > 0));
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		textInput.setText(dataField.getAttribute(DataFieldConstants.VALUE));
	}

	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) textInput.setForeground(Color.BLACK);
		else textInput.setForeground(Color.WHITE);
		
		textInput.setEditable(enabled);
	}
	
	public void setHighlighted(boolean highlight) {
		//boolean previouslyHighlighted = highlighted;
		
		super.setHighlighted(highlight);
		// if the user highlighted this field by clicking the field (not the textBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && !textInput.hasFocus()) {
		//	textInput.removeFocusListener(componentFocusListener);
			textInput.requestFocusInWindow();
		//	textInput.addFocusListener(componentFocusListener);
		}
	}

}
