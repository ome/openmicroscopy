package xmlMVC;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Tree {
	
	private DataFieldNode rootNode;		// the root of the dataField tree. 
	
	private ArrayList<DataFieldNode> highlightedFields;
	
	public final static String ELEMENT = "element";
	
	private SelectionObserver selectionObserver;
	private XMLUpdateObserver xmlUpdateObserver;
	
	
	public Tree(Document document, SelectionObserver selectionObserver, XMLUpdateObserver xmlObserver) {
		
		this.selectionObserver = selectionObserver;
		this.xmlUpdateObserver = xmlObserver;
		highlightedFields = new ArrayList<DataFieldNode>();
		
		Element rootElement = document.getDocumentElement();
			
		LinkedHashMap<String, String> allAttributes = new LinkedHashMap<String, String>();

		parseElementToMap(rootElement, allAttributes);
			 
		rootNode = new DataFieldNode(allAttributes, this);
			
		buildTreeFromDOM(rootNode, rootElement);
	}
	
	public Tree(Document document) {
		highlightedFields = new ArrayList<DataFieldNode>();
		
		Element rootElement = document.getDocumentElement();
			
		LinkedHashMap<String, String> allAttributes = new LinkedHashMap<String, String>();

		parseElementToMap(rootElement, allAttributes);
			 
		rootNode = new DataFieldNode(allAttributes, this);
			
		buildTreeFromDOM(rootNode, rootElement);
	}
	
	public Tree(SelectionObserver selectionObserver, XMLUpdateObserver xmlObserver) {
		rootNode = new DataFieldNode(this);
		this.selectionObserver = selectionObserver;
		this.xmlUpdateObserver = xmlObserver;
		highlightedFields = new ArrayList<DataFieldNode>();
	}
	
//	 start a blank protocol
	public void openBlankProtocolFile() {
		
		// the root of the dataField tree
		rootNode = new DataFieldNode(this);
		DataField rootField = rootNode.getDataField();
		
		rootField.changeDataFieldInputType(DataField.PROTOCOL_TITLE);
		rootField.setName("Title - click to edit", true);
		
		DataFieldNode newNode = new DataFieldNode(rootNode, this);// make a new default-type field
		rootNode.addChild(newNode);
	}
	
//	 example with different field types
	public void openDemoProtocolFile() {
		
//		 the root of the dataField tree
		rootNode = new DataFieldNode(this);
		DataField protocolField = rootNode.getDataField();
		
		protocolField.changeDataFieldInputType(DataField.PROTOCOL_TITLE);
		protocolField.setAttribute(DataField.ELEMENT_NAME, "Protocol Title - click to edit");
		
		DataFieldNode newNode;
		
		for (int i=0; i < DataField.INPUT_TYPES.length ;i++) {
			newNode = new DataFieldNode(rootNode, this);
			newNode.getDataField().changeDataFieldInputType(DataField.INPUT_TYPES[i]);
			newNode.getDataField().setName(DataField.INPUT_TYPES[i] + " Example", true);
			rootNode.addChild(newNode);
		}
	}
	
	public void buildTreeFromDOM(DataFieldNode dfNode, Element inputElement) {
		
		NodeList children = inputElement.getChildNodes();
		
		for (int i=0; i < children.getLength(); i++) {
			
			// skip any empty (text) nodes
			Node node = children.item(i);
	
			 
			 if (node != null && (node.getNodeType() == Node.ELEMENT_NODE)) {
				 Element element = (Element)node; 
				 LinkedHashMap<String, String> allAttributes = new LinkedHashMap<String, String>();

				 parseElementToMap(element, allAttributes);
				 DataFieldNode newNode = new DataFieldNode(allAttributes, dfNode, this);
				 dfNode.addChild(newNode);
				 buildTreeFromDOM(newNode, element);
			 }
			 
			 if (node != null && (node.getNodeType() == Node.TEXT_NODE)) {
				 String textValue = node.getTextContent().trim();
				 if (textValue.length() > 0){
					 dfNode.getDataField().setAttribute(DataField.VALUE, node.getTextContent());
					 // dfNode.getDataField().changeDataFieldInputType(DataField.TEXT_ENTRY_STEP);
					 // System.out.println("Tree.buildTreeFromDom: Text Node value is " + node.getTextContent());
				 }
			 }
		}
		
	}
	
	public void parseElementToMap(Element element, LinkedHashMap<String, String> allAttributes) {
		 String attributeValue;
		 String attribute;
		 
		 NamedNodeMap attributes = element.getAttributes();
		 for (int i=0; i<attributes.getLength(); i++) {
			 attribute = attributes.item(i).getNodeName();
			 attributeValue = attributes.item(i).getNodeValue();
	
			 if (attributeValue != null) {
				allAttributes.put(attribute, attributeValue);
			 }
		 }
		 
		 String elementName = element.getNodeName();
		 // if the xml file's elements don't have "elementName" attribute, use the <tagName>
		 if (allAttributes.get(DataField.ELEMENT_NAME) == null) {
			 allAttributes.put(DataField.ELEMENT_NAME, elementName);
		 }
	}


//	 duplicate a dataField and add it at specified index
	public void duplicateDataFields() {
		
		if (highlightedFields.size() == 0) return;
		
		DataFieldNode lastDataFieldNode = highlightedFields.get(highlightedFields.size()-1);

		DataFieldNode parentNode = lastDataFieldNode.getParentNode();
		int indexToAddNewNode = lastDataFieldNode.getMyIndexWithinSiblings() + 1;
		
		// need to duplicate everything. Easiest to do it via hashmap:
			
			for (DataFieldNode highlightedField: highlightedFields){
				
				DataFieldNode newNode = duplicateDataFieldNode(highlightedField);
				newNode.setParent(parentNode);
				
				parentNode.addChild(indexToAddNewNode, newNode);
				
				duplicateDataFieldTree(highlightedField, newNode);
				
				indexToAddNewNode++;
			}
	}
	
	
	// returns a duplicate node with no parent
	public DataFieldNode duplicateDataFieldNode(DataFieldNode highlightedField) {
		LinkedHashMap<String, String> allAttributes = highlightedField.getDataField().getAllAttributes();
//		 get all attributes of the datafield
		LinkedHashMap<String, String> newAttributes = new LinkedHashMap<String, String>(allAttributes);
		
		DataFieldNode newNode = new DataFieldNode(newAttributes, this);
		
		return newNode;
	}

	public void duplicateDataFieldTree(DataFieldNode oldNode, DataFieldNode newNode) {
		
		ArrayList<DataFieldNode> children = oldNode.getChildren();
		if (children.size() == 0) return;
			
			for (DataFieldNode child: children){
			 
				DataFieldNode newChild = duplicateDataFieldNode(child);
				newChild.setParent(newNode);
				
				newNode.addChild(newChild);
				
				duplicateDataFieldTree(child, newChild);
			}
	}

	public void insertProtocolFromNewFile(Document document) {
	
		if (highlightedFields.size() == 0) return;
	
		DataFieldNode lastSelectedNode = highlightedFields.get(highlightedFields.size()-1);

		DataFieldNode parentNode = lastSelectedNode.getParentNode();
		int indexToInsert = lastSelectedNode.getMyIndexWithinSiblings() + 1;
	
		Element protocol = document.getDocumentElement();
	
		LinkedHashMap<String, String> allAttributes = new LinkedHashMap<String, String>();
		parseElementToMap(protocol, allAttributes);
	
		DataFieldNode newNode = new DataFieldNode(allAttributes, parentNode, this);
		parentNode.addChild(indexToInsert, newNode);
	
		buildTreeFromDOM(newNode, protocol);
	}
	
	
	public void demoteDataFields() {
		
		if (highlightedFields.isEmpty()) return;
		
		// fields need to become children of their preceeding sibling (if they have one)
		DataFieldNode firstNode = highlightedFields.get(0);
		int indexOfFirstSibling = firstNode.getMyIndexWithinSiblings();
		
		// if no preceeding sibling, can't demote
		if (indexOfFirstSibling == 0) return;
		
		DataFieldNode parentNode = firstNode.getParentNode();
		DataFieldNode preceedingSiblingNode = parentNode.getChild(indexOfFirstSibling-1);
		
		// move nodes
		for (DataFieldNode highlightedField: highlightedFields) {
			preceedingSiblingNode.addChild(highlightedField);
			highlightedField.setParent(preceedingSiblingNode);
		}
//		 delete them from the end (reverse order)
		for (int i=highlightedFields.size()-1; i>=0; i--) {
			parentNode.removeChild(highlightedFields.get(i));
		}
	}
	
	public void promoteDataFields() {
		
		if (highlightedFields.size() < 1) return;
		DataFieldNode node = highlightedFields.get(0);
		DataFieldNode parentNode = node.getParentNode();
		DataFieldNode grandParentNode = parentNode.getParentNode();
		// if parent is root (grandparent null) then can't promote
		if (grandParentNode == null) return;
		
		// any fields that are children of the last to be promoted, 
		// must first become children of that node. 
		DataFieldNode lastNode = highlightedFields.get(highlightedFields.size()-1);
		DataFieldNode lastNodeParent = lastNode.getParentNode();
		
		int indexOfLast = lastNodeParent.indexOfChild(lastNode);
		int numChildren = lastNodeParent.getChildren().size();
		
		// copy children in correct order
		for (int i=indexOfLast+1; i< numChildren; i++) {
			DataFieldNode nodeToCopy = lastNodeParent.getChild(i);
			lastNode.addChild(nodeToCopy);
			nodeToCopy.setParent(lastNode);
		}
		// delete them from the end (reverse order)
		for (int i=numChildren-1; i>indexOfLast; i--) {
			lastNodeParent.removeChild(lastNodeParent.getChild(i));
		}
		
		// loop backwards so that the top field is last added, next to parent
		for (int i=highlightedFields.size()-1; i >=0; i--) {
			promoteDataField(highlightedFields.get(i));
		}
	}
	
	// promotes a dataField to become a sibling of it's parent
	public void promoteDataField(DataFieldNode node) {
		
		DataFieldNode parentNode = node.getParentNode();
		DataFieldNode grandParentNode = parentNode.getParentNode();
		
		// if parent is root (grandparent null) then can't promote
		if (grandParentNode == null) return;
		
		int indexOfParent = grandParentNode.indexOfChild(parentNode);
		
		grandParentNode.addChild(indexOfParent + 1, node);	// adds after parent
		node.setParent(grandParentNode);
		parentNode.removeChild(node);
	}
	
//	 if the highlighted fields have a preceeding sister, move it below the highlighted fields
	public void moveFieldsUp() {
		
		if (highlightedFields.size() == 0) return;
		
		int numFields = highlightedFields.size();

		DataFieldNode firstNode = highlightedFields.get(0);
		int firstNodeIndex = firstNode.getMyIndexWithinSiblings();
		if (firstNodeIndex < 1) return;		// can't move fields up.
		
		DataFieldNode parentNode = firstNode.getParentNode();
		DataFieldNode preceedingNode = parentNode.getChild(firstNodeIndex - 1);
		// add the preceeding node after the last node
		parentNode.addChild(firstNodeIndex + numFields, preceedingNode);
		parentNode.removeChild(preceedingNode);
	}
	
//	 if the highlighted fields have a preceeding sister, move it below the highlighted fields
	public void moveFieldsDown() {
		
		if (highlightedFields.size() == 0) return;
		
		int numFields = highlightedFields.size();

		DataFieldNode lastNode = highlightedFields.get(numFields-1);
		DataFieldNode parentNode = lastNode.getParentNode();
		
		int lastNodeIndex = lastNode.getMyIndexWithinSiblings();
		if (lastNodeIndex == parentNode.getChildren().size() - 1) return;	// can't move fields down.
	
		DataFieldNode succeedingNode = parentNode.getChild(lastNodeIndex + 1);
		// add the succceeding node before the first node
		int indexToMoveTo = lastNodeIndex - numFields + 1;
		parentNode.addChild(indexToMoveTo, succeedingNode);
		// remove the succeeding node (now 1 more position down the list - after inserting above)
		parentNode.removeChild(lastNodeIndex + 2);
	}
	
	public void buildDOMfromTree(Document document, boolean saveExpValues) {

		//DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			// DocumentBuilder db = dbf.newDocumentBuilder();
			//document = db.newDocument();
			Element element = document.createElement(ELEMENT);  
			
			DataField rootField = rootNode.getDataField();
	
			// get all attributes of the datafiel
			LinkedHashMap<String, String> allAttributes = rootField.getAllAttributes();
			parseAttributesMapToElement(allAttributes, element);
			
			document.appendChild(element);
			System.out.println("Tree.buildDOMfromTree appendedChild: " + element.getNodeName());
			
			buildDOMchildrenFromTree(document, rootNode, element, saveExpValues);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public void buildDOMchildrenFromTree(Document document, DataFieldNode rootNode, Element rootElement, boolean saveExpValues) {
		
		ArrayList<DataFieldNode> childNodes = rootNode.getChildren();
		if (childNodes.size() == 0) return;

		
		for (DataFieldNode child: childNodes) {
			
			DataField dataField = child.getDataField();
			Element element = document.createElement(ELEMENT);
			
			LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
			parseAttributesMapToElement(allAttributes, element);
			
			if(!saveExpValues) element.setAttribute(DataField.VALUE, "");
			
			rootElement.appendChild(element);
			System.out.println("Tree.buildDOMchildrenFromTree appendedChild: " + element.getNodeName());
			
			
			buildDOMchildrenFromTree(document, child, element, saveExpValues);
		}  // end for
	}
	
	public void parseAttributesMapToElement(LinkedHashMap<String, String> allAttributes, Element element) {
		
		Iterator keyIterator = allAttributes.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String key = (String)keyIterator.next();
			String value = allAttributes.get(key);
			
			if ((value != null) && (value.length() > 0)) {
				element.setAttribute(key, value);
				System.out.println("Tree.parseAttributesMapToElement key = " + key + ", value = " + value);
			}
		}
	}
	
	// add a blank dataField
	public void addDataField() {
		DataFieldNode newNode = new DataFieldNode(rootNode, this);// make a new default-type field
		addDataField(newNode);
	}
	
	
	// copy and add new dataFields
	public void copyAndInsertDataFields(ArrayList<DataFieldNode> dataFieldNodes) {
		for (DataFieldNode node: dataFieldNodes) {
			
			DataFieldNode newNode = duplicateDataFieldNode(node);
			duplicateDataFieldTree(node, newNode);
			
			addDataField(newNode);
		}
	}
	
//	 copy and add new dataField
	public void copyAndInsertDataField(DataFieldNode node) {
			
		DataFieldNode newNode = duplicateDataFieldNode(node);
		duplicateDataFieldTree(node, newNode);
			
		addDataField(newNode);
		
	}
	
//	 add a new dataField after the last highlighted dataField, then highlights the new field
	public void addDataField(DataFieldNode newNode) {
		
		// get selected Fields and add dataField after last seleted one

		DataFieldNode lastDataField = null;
		
		if (highlightedFields.size() > 0) {
			lastDataField = highlightedFields.get(highlightedFields.size()-1);
		} else {
			// add after last child of protocol (if there are any!)
			int numChildren = getRootNode().getChildren().size();
			if (numChildren > 0) 
				lastDataField = rootNode.getChild(numChildren - 1);
			// otherwise, lastDataField is null, and new dataField will be 1st child of protocol
		}
		
		// if no dataField selected (none exist!), add at top (1st child of protocol)
		if (lastDataField == null) {
			newNode.setParent(rootNode);
			rootNode.addChild(newNode);
			nodeSelected(newNode, true);
		}
		else {
		// otherwise, add after the dataField.
			DataFieldNode parentNode = lastDataField.getParentNode();
			int indexToInsert = lastDataField.getMyIndexWithinSiblings() + 1;

			newNode.setParent(parentNode);
			parentNode.addChild(indexToInsert, newNode);
			
			nodeSelected(newNode, true);
		}
	}
	
//	 delete the highlighted dataFields
	public void deleteDataFields(boolean saveChildren) {
		for (DataFieldNode node: highlightedFields) {
			
			if (saveChildren) promoteAllChildrenToSiblings(node);

			DataFieldNode parentNode = node.getParentNode();
			parentNode.removeChild(node);
		}
		highlightedFields.clear();
	}
	
	public void promoteAllChildrenToSiblings(DataFieldNode node) {
		
		ArrayList<DataFieldNode> children = node.getChildren();
		DataFieldNode parentNode = node.getParentNode();
		int nodeIndex = node.getMyIndexWithinSiblings();
		
		for (int i=children.size() -1; i >=0; i--) {
			parentNode.addChild(nodeIndex + 1, children.get(i));
			children.get(i).setParent(parentNode);
			node.removeChild(children.get(i));
		}
		
	}
	
	
	public void nodeSelected(DataFieldNode selectedNode, boolean clearOthers) {
		
		// always need to deselect rootNode
		rootNode.setHighlighted(false);
		if (selectedNode.getParentNode() == null) {
			rootNode.setHighlighted(true);
		}
		
		if (clearOthers) {
			// clear highlighting from all other dataField panels
			for (DataFieldNode highlightedNode: highlightedFields) {
				highlightedNode.setHighlighted(false);
			} 
			highlightedFields.clear();
			
		} else {	
			// if user tries to select multiple fields, they must have same parent
			// otherwise duplicate and delete operations become very confusing!
			// if no parent node, then this is protocol root node.
			
			DataFieldNode clickedNodeParent = selectedNode.getParentNode();
			
			for (int i=highlightedFields.size()-1; i>=0; i--) {
				DataFieldNode parent = highlightedFields.get(i).getParentNode();
				
				// if parent of an already selected field is not the same as..
				// the clicked-field's parent, de-select it.
				if (!(parent.equals(clickedNodeParent))) {
					highlightedFields.get(i).setHighlighted(false);
					highlightedFields.remove(i);
				}
			}
		}
		
		// add dataField to selected fields (if not protocol (root) field)
		if (selectedNode.getParentNode() != null) 
			addToHighlightedFields(selectedNode);
		
		if (selectionObserver != null) selectionObserver.selectionChanged();
	}
	
//	 need to make sure that highlighted fields (siblings) are sorted in their sibling order
	// and that only consecutive siblings are selected
	public void addToHighlightedFields(DataFieldNode dataFieldNode) {
		
		// if empty, just add
		if (highlightedFields.size() == 0)  {
			highlightedFields.add(dataFieldNode);
			dataFieldNode.setHighlighted(true);
		}
		
		// need to highlight all fields between currently selected fields and newly selected field
		else {
			int siblingIndex = dataFieldNode.getMyIndexWithinSiblings();
			
			// get the max and min indexes of highlighted fields
			int highlightedIndexMax = highlightedFields.get(0).getMyIndexWithinSiblings();
			int highlightedIndexMin = highlightedFields.get(0).getMyIndexWithinSiblings();
			for (DataFieldNode highlightedField: highlightedFields) {
				int index  = highlightedField.getMyIndexWithinSiblings();
				if (index > highlightedIndexMax) highlightedIndexMax = index;
				if (index < highlightedIndexMin) highlightedIndexMin = index;
			}
			
			DataFieldNode parentNode = dataFieldNode.getParentNode();
			
			// if so, add at end of list or at the start
			if (siblingIndex > highlightedIndexMax) {
				for (int i=highlightedIndexMax +1; i<siblingIndex + 1 ; i++) {
					DataFieldNode siblingDataFieldNode = parentNode.getChild(i);
					highlightedFields.add(siblingDataFieldNode);
					siblingDataFieldNode.setHighlighted(true);
				}
			}
			if (siblingIndex < highlightedIndexMin) {
				for (int i=highlightedIndexMin -1; i>siblingIndex - 1 ; i--) {
					DataFieldNode siblingDataFieldNode = parentNode.getChild(i);
					highlightedFields.add(0, siblingDataFieldNode);
					siblingDataFieldNode.setHighlighted(true);
				}
			}
		}
		
	}
	
	
	
	public void copyDefaultValuesToInputFields() {
		
		Iterator iterator = rootNode.createIterator();
			
			while (iterator.hasNext()) {
				DataFieldNode node = (DataFieldNode)iterator.next();
				node.getDataField().copyDefaultValueToInputField();
			}
	}
	

	public DataFieldNode getRootNode() {
		return rootNode;
	}
	
	public JPanel getFieldEditorToDisplay() {
		
		JPanel currentFieldEditor;
		
		if (highlightedFields.size() == 1) {
			currentFieldEditor = highlightedFields.get(0).getDataField().getFieldEditor();
		}
		else if (rootNode.getHighlighted()) {
			currentFieldEditor = rootNode.getDataField().getFieldEditor();
		}
		else
			currentFieldEditor = new FieldEditor();
		
		return currentFieldEditor;
	}
	
	public void dataFieldUpdated() {
		if (xmlUpdateObserver != null) xmlUpdateObserver.xmlUpdated();
	}
	
	public ArrayList<DataFieldNode> getHighlightedFields() {
		return highlightedFields;
	}
}
