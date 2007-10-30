package tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.Box;
import javax.swing.JPanel;

import org.w3c.dom.Element;


public class DataFieldNode implements Visitable {
	
	DataField dataField;
	
	boolean highlighted = false; 	// is this node/dataField selected (displayed blue)
	
	Tree tree;		// class that manages tree structure (takes click commands)
	ArrayList<DataFieldNode> children;
	DataFieldNode parent;
	Box childBox;	// swing component that holds all subtree. Hide when collapse
	
	// constructor 
	public DataFieldNode(LinkedHashMap<String, String> allAttributesMap, DataFieldNode parent, Tree tree) {
		this.parent = parent;
		this.tree = tree;
		children = new ArrayList<DataFieldNode>();
		dataField = new DataField(allAttributesMap, this);
	}
	
	// this constructor used for root node (no parent)
	public DataFieldNode(LinkedHashMap<String, String> allAttributesMap,  Tree tree) {
		this.parent = null;
		this.tree = tree;
		children = new ArrayList<DataFieldNode>();
		dataField = new DataField(allAttributesMap, this);
	}
	// this constructor used for blank root node (no parent)
	public DataFieldNode( Tree tree) {
		children = new ArrayList<DataFieldNode>();
		dataField = new DataField(this);
		this.parent = null;
		this.tree = tree;
	}
	
	// constructor to make a copy of existing Node
	// retuns duplicate node with no parent
	public DataFieldNode(DataFieldNode copyThisNode,  Tree tree) {
		
		children = new ArrayList<DataFieldNode>();
		this.tree = tree;
	
		dataField = new DataField(copyThisNode.getDataField(), this);
	}
	
	// constructor to make a copy of existing Node
	// retuns duplicate node with no parent
	public DataFieldNode(DataFieldNode copyThisNode) {
		
		children = new ArrayList<DataFieldNode>();
		// get ref to tree from parent (when setParent is called)
	
		dataField = new DataField(copyThisNode.getDataField(), this);
	}
	
	public int getMyIndexWithinSiblings() {
		if (parent == null)
			throw (new NullPointerException("Can't getMyIndexWithinSiblings because parent == null"));
			
		return parent.indexOfChild(this);
	}
	
	public void setParent(DataFieldNode parent) {
		this.parent = parent;
		if (tree == null) tree = parent.getTree();
	}

	@SuppressWarnings("unchecked")
	public Iterator<DataFieldNode> createIterator() {
		return new DataFieldIterator(children.iterator());
	}
	
	public void addChild(DataFieldNode dataFieldNode) {
		children.add(dataFieldNode);
	}
	public void addChild(int index, DataFieldNode dataFieldNode) {
		children.add(index, dataFieldNode);
	}
	public void removeChild(DataFieldNode child) {
		children.remove(child);
	}
	public void removeChild(int index){
		children.remove(index);
	}
	public int indexOfChild(DataFieldNode child) {
		return children.indexOf(child);
	}
	public DataFieldNode getChild(int index) {
		return children.get(index);
	}

	public DataField getDataField() {
		return dataField;
	}
	public JPanel getFieldEditor() {
		return dataField.getFieldEditor();
	}
	public JPanel getFormField() {
		return dataField.getFormField();
	}
	public DataFieldNode getParentNode() {
		return parent;
	}
	
	public ArrayList<DataFieldNode> getChildren() {
		return children;
	}
	public void setChildBox(Box childBox) {
		this.childBox = childBox;
	}
	public Box getChildBox() {
		return childBox;
	}
	
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
		dataField.setHighlighted(highlighted);
	}
	public boolean getHighlighted() {
		return highlighted;
	}
	public void nodeClicked(boolean clearOthers) {
		getTree().nodeSelected(this, clearOthers);
	}
	public void dataFieldUpdated() {
		getTree().dataFieldUpdated();
	}
	public void hideChildren(boolean hidden) {
		if (childBox != null)	// sometimes visibility of children is set before UI is fully built
			childBox.setVisible(!hidden);
	}

	public void acceptVistor(DataFieldVisitor visitor) {
		dataField.acceptVistor(visitor);
	}
	public void collapseAllChildren(boolean collapse) {
		getTree().collapseAllChildren(collapse);
	}
	public Tree getTree() {
		if ((tree == null) && (parent != null)) tree = parent.getTree();
		return tree;
	}
}
