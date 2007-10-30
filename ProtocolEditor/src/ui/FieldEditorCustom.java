package ui;

import tree.DataField;

// all dataField attributes are displayed in panel
// used for displaying imported XML elements that may have other attributes

public class FieldEditorCustom extends FieldEditor {

	public FieldEditorCustom(DataField dataField) {
		
		super(dataField);
		
		// can't edit custom fields
		nameFieldEditor.getTextField().setEnabled(false);
		
		attributeFieldsPanel.remove(inputTypePanel);
		attributeFieldsPanel.remove(descriptionFieldEditor);
		attributeFieldsPanel.remove(urlFieldEditor);
	}

	
}
