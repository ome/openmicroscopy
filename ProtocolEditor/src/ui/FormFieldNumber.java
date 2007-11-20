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

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;

import tree.DataField;

public class FormFieldNumber extends FormField {
	
	JTextField numberTextBox;
	JLabel unitsLabel;
	
	
	public FormFieldNumber (DataField dataField) {
		super(dataField);
		
		String valueString = dataField.getAttribute(DataField.VALUE);
		
		String units = dataField.getAttribute(DataField.UNITS);
		
		numberTextBox = new JTextField(valueString);
		visibleAttributes.add(numberTextBox);
		numberTextBox.addMouseListener(new FormPanelMouseListener());
		numberTextBox.addFocusListener(new NumberCheckerListener());
		numberTextBox.setName(DataField.VALUE);
		numberTextBox.addFocusListener(focusChangedListener);
		numberTextBox.addKeyListener(textChangedListener);
		numberTextBox.setToolTipText("Must enter a number");
		
		unitsLabel = new JLabel(units);
		visibleAttributes.add(unitsLabel);
		
		horizontalBox.add(numberTextBox);
		horizontalBox.add(Box.createHorizontalStrut(10));
		horizontalBox.add(unitsLabel);
		
		//setExperimentalEditing(false);	// default created as uneditable
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
		String number = numberTextBox.getText();
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
	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) numberTextBox.setForeground(Color.BLACK);
		else numberTextBox.setForeground(Color.WHITE);
		
		numberTextBox.setEditable(enabled);
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		numberTextBox.setText(dataField.getAttribute(DataField.VALUE));
		checkForNumber();
		setUnits(dataField.getAttribute(DataField.UNITS));
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
				dataField.setAttribute(DataField.VALUE, number, false);
				
				numberTextBox.setBackground(Color.WHITE);
			}
		}catch (Exception ex) {
			// couldn't convert textBox string to float
			numberTextBox.setBackground(Color.RED);
		}
	}

}
