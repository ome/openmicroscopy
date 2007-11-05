package tree;

import java.util.ArrayList;

import javax.swing.undo.AbstractUndoableEdit;

public class EditDeleteField extends AbstractUndoableEdit {
	
	ArrayList<DataFieldNode> deletedFields;
	int indexOfFirstHighlightedField;
	DataFieldNode parentNode;
	
	public EditDeleteField (ArrayList<DataFieldNode> deleteTheseFields) {
		
		deletedFields = new ArrayList<DataFieldNode>(deleteTheseFields);
		if (!deletedFields.isEmpty()) {
			DataFieldNode firstNode = deleteTheseFields.get(0);
			indexOfFirstHighlightedField = firstNode.getMyIndexWithinSiblings();
			parentNode = firstNode.getParentNode();
		}
		
	}
	
	public void undo() {
		Tree.insertTheseDataFields(deletedFields, parentNode, indexOfFirstHighlightedField);
	}
	public void redo() {
		Tree.deleteDataFields(deletedFields);
		System.out.println("EditDeleteField redo()");
	}
	
	public String getPresentationName() {
		     return "Delete Fields";
	}
	
	 public boolean canUndo() {
	         return true;
	  }

	  public boolean canRedo() {
	         return true;
	  }
}
