package ui;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JTextField;

import org.w3c.dom.Element;

import tree.DataField;

public class FormFieldText extends FormField {
	
	JTextField textInput;
	
	public FormFieldText(DataField dataField) {
		super(dataField);
		
		String value = dataField.getAttribute(DataField.VALUE);
		
		textInput = new JTextField(value);
		visibleAttributes.add(textInput);
		textInput.addMouseListener(new FormPanelMouseListener());
		textInput.setName(DataField.VALUE);
		textInput.addFocusListener(focusChangedListener);
		textInput.addKeyListener(textChangedListener);
		horizontalBox.add(textInput);
		
		//setExperimentalEditing(false);	// default created as uneditable
	}
	
	// overridden by subclasses if they have other attributes to retrieve from dataField
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		textInput.setText(dataField.getAttribute(DataField.VALUE));
	}

	
//	 overridden by subclasses that have input components
	public void setExperimentalEditing(boolean enabled) {
		
		if (enabled) textInput.setForeground(Color.BLACK);
		else textInput.setForeground(Color.WHITE);
		
		textInput.setEditable(enabled);
	}

}
