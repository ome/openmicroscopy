package ui;

import java.util.ArrayList;

import org.w3c.dom.Element;

import tree.DataField;


public class FieldEditorText extends FieldEditor {
	
	private AttributeEditor defaultFieldEditor;
	
	public FieldEditorText(DataField dataField) {
		
		super(dataField);
		
		String defaultValue = dataField.getAttribute(DataField.DEFAULT);
		
		defaultFieldEditor = new AttributeEditor
			("Default Value: ", defaultValue,  textChangedListener, focusChangedListener);
		attributeFieldsPanel.add(defaultFieldEditor);
	}
	
	//	 subclasses override these if they have attributes other than name, desc, inputType.
	//	 called when focus lost
	public void updateModelsOtherAttributes() {	
		dataField.setAttribute(DataField.DEFAULT, defaultFieldEditor.getTextFieldText(), false);
	}

}
