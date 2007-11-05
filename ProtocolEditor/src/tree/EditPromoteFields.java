package tree;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;

public class EditPromoteFields extends AbstractUndoableEdit {
	
	ArrayList<DataFieldNode> movedFields;
	int lastNodeChildCount;
	
	public EditPromoteFields (ArrayList<DataFieldNode> moveTheseFields) {
		
		movedFields = new ArrayList<DataFieldNode>(moveTheseFields);
		lastNodeChildCount = movedFields.get(movedFields.size()-1).getChildren().size();
	}
	
	public void undo() {
		Tree.demoteDataFields(movedFields);
		// now have to restore any extra children of last node, acquired when it was promoted
		DataFieldNode lastNode = movedFields.get(movedFields.size()-1);
		int lastNodeNewChildCount = lastNode.getChildren().size();
		
		for (int i=lastNodeNewChildCount-1; i>lastNodeChildCount-1; i--) {
			Tree.promoteDataField(lastNode.getChild(i));
		}
	}
	public void redo() {
		Tree.promoteDataFields(movedFields);
	}
	
	public String getPresentationName() {
		return "Promote Fields";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}


}
