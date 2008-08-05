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
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;


import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import treeModel.fields.FieldPanel;
import ui.components.AttributeTextEditor;

public class FormFieldNumber extends FieldPanel {
	
	JTextField numberTextBox;
	JLabel unitsLabel;
	
	
	public FormFieldNumber (IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		
		String units = dataField.getAttribute(DataFieldConstants.UNITS);
		
		numberTextBox = new AttributeTextEditor(dataField, 
				DataFieldConstants.VALUE);
		
		numberTextBox.addFocusListener(componentFocusListener);
		numberTextBox.addFocusListener(new NumberCheckerListener());
		numberTextBox.setMaximumSize(new Dimension(100, 30));
		numberTextBox.setToolTipText("Must enter a number");
		
		unitsLabel = new JLabel(units);
		
		
		horizontalBox.add(numberTextBox);
		horizontalBox.add(Box.createHorizontalStrut(10));
		horizontalBox.add(unitsLabel);
		
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
		
		if (numberTextBox != null)	// just in case!
			numberTextBox.setEnabled(enabled);
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
	
	public void setUnits(String units) {
		unitsLabel.setText(units);
	}
	//used to load default values etc. Have to update dataField. 
	public void setValue(String newValue) {
		numberTextBox.setText(newValue);
		//updateDataField();
	}
	
	
	public class NumberCheckerListener implements FocusListener {
		public void focusLost(FocusEvent event){	
			checkForNumber();
		}
		public void focusGained(FocusEvent event){	}
	}
	
	private void checkForNumber() {
		String number = dataField.getAttribute(DataFieldConstants.VALUE);
		float value;
		try {
			if (number.length() > 0) {
				value = Float.parseFloat(number);
				numberTextBox.setBackground(Color.WHITE);
			}
		}catch (Exception ex) {
			numberTextBox.setBackground(Color.RED);
		}
	}

	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		checkForNumber();
		setUnits(dataField.getAttribute(DataFieldConstants.UNITS));
	}
	
	public void multiplyCurrentValue(float factor) {
		String number = numberTextBox.getText();
		float currentValue;
		try {
			if (number.length() > 0) {
				currentValue = Float.parseFloat(number);
				currentValue = currentValue * factor;
				number = Float.toString(currentValue);
				numberTextBox.setText(number);
				dataField.setAttribute(DataFieldConstants.VALUE, number, false);
				
				numberTextBox.setBackground(Color.WHITE);
			}
		}catch (Exception ex) {
			// couldn't convert textBox string to float
			numberTextBox.setBackground(Color.RED);
		}
	}
	
	public void setSelected(boolean highlight) {
		super.setSelected(highlight);
		// if the user highlighted this field by clicking the field (not the textBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (!numberTextBox.hasFocus()))
			numberTextBox.requestFocusInWindow();
	}

}
