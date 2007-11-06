package ui;

import tree.DataField;


// this class handles instantiation of FieldEditor and FormField subclasses
// depends on the inputType attribute of dataField.

public class FieldEditorFormFieldFactory {
	
	// singleton
	private static FieldEditorFormFieldFactory uniqueInstance = new FieldEditorFormFieldFactory();
	
	// constructor
	private FieldEditorFormFieldFactory() {
	}
	
	public static FieldEditorFormFieldFactory getInstance() {
		return uniqueInstance;
	}
	
	// FieldEditor Factory
	public FieldEditor getFieldEditor(DataField dataField) {
		
		FieldEditor fieldEditor = null;
		
		String inputType = dataField.getInputType();
		
		if (inputType == null) {
			fieldEditor = new FieldEditorCustom(dataField);
		}
		else if (inputType.equals(DataField.TEXT_ENTRY_STEP)) {
			fieldEditor = new FieldEditorText(dataField);
		}
		else if (inputType.equals(DataField.MEMO_ENTRY_STEP)) {
			fieldEditor = new FieldEditorMemo(dataField);
		}
		else if (inputType.equals(DataField.PROTOCOL_TITLE)) {
			fieldEditor = new FieldEditorProtocol(dataField);
		}
		else if (inputType.equals(DataField.NUMBER_ENTRY_STEP)) {
			fieldEditor = new FieldEditorNumber(dataField);
		}
		else if (inputType.equals(DataField.DATE)) {
			fieldEditor = new FieldEditorDate(dataField);
		}
		else if (inputType.equals(DataField.DROPDOWN_MENU_STEP)) {
			fieldEditor = new FieldEditorDropDown(dataField);
		}
		else if (inputType.equals(DataField.CUSTOM)) {
			fieldEditor = new FieldEditorCustom(dataField);
		} 
		else if (inputType.equals(DataField.TABLE)) {
			fieldEditor = new FieldEditorTable(dataField);
		} 
		else if (inputType.equals(DataField.FIXED_PROTOCOL_STEP)) {
			fieldEditor = new FieldEditorFixed(dataField);
		}
		else {
			fieldEditor = new FieldEditorCustom(dataField);
			dataField.setAttribute(DataField.INPUT_TYPE, DataField.CUSTOM, false);
		}
		
		return fieldEditor;
	}
	
	
	// FormField Factory
	public FormField getFormField(DataField dataField) {
		
		FormField formField = null;
		
		String inputType = dataField.getInputType();
		
		if (inputType == null) {
			formField = new FormFieldCustom(dataField);
		}
		else if (inputType.equals(DataField.TEXT_ENTRY_STEP)) {
			formField = new FormFieldText(dataField);
		}
		else if (inputType.equals(DataField.MEMO_ENTRY_STEP)) {
			formField = new FormFieldMemo(dataField);
		}
		else if (inputType.equals(DataField.PROTOCOL_TITLE)) {
			formField = new FormFieldProtocol(dataField);
		}
		else if (inputType.equals(DataField.NUMBER_ENTRY_STEP)) {
			formField = new FormFieldNumber(dataField);
		}
		else if (inputType.equals(DataField.DATE)) {
			formField = new FormFieldDate(dataField);
		}
		else if (inputType.equals(DataField.DROPDOWN_MENU_STEP)) {
			formField = new FormFieldDropDown(dataField);
		}
		else if (inputType.equals(DataField.CUSTOM)) {
			formField = new FormFieldCustom(dataField);
		}
		else if (inputType.equals(DataField.TABLE)) {
			formField = new FormFieldTable(dataField);
		}
		else if (inputType.equals(DataField.FIXED_PROTOCOL_STEP)) {
			formField = new FormFieldFixed(dataField);
		} else {
			formField = new FormFieldCustom(dataField);
			dataField.setAttribute(DataField.INPUT_TYPE, DataField.CUSTOM, false);
		}
		
		return formField;
	}

}
