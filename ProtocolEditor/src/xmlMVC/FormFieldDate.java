package xmlMVC;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JTextField;

public class FormFieldDate extends FormField {
	
	JTextField textInput;
	
	
	public FormFieldDate(DataField dataField) {
		super(dataField);
		
		String date = dataField.getAttribute(DataField.VALUE);
		
		textInput = new JTextField(date);
		textInput.addMouseListener(new formPanelMouseListener());
		textInput.addFocusListener(new focusLostUpdatDataFieldListener());
		horizontalBox.add(textInput);
		
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

