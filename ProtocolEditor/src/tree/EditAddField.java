package tree;

import javax.swing.undo.AbstractUndoableEdit;

public class EditAddField extends AbstractUndoableEdit {
	
	DataFieldNode newNode;
	DataFieldNode parentNode;
	int index;
	
	public EditAddField(DataFieldNode newNode) {
		
		this.newNode = newNode;
		parentNode = newNode.getParentNode();
		
	}
	
	public void undo() {
		//need ref to new field (will have been added after last highlighted field)
		index = newNode.getMyIndexWithinSiblings();
		System.out.println("TreeAction.ADD_NEW_FIELD indexToRemove = " + index);
		parentNode.removeChild(index);
	}
	
	public void redo() {
		Tree.addDataField(newNode, parentNode, index);
	}
	

	public String getPresentationName() {
		return "Add Field";
	}

	  public boolean canUndo() {
	         return true;
	  }

	  public boolean canRedo() {
	         return true;
	  }
}
