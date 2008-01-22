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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.FormField.FormPanelMouseListener;

public class FormFieldDropDown extends FormField {
	
	ActionListener valueSelectionListener = new ValueSelectionListener();
	
	String[] ddOptions = {" "};
	
	JComboBox comboBox;
	
	public FormFieldDropDown (IDataFieldObservable dataFieldObs) {
	
		super(dataFieldObs);
		
		String dropDownOptions = dataField.getAttribute(DataFieldConstants.DROPDOWN_OPTIONS);
		String value = dataField.getAttribute(DataFieldConstants.VALUE);
		
		comboBox = new JComboBox();
		
		comboBox.addActionListener(valueSelectionListener);
		// comboBox.addFocusListener(new FocusLostUpdatDataFieldListener());
		
		setDropDownOptions(dropDownOptions);
		
		if (value != null) setValue(value);
		comboBox.addFocusListener(componentFocusListener);
		horizontalBox.add(comboBox);
	
		//setExperimentalEditing(false);	// default created as uneditable
	}
	
	public void setDropDownOptions(String options) {
		if (options != null) {
			String dropDownOptions = options;
			ddOptions = dropDownOptions.split(",");
			for(int i=0; i<ddOptions.length; i++) {
				ddOptions[i] = ddOptions[i].trim();
			}
			
			comboBox.removeActionListener(valueSelectionListener);
		
			comboBox.removeAllItems();
			for(int i=0; i<ddOptions.length; i++) {
				comboBox.addItem(ddOptions[i]);
			}
			
			// Set it to the current value, (if it exists in the new ddOptions)
			String value = dataField.getAttribute(DataFieldConstants.VALUE);
			if (value != null) {
				for (int i=0; i<ddOptions.length; i++)
					if (value.equals(ddOptions[i]))
						comboBox.setSelectedIndex(i);
			}
			
			comboBox.addActionListener(valueSelectionListener);
			
			//need to update value (in case it wasn't in the new ddOptions)
			updateDataField();
		} else {
			// options == null, remove all
			comboBox.removeActionListener(valueSelectionListener);
			comboBox.removeAllItems();
			comboBox.addActionListener(valueSelectionListener);
		}
	}
	
//	 overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		setDropDownOptions(dataField.getAttribute(DataFieldConstants.DROPDOWN_OPTIONS));
	}

	
	// overridden by subclasses (when focus lost) if they have values that need saving 
	public void updateDataField() {
		int index = comboBox.getSelectedIndex();
		if ((index >= 0) && (index < ddOptions.length)) {
			String currentValue = ddOptions[index];
			dataField.setAttribute(DataFieldConstants.VALUE, currentValue, false);
		}
	}

//	 overridden by subclasses if they have a value and text field
	public void setValue(String newValue) {
		if (newValue == null) return;
		
		comboBox.removeActionListener(valueSelectionListener);
		
		for (int i=0; i<ddOptions.length; i++)
			if (newValue.equals(ddOptions[i]))
				comboBox.setSelectedIndex(i);
		
		comboBox.addActionListener(valueSelectionListener);
		
		updateDataField();
	}

///	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		comboBox.setEnabled(enabled);
		
		if (enabled) comboBox.setForeground(Color.BLACK);
		else comboBox.setForeground(comboBox.getBackground());
		
	}
	
	public class ValueSelectionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			dataField.setAttribute(DataFieldConstants.VALUE, comboBox.getSelectedItem().toString(), true);
		}
	}
	
}
