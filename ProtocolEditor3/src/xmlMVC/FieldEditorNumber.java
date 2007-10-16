package xmlMVC;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

import javax.swing.JTextField;

import xmlMVC.FormField.FormPanelMouseListener;

public class FieldEditorNumber extends FieldEditor {
	
	private AttributeEditor defaultFieldEditor;
	private AttributeEditor unitsFieldEditor;
	
	public FieldEditorNumber (DataField dataField) {
		
		super(dataField);
		
		String defaultValueString = dataField.getAttribute(DataField.DEFAULT);
		String units = dataField.getAttribute(DataField.UNITS);
		
		defaultFieldEditor = new AttributeEditor("Default: ", defaultValueString);
		defaultFieldEditor.getTextField().addFocusListener(new NumberCheckerListener());
		attributeFieldsPanel.add(defaultFieldEditor);
		
		unitsFieldEditor = new AttributeEditor("Units: ", units);
		attributeFieldsPanel.add(unitsFieldEditor);
	}
	
	//	 subclasses override these if they have attributes other than name, desc, inputType.
	//	 called when focus lost
	public void updateModelsOtherAttributes() {	
		dataField.setAttribute(DataField.DEFAULT, defaultFieldEditor.getTextFieldText(), false);
		dataField.setAttribute(DataField.UNITS, unitsFieldEditor.getTextFieldText(), false);
	}
	
	public class NumberCheckerListener implements FocusListener {
		public void focusLost(FocusEvent event){	
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
		public void focusGained(FocusEvent event){	}
	}

}
