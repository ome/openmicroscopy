package tree;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;

import ui.FormFieldNumber;

public class EditMultiplyValues extends AbstractUndoableEdit {
	
	ArrayList<EditDataFieldAttribute> editedFields;
	
	
	public EditMultiplyValues(ArrayList<DataFieldNode> highlightedFields, float factor) {
		
		editedFields = new ArrayList<EditDataFieldAttribute>();
		
		for (DataFieldNode node: highlightedFields) {
			DataField dataField = node.getDataField();
			String oldValue = dataField.getAttribute(DataField.VALUE);
			
			try {
				FormFieldNumber formFieldNumber = (FormFieldNumber)dataField.getFormField();
				formFieldNumber.multiplyCurrentValue(factor);
				
				String newValue = dataField.getAttribute(DataField.VALUE);
				editedFields.add(new EditDataFieldAttribute(dataField, DataField.VALUE, oldValue, newValue));	// keep a reference to fields that have been edited
			} catch (Exception ex) {
				// cast failed: formField is not a Number field
			}
		}
		
	}
	
	public void undo() {
		for (EditDataFieldAttribute field: editedFields) {
			field.undo();
		}
		// will select the first field, highlighting the whole range (last is already highlighted)
		editedFields.get(0).selectField();
	}
	
	public void redo() {
		for (EditDataFieldAttribute field: editedFields) {
			field.redo();
		}
		editedFields.get(0).selectField();
	}

	public String getPresentationName() {
		return "Multiply Values";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
