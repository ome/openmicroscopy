package ui;

import tree.DataField;

public class FieldEditorTable extends FieldEditor {
	
	AttributeMemoEditor tableColumnsEditor;
	
	public FieldEditorTable (DataField dataField) {
		
		super(dataField);
	
		//	 comma-delimited list of options
		String tableColumns = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
		
		tableColumnsEditor = new AttributeMemoEditor
			("Columns: (separate with commas)", DataField.TABLE_COLUMN_NAMES, tableColumns);
		tableColumnsEditor.setToolTipText("Add columns names, separated by commas");
		tableColumnsEditor.setTextAreaRows(4);
		attributeFieldsPanel.add(tableColumnsEditor);	
	}

}
