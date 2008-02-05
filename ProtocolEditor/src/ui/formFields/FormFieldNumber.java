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

public class FormFieldNumber extends FormField {
	
	JTextField numberTextBox;
	JLabel unitsLabel;
	
	
	public FormFieldNumber (IDataFieldObservable dataFieldObs) {
		super(dataFieldObs);
		
		String valueString = dataField.getAttribute(DataFieldConstants.VALUE);
		
		String units = dataField.getAttribute(DataFieldConstants.UNITS);
		
		numberTextBox = new JTextField(valueString);
		visibleAttributes.add(numberTextBox);
		numberTextBox.addFocusListener(componentFocusListener);
		numberTextBox.addFocusListener(new NumberCheckerListener());
		numberTextBox.setName(DataFieldConstants.VALUE);
		numberTextBox.setMaximumSize(new Dimension(100, 30));
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

	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		numberTextBox.setText(dataField.getAttribute(DataFieldConstants.VALUE));
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
	
	public void setHighlighted(boolean highlight) {
		super.setHighlighted(highlight);
		// if the user highlighted this field by clicking the field (not the textBox itself) 
		// need to get focus, otherwise focus will remain elsewhere. 
		if (highlight && (!numberTextBox.hasFocus()))
			numberTextBox.requestFocusInWindow();
	}

}
