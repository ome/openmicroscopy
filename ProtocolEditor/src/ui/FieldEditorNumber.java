package ui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JTextField;

import tree.DataField;
import ui.FormField.FormPanelMouseListener;

public class FieldEditorNumber extends FieldEditor {
	
	private AttributeEditor defaultFieldEditor;
	private AttributeEditor unitsFieldEditor;
	
	public FieldEditorNumber (DataField dataField) {
		
		super(dataField);
		
		String defaultValueString = dataField.getAttribute(DataField.DEFAULT);
		String units = dataField.getAttribute(DataField.UNITS);
		
		defaultFieldEditor = new AttributeEditor("Default: ", DataField.DEFAULT, defaultValueString);
		defaultFieldEditor.getTextField().addFocusListener(new NumberCheckerListener());
		attributeFieldsPanel.add(defaultFieldEditor);
		
		unitsFieldEditor = new AttributeEditor("Units: ", DataField.UNITS, units);
		attributeFieldsPanel.add(unitsFieldEditor);
	}

	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		defaultFieldEditor.setTextFieldText(dataField.getAttribute(DataField.DEFAULT));
		checkForNumber();
		unitsFieldEditor.setTextFieldText(dataField.getAttribute(DataField.UNITS));
	}
	
	public class NumberCheckerListener implements FocusListener {
		public void focusLost(FocusEvent event){	
			checkForNumber();
		}
		public void focusGained(FocusEvent event){	}
	}
	
	private void checkForNumber() {
		String number = defaultFieldEditor.getTextFieldText();
		try {
			if (number.length() > 0) {
				float value = Float.parseFloat(number);
				defaultFieldEditor.getTextField().setBackground(Color.WHITE);
			}
		}catch (Exception ex) {
			defaultFieldEditor.getTextField().setBackground(Color.RED);
		}
	}

}
