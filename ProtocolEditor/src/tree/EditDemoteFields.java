package tree;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;

public class EditDemoteFields extends AbstractUndoableEdit {
	
	ArrayList<DataFieldNode> movedFields;
	int lastNodeChildCount;
	
	public EditDemoteFields (ArrayList<DataFieldNode> moveTheseFields) {
		
		movedFields = new ArrayList<DataFieldNode>(moveTheseFields);
		lastNodeChildCount = movedFields.get(movedFields.size()-1).getChildren().size();
	}
	
	public void undo() {
		Tree.promoteDataFields(movedFields);
	}
	public void redo() {
		Tree.demoteDataFields(movedFields);
	}
	
	public String getPresentationName() {
		return "Demote Fields";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
