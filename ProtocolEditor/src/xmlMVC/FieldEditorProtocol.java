package xmlMVC;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

import xmlMVC.FieldEditor.AttributeEditor;

public class FieldEditorProtocol extends FieldEditor {
	
	private AttributeEditor keywordsFieldEditor;
	
	public FieldEditorProtocol (DataField dataField) {
		
		super(dataField);
		
//		 comma-delimited set of search words
		String keywords = dataField.getAttribute(DataField.KEYWORDS);

		// can't change the protocol field to a different type
		inputTypeSelector.setEnabled(false);
		
		keywordsFieldEditor = new AttributeEditor
			("Keywords: ", keywords);
		keywordsFieldEditor.setToolTipText("Add keywords, separated by commas");
		attributeFieldsPanel.add(keywordsFieldEditor);
		
	}
	
//	 subclasses override these if they have attributes other than name, desc, inputType.
	//	 called when focus lost
	public void updateModelsOtherAttributes() {	
		dataField.setAttribute(DataField.KEYWORDS, keywordsFieldEditor.getTextFieldText(), false);
	}
}
