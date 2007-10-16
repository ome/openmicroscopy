package xmlMVC;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.util.ArrayList;

// this panel displays the hierarchical tree, made from (FormField) JPanels
// uses recursive buildFormTree() method, indenting children each time

public class FormDisplay extends JPanel {
	
	private XMLView parentXMLView;
	
	int childLeftIndent = 40;
	
	Box verticalFormBox;
	
	FormDisplay(XMLView parent) {
		
		parentXMLView = parent;
		
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		verticalFormBox = Box.createVerticalBox();
		this.add(verticalFormBox, BorderLayout.NORTH);
				
		// get the formField JPanel from the dataField
		if (parentXMLView.getRootNode() != null) {
			JPanel newFormField = parentXMLView.getRootNode().getFormField();
			verticalFormBox.add(newFormField);
			
			// pass the node and the Box that already contains it to buildFormTree()
			// this will get the nodes children and add them to the Box (within a new Box)
			buildFormTree(parentXMLView.getRootNode(), verticalFormBox);
		}
	}
	
	FormDisplay(DataFieldNode rootNode) {
		
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		verticalFormBox = Box.createVerticalBox();
		this.add(verticalFormBox, BorderLayout.NORTH);
				
		// get the formField JPanel from the dataField
		if (rootNode != null) {
			JPanel newFormField = rootNode.getFormField();
			verticalFormBox.add(newFormField);
			
			// pass the node and the Box that already contains it to buildFormTree()
			// this will get the nodes children and add them to the Box (within a new Box)
			buildFormTree(rootNode, verticalFormBox);
		}
	}
	
//	 this will get the node's children and add them to the Box (within a new Box)
	public void buildFormTree(DataFieldNode dfNode, Box verticalBox) {
		
		ArrayList<DataFieldNode> children = dfNode.getChildren();
		
		Box childBox = Box.createVerticalBox();
		childBox.setBorder(BorderFactory.createEmptyBorder(0, childLeftIndent, 0, 0));
		// the node gets a ref to the Box (used for collapsing. Box becomes hidden)
		dfNode.setChildBox(childBox);
		//	set visibility of the childBox wrt collapsed boolean of dataField
		//	 & sets collapse button visible if dataFieldNode has children
		dfNode.getDataField().refreshTitleCollapsed();
	
		// for each child, get their JPanel, add it to the childBox
		for (DataFieldNode child: children){
			JPanel newFormField = child.getFormField();
			childBox.add(newFormField);
			// recursively build the tree below each child
			buildFormTree(child, childBox);
		}
		// add the new childBox to it's parent
		verticalBox.add(childBox);
	}
	
	public void refreshForm() {
		// update reference to the root
		DataFieldNode protocolRootNode = parentXMLView.getRootNode();
		
		if (protocolRootNode == null) return;
		
		verticalFormBox.setVisible(false);	// otherwise if the new form is smaller, old one still visible
		
		this.remove(verticalFormBox);
		
		verticalFormBox = Box.createVerticalBox();
		
		JPanel newFormField = protocolRootNode.getDataField().getFormField();
		verticalFormBox.add(newFormField);
		
		buildFormTree(protocolRootNode, verticalFormBox);
		
		this.add(verticalFormBox, BorderLayout.NORTH);
		this.getParent().getParent().validate();
		this.invalidate();
		this.repaint();
		
	}
	
}

