package tree;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;

public class EditMoveFieldsDown extends AbstractUndoableEdit {
	
	ArrayList<DataFieldNode> movedFields;
	
	public EditMoveFieldsDown (ArrayList<DataFieldNode> moveTheseFields) {
		
		movedFields = new ArrayList<DataFieldNode>(moveTheseFields);
		
	}
	
	public void undo() {
		Tree.moveFieldsUp(movedFields);
	}
	public void redo() {
		Tree.moveFieldsDown(movedFields);
	}
	
	public String getPresentationName() {
		return "Move Fields Down";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
