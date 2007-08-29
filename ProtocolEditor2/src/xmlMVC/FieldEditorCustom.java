package xmlMVC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;

import xmlMVC.FieldEditor.AttributeEditor;

public class FieldEditorCustom extends FieldEditor {

	Box customAttributesBox;
	
	ArrayList<AttributeEditor> customAttributesFields = new ArrayList<AttributeEditor>();
	
	
	public FieldEditorCustom(DataField dataField) {
		
		super(dataField);
		
		dataField.setAttribute(DataField.INPUT_TYPE, DataField.CUSTOM, false);
		
		customAttributesBox = Box.createVerticalBox();
		
		displayAllAttributes();
		
		attributeFieldsPanel.add(customAttributesBox);
	}

	public void displayAllAttributes() {
		LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
		
		Iterator keyIterator = allAttributes.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			String value = allAttributes.get(name);
			
			AttributeEditor attributeEditor = new AttributeEditor(name, value);
			// keep a list of fields
			customAttributesFields.add(attributeEditor);
			customAttributesBox.add(attributeEditor);
		}
	}
	
//	called when focus lost. Overridden by subclasses if they have other attribute fields
	public void updateModelsOtherAttributes() {	
		
		for (AttributeEditor field: customAttributesFields) {
			dataField.setAttribute(field.getAttributeName(), field.getTextFieldText(), false);
		}
		
	}

	//called by dataField when something changes. 
	public void dataFieldUpdated() {
		nameFieldEditor.setTextFieldText(dataField.getAttribute(DataField.ELEMENT_NAME));
		descriptionFieldEditor.setTextAreaText(dataField.getAttribute(DataField.DESCRIPTION));
		urlFieldEditor.setTextFieldText(dataField.getAttribute(DataField.URL));
		
		customAttributesBox.removeAll();
		customAttributesFields.clear();
		
		displayAllAttributes();
		customAttributesBox.validate();
	}
	
	
	
}
