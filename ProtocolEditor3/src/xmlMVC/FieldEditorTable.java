package xmlMVC;

import xmlMVC.FieldEditor.AttributeMemoEditor;

public class FieldEditorTable extends FieldEditor {
	
	AttributeMemoEditor tableColumnsEditor;
	
	public FieldEditorTable (DataField dataField) {
		
		super(dataField);
	
		//	 comma-delimited list of options
		String tableColumns = dataField.getAttribute(DataField.TABLE_COLUMN_NAMES);
		
		tableColumnsEditor = new AttributeMemoEditor
			("Columns: (separate with commas)", tableColumns);
		tableColumnsEditor.setToolTipText("Add columns names, separated by commas");
		tableColumnsEditor.setTextAreaRows(4);
		attributeFieldsPanel.add(tableColumnsEditor);
		
	}
	
	//	 subclasses override these if they have attributes other than name, desc, inputType.
	//	 called when focus lost
	public void updateModelsOtherAttributes() {	
		String tableCols = tableColumnsEditor.getTextAreaText();

		dataField.setAttribute(DataField.TABLE_COLUMN_NAMES, tableCols, true);
	}

}
