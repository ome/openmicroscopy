package tree;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;

public class EditMoveFieldsUp extends AbstractUndoableEdit {
	
	ArrayList<DataFieldNode> movedFields;
	
	public EditMoveFieldsUp (ArrayList<DataFieldNode> moveTheseFields) {
		
		movedFields = new ArrayList<DataFieldNode>(moveTheseFields);
		
	}
	
	public void undo() {
		Tree.moveFieldsDown(movedFields);
	}
	public void redo() {
		Tree.moveFieldsUp(movedFields);
	}
	
	public String getPresentationName() {
		return "Move Fields Up";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}
}
