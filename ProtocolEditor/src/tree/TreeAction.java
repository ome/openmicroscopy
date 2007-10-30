package tree;

import java.util.ArrayList;

import tree.Tree.Actions;

public class TreeAction {
	
	Actions command;
	ArrayList<DataFieldNode> highlightedFields;
	int indexOfFirstHighlightedField;
	int lastNodeChildCount;
	DataFieldNode rootNode;
	
	public TreeAction(Actions command, ArrayList<DataFieldNode> highlightedFields) {
		this.command = command;
		this.highlightedFields = new ArrayList<DataFieldNode>(highlightedFields);
		if (!highlightedFields.isEmpty()) {
			indexOfFirstHighlightedField = highlightedFields.get(0).getMyIndexWithinSiblings();
			lastNodeChildCount = highlightedFields.get(highlightedFields.size()-1).getChildren().size();
		}
		System.out.println("TreeAction.Constructor indexOfFirstHighlightedField = " + indexOfFirstHighlightedField);
	}
	
	public TreeAction(Actions command, ArrayList<DataFieldNode> highlightedFields, DataFieldNode rootNode) {
		this.rootNode = rootNode;
		this.command = command;
		this.highlightedFields = new ArrayList<DataFieldNode>(highlightedFields);
		if (!highlightedFields.isEmpty())
			indexOfFirstHighlightedField = highlightedFields.get(0).getMyIndexWithinSiblings();
		System.out.println("TreeAction.Constructor inexOfFirstHL = " + indexOfFirstHighlightedField);
		
	}
	
	public void undo() {
		
		switch (command) {
		
		case ADD_NEW_FIELD: {
			//need ref to new field (will have been added after last highlighted field)
			DataFieldNode parent;
			int index;
			// if no fields were highlighted, newField will be the last child of root
			if (highlightedFields.isEmpty()) {
				parent = rootNode;
				index = rootNode.getChildren().size() -1;
			}
			else {
				parent = highlightedFields.get(0).getParentNode();
				index = indexOfFirstHighlightedField + highlightedFields.size();
			}
			System.out.println("TreeAction.ADD_NEW_FIELD indexToRemove = " + index);
			parent.removeChild(index);
			
			break;
		}
		case DELTE_FIELDS: {
			System.out.println("TreeAction: Undo DELETE_FIELDS firstField index = " + highlightedFields.get(0).getMyIndexWithinSiblings());
			Tree.insertTheseDataFields(highlightedFields, highlightedFields.get(0).getParentNode(), indexOfFirstHighlightedField);
			System.out.println("TreeAction: Undo DELETE_FIELDS firstField index = " + highlightedFields.get(0).getMyIndexWithinSiblings());
			
			break;
		}
		case MOVE_FIELDS_UP: {
			Tree.moveFieldsDown(highlightedFields);
			break;
		}
		case MOVE_FIELDS_DOWN: {
			System.out.println("TreeAction.Undo MoveFieldsDown: indexOfFirstHighlightedField = " 
					+ highlightedFields.get(0).getMyIndexWithinSiblings());
			Tree.moveFieldsUp(highlightedFields);
			break;
		}
		case PROMOTE_FIELDS: {
			Tree.demoteDataFields(highlightedFields);
			// now have to restore any extra children of last node, acquired when it was promoted
			DataFieldNode lastNode = highlightedFields.get(highlightedFields.size()-1);
			int lastNodeNewChildCount = lastNode.getChildren().size();
			
			for (int i=lastNodeNewChildCount-1; i>lastNodeChildCount-1; i--) {
				Tree.promoteDataField(lastNode.getChild(i));
			}
			break;
		}
		case DEMOTE_FIELDS: {
			Tree.promoteDataFields(highlightedFields);
			break;
		}
		case DUPLICATE_FIELDS: {
			Tree.deleteDataFields(highlightedFields);
			break;
		}
		case PASTE_FIELDS: {
			// in this case, highlightedFields are the pasted fields
			Tree.deleteDataFields(highlightedFields);
			break;
		}
		case IMPORT_FIELDS: {
			// in this case, highlightedFields are the imported fields
			Tree.deleteDataFields(highlightedFields);
		}
		}
	}
	
	public Enum<Actions> getCommand() {
		return command;
	}

}
