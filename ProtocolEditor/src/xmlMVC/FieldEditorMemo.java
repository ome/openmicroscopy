package xmlMVC;

import java.util.ArrayList;

import org.w3c.dom.Element;


public class FieldEditorMemo extends FieldEditor {
	
private AttributeMemoEditor defaultFieldEditor;
	
	public FieldEditorMemo(DataField dataField) {
		
		super(dataField);
		
		String defaultValue = dataField.getAttribute(DataField.DEFAULT);
		if (defaultValue == null) defaultValue = "";
		
		defaultFieldEditor = new AttributeMemoEditor
			("Default Text: ", defaultValue);
		defaultFieldEditor.setTextAreaRows(3);
		attributeFieldsPanel.add(defaultFieldEditor);
	}
	
	//	 called when focus lost
	public void updateModelsOtherAttributes() {	
		dataField.setAttribute(DataField.DEFAULT, defaultFieldEditor.getTextAreaText(), false);
	}

}
