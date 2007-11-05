package tree;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;

public class EditImportFields extends AbstractUndoableEdit {
	
	ArrayList<DataFieldNode> addedFields;
	DataFieldNode parentNode;
	int indexOfFirstHighlightedField;
	
	public EditImportFields (ArrayList<DataFieldNode> addTheseFields) {
		
		addedFields = new ArrayList<DataFieldNode>(addTheseFields);
		
		DataFieldNode firstNode = addedFields.get(0);
		parentNode = firstNode.getParentNode();
		indexOfFirstHighlightedField = firstNode.getMyIndexWithinSiblings();
		
	}
	
	public void undo() {
		Tree.deleteDataFields(addedFields);
	}
	public void redo() {
		Tree.insertTheseDataFields(addedFields, parentNode, indexOfFirstHighlightedField);
	}
	
	public String getPresentationName() {
		return "Import Fields";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}

