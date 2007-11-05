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
	}
	
	public void redo() {
		dataField.setAttribute(attribute, newValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	public String getPresentationName() {
		return "Edit Field";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}
}
