package ui;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

import tree.DataField;

public class FieldEditorProtocol extends FieldEditor {
	
	private AttributeEditor keywordsFieldEditor;
	
	public FieldEditorProtocol (DataField dataField) {
		
		super(dataField);
		
//		 comma-delimited set of search words
		String keywords = dataField.getAttribute(DataField.KEYWORDS);

		// can't change the protocol field to a different type
		inputTypeSelector.setEnabled(false);
		
		keywordsFieldEditor = new AttributeEditor
			("Keywords: ", DataField.KEYWORDS, keywords);
		keywordsFieldEditor.setToolTipText("Add keywords, separated by commas");
		attributeFieldsPanel.add(keywordsFieldEditor);
		
	}

}
