package tree;

import javax.swing.undo.AbstractUndoableEdit;

public class EditDataFieldAttribute extends AbstractUndoableEdit {
	
	private DataField dataField;
	private String attribute;
	private String oldValue;
	private String newValue;
	
	public EditDataFieldAttribute(DataField dataField, String attribute, String oldValue, String newValue) {
		this.dataField = dataField;
		this.attribute = attribute;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public void undo() {
		dataField.setAttribute(attribute, oldValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
		dataField.formFieldClicked(true);	// highlight this field
	}
	
	public void redo() {
		dataField.setAttribute(attribute, newValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
		dataField.formFieldClicked(true);	// highlight this field
	}
	
	public void undoNoHighlight() {
		dataField.setAttribute(attribute, oldValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	public void redoNoHighlight() {
		dataField.setAttribute(attribute, newValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	// used to highlight a range of fields, when this is the first field in a range
	public void selectField() {
		dataField.formFieldClicked(false);
	}
	
	public String getPresentationName() {
		return "Edit " + attribute;
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}
}
