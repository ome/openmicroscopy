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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import tree.DataFieldConstants;
import tree.IDataFieldObservable;
import ui.components.AttributeMemoEditor;

public class FieldEditorDropDown extends FieldEditor {
	
	JComboBox defaultValueComboBox;
	ActionListener defaultValueSelectionListener;
	
	private AttributeMemoEditor optionsFieldEditor;
	
	public FieldEditorDropDown (IDataFieldObservable dataFieldObs) {
		
		super(dataFieldObs);
		
		// comma-delimited list of options
		String dropDownOptions = dataField.getAttribute(DataFieldConstants.DROPDOWN_OPTIONS);
		String defaultValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
		String[] ddOptions = {" "};
		
		optionsFieldEditor = new AttributeMemoEditor
			(dataField, "Drop-down options: (separate with commas)", DataFieldConstants.DROPDOWN_OPTIONS, dropDownOptions);
		optionsFieldEditor.setToolTipText("Add keywords, separated by commas");
		optionsFieldEditor.setTextAreaRows(4);
		attributeFieldsPanel.add(optionsFieldEditor);
		
		
//		 Drop-down selector for default value
		JPanel dropDownDefaultPanel = new JPanel();
		dropDownDefaultPanel.setBorder(new EmptyBorder(3,3,3,3));
		dropDownDefaultPanel.setLayout(new BoxLayout(dropDownDefaultPanel, BoxLayout.X_AXIS));
		
		defaultValueComboBox = new JComboBox();
		
		if (dropDownOptions != null) {
			setDropDownOptions(dropDownOptions);
			// also sets it to correct default Value
		}
		
		dropDownDefaultPanel.add(new JLabel("Default Value: "));
		dropDownDefaultPanel.add(defaultValueComboBox);
		defaultValueSelectionListener = new DefaultValueSelectionListener();
		defaultValueComboBox.addActionListener(defaultValueSelectionListener);
		attributeFieldsPanel.add(dropDownDefaultPanel);
		
		// this is called by the super() constructor, but at that time
		// not all components will have been instantiated. 
		refreshLockedStatus();
	}
	
	
	public void setDropDownOptions(String options) {
		if (options != null) {
			String dropDownOptions = options;
			String [] ddOptions = dropDownOptions.split(",");
			for(int i=0; i<ddOptions.length; i++) {
				ddOptions[i] = ddOptions[i].trim();
			}	
			
			// so that action not fired when changing options
			defaultValueComboBox.removeActionListener(defaultValueSelectionListener);
			
			defaultValueComboBox.removeAllItems();
			for(int i=0; i<ddOptions.length; i++) {
				defaultValueComboBox.addItem(ddOptions[i]);
			}
			
//			 Set it to the current defaultValue, (if it exists in the new ddOptions)
			String defaultValue = dataField.getAttribute(DataFieldConstants.DEFAULT);
			if (defaultValue != null) {
				for (int i=0; i<ddOptions.length; i++)
					if (defaultValue.equals(ddOptions[i]))
						defaultValueComboBox.setSelectedIndex(i);
			}
			
			//need to update default value (in case it wasn't in the new ddOptions)
			defaultValue = defaultValueComboBox.getSelectedItem().toString();
			
			defaultValueComboBox.addActionListener(defaultValueSelectionListener);
		}
		else {	// options == null, remove all
			defaultValueComboBox.removeActionListener(defaultValueSelectionListener);
			defaultValueComboBox.removeAllItems();
			defaultValueComboBox.addActionListener(defaultValueSelectionListener);
		}
	}
	
	// called by dataField when something changes, eg. ddOptions have been set 
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		
		String dropDownOptions = dataField.getAttribute(DataFieldConstants.DROPDOWN_OPTIONS);
		
		setDropDownOptions(dropDownOptions);	// also takes care of default
		optionsFieldEditor.setTextAreaText(dropDownOptions);
		
	}
	
	public class DefaultValueSelectionListener implements ActionListener {
		public void actionPerformed (ActionEvent event) {
			dataField.setAttribute(DataFieldConstants.DEFAULT, defaultValueComboBox.getSelectedItem().toString(), true);
		}
	}
	
	/**
	 * This is called by the superclass FieldEditor.dataFieldUpdated().
	 * Need to refresh the enabled status of additional components in this subclass. 
	 */
	public void enableEditing(boolean enabled) {
		super.enableEditing(enabled);
		
		// need to check != null because this is called by the super() constructor
		// before all subclass components have been instantiated. 
		if (optionsFieldEditor != null) {
			optionsFieldEditor.getTextArea().setEnabled(enabled);
		}
		if (defaultValueComboBox != null) {
			defaultValueComboBox.setEnabled(enabled);
		}
	}

}
