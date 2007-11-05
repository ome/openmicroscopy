package tree;

import java.util.LinkedHashMap;

import javax.swing.undo.AbstractUndoableEdit;

public class EditDataFieldType extends AbstractUndoableEdit {
	
	private DataField dataField;
	private LinkedHashMap<String, String> oldAttributes;
	private LinkedHashMap<String, String> newAttributes;

	
	public EditDataFieldType(DataField dataField, LinkedHashMap<String, String> allAttributes) {
		this.dataField = dataField;
		this.oldAttributes = new LinkedHashMap<String, String>(allAttributes);
	}
	
	public void undo() {
		// first, remember the new attributes to you can redo
		newAttributes = new LinkedHashMap<String, String>(dataField.getAllAttributes());
		
		dataField.setAllAttributes(oldAttributes);
		// set fieldEditor and formField to null, so that new ones are created for the new inputType
		dataField.resetFieldEditorFormField();
		
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();	
	}
	
	public void redo() {
		dataField.setAllAttributes(newAttributes);
		// set fieldEditor and formField to null, so that new ones are created for the new inputType
		dataField.resetFieldEditorFormField();	
		
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	public String getPresentationName() {
		return "Change Field Type";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}
}
