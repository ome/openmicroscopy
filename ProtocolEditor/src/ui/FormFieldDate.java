package ui;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JTextField;

import tree.DataField;

public class FormFieldDate extends FormField {
	
	JTextField textInput;
	
	
	public FormFieldDate(DataField dataField) {
		super(dataField);
		
		String date = dataField.getAttribute(DataField.VALUE);
		
		textInput = new JTextField(date);
		visibleAttributes.add(textInput);
		textInput.addMouseListener(new FormPanelMouseListener());
		textInput.addFocusListener(new FocusLostUpdatDataFieldListener());
		horizontalBox.add(textInput);
		
		setExperimentalEditing(false);	// default created as uneditable
	}
	
	// overridden by subclasses (when focus lost) if they have values that need saving 
	public void updateDataField() {
		dataField.setAttribute(DataField.VALUE, textInput.getText(), false);
	}
	
//	 overridden by subclasses if they have a value and text field
	public void setValue(String newValue) {
		textInput.setText(newValue);
		updateDataField();
	}
	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) textInput.setForeground(Color.BLACK);
		else textInput.setForeground(textInput.getBackground());
		
		textInput.setEditable(enabled);
	}
	
}

