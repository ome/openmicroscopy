package ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JComboBox;

import tree.DataField;
import ui.FormField.FormPanelMouseListener;

public class FormFieldDropDown extends FormField {
	
	ActionListener valueSelectionListener = new ValueSelectionListener();
	
	String[] ddOptions = {" "};
	
	JComboBox comboBox;
	
	public FormFieldDropDown (DataField dataField) {
	
		super(dataField);
		
		String dropDownOptions = dataField.getAttribute(DataField.DROPDOWN_OPTIONS);
		String value = dataField.getAttribute(DataField.VALUE);
		
		comboBox = new JComboBox();
		
		comboBox.addActionListener(valueSelectionListener);
		// comboBox.addFocusListener(new FocusLostUpdatDataFieldListener());
		
		setDropDownOptions(dropDownOptions);
		
		if (value != null) setValue(value);
		comboBox.addMouseListener(new FormPanelMouseListener());
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
			String value = dataField.getAttribute(DataField.VALUE);
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
		setDropDownOptions(dataField.getAttribute(DataField.DROPDOWN_OPTIONS));
	}

	
	// overridden by subclasses (when focus lost) if they have values that need saving 
	public void updateDataField() {
		int index = comboBox.getSelectedIndex();
		if ((index >= 0) && (index < ddOptions.length)) {
			String currentValue = ddOptions[index];
			dataField.setAttribute(DataField.VALUE, currentValue, false);
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
			dataField.setAttribute(DataField.VALUE, comboBox.getSelectedItem().toString(), true);
		}
	}
	
}
