package xmlMVC;

import java.util.ArrayList;


public class FieldEditorNumber extends FieldEditor {
	
	private AttributeEditor defaultFieldEditor;
	private AttributeEditor unitsFieldEditor;
	
	public FieldEditorNumber (DataField dataField) {
		
		super(dataField);
		
		String defaultValueString = dataField.getAttribute(DataField.DEFAULT);
		String units = dataField.getAttribute(DataField.UNITS);
		
		defaultFieldEditor = new AttributeEditor("Default: ", defaultValueString);
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
	

}
