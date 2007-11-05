package tree;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;

public class EditPasteFields extends AbstractUndoableEdit {
	
	ArrayList<DataFieldNode> addedFields;
	DataFieldNode parentNode;
	int indexOfFirstHighlightedField;
	
	public EditPasteFields (ArrayList<DataFieldNode> addTheseFields) {
		
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
		return "Paste Fields";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}

