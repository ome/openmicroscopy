package ui;

import java.text.DateFormat;
import java.util.Date;

import tree.DataField;

public class FieldEditorDate extends FieldEditor {
	
	AttributeEditor defaultFieldEditor;
	
	public FieldEditorDate (DataField dataField) {
		
		super(dataField);
		
		DateFormat fDateFormat = DateFormat.getDateInstance (DateFormat.MEDIUM);
		
		Date now = new Date ();

	    // Format the time string.
	    String defaultDate = fDateFormat.format (now);
	    
	    dataField.setAttribute(DataField.DEFAULT, defaultDate, false);
		
		defaultFieldEditor = new AttributeEditor("Default: ", DataField.DEFAULT, defaultDate);
		// don't allow users to set any other default data!
		defaultFieldEditor.setEnabled(false);
		attributeFieldsPanel.add(defaultFieldEditor);
		
	}
	
	// called when dataField changes attributes
	public void dataFieldUpdated() {
		super.dataFieldUpdated();
		defaultFieldEditor.setTextFieldText(dataField.getAttribute(DataField.DEFAULT));
	}

}
