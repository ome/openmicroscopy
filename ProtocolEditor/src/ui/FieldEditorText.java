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
			("Default Value: ", DataField.DEFAULT, defaultValue);
		attributeFieldsPanel.add(defaultFieldEditor);
	}

}
