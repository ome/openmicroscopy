package ui;

import tree.DataField;


public class FieldEditorMemo extends FieldEditor {
	
private AttributeMemoEditor defaultFieldEditor;
	
	public FieldEditorMemo(DataField dataField) {
		
		super(dataField);
		
		String defaultValue = dataField.getAttribute(DataField.DEFAULT);
		if (defaultValue == null) defaultValue = "";
		
		defaultFieldEditor = new AttributeMemoEditor
			("Default Text: ", DataField.DEFAULT, defaultValue);
		defaultFieldEditor.setTextAreaRows(3);
		attributeFieldsPanel.add(defaultFieldEditor);
	}

}
