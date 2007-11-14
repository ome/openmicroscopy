package tree;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.undo.AbstractUndoableEdit;

public class EditCopyDefaultValues extends AbstractUndoableEdit {
	
	Iterator<DataFieldNode> iterator;
	ArrayList<EditDataFieldAttribute> editedFields;
	
	public EditCopyDefaultValues (DataFieldNode rootNode) {
		
		iterator = rootNode.iterator();
		editedFields = new ArrayList<EditDataFieldAttribute>();

		
		while (iterator.hasNext()) {
			DataField field = (DataField)iterator.next().getDataField();
			String oldValue = field.getAttribute(DataField.VALUE);	// may be null
			String newValue = field.getAttribute(DataField.DEFAULT);
			
			if (newValue != null) {		// make a list of all fields that have a default value
				editedFields.add(new EditDataFieldAttribute(field, DataField.VALUE, oldValue, newValue));	// keep a reference to fields that have been edited
			}
			
		}
		redo();		// this sets value to newValue for all fields in the list
	}
	
	
	public void undo() {
		for (EditDataFieldAttribute field: editedFields) {
			field.undoNoHighlight();
		}
	}
	
	public void redo() {
		for (EditDataFieldAttribute field: editedFields) {
			field.redoNoHighlight();
		}
	}
	
	public String getPresentationName() {
		return "Load Default Values";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
