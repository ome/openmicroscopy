package xmlMVC;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

// Tree manages the tree data structure
// also knows which fields are currently selected (applies actions to these)

public class Tree implements Visitable{
	
	public static final int MOVE_FIELDS_UP = 0;
	public static final int MOVE_FIELDS_DOWN = 1;
	public static final int DELTE_FIELDS = 3;
	public static final int DELETE_FIELDS_SAVE_CHILDREN = 4;
	public static final int ADD_NEW_FIELD = 5;
	public static final int DEMOTE_FIELDS = 6;
	public static final int PROMOTE_FIELDS = 7;
	public static final int DUPLICATE_FIELDS = 8;
	public static final int COPY_FIELDS = 9;
	public static final int PASTE_FIELDS = 10;
	
	private DataFieldNode rootNode;		// the root of the dataField tree. 
	
	private File file;		// the file that this tree is built from
	private boolean treeEdited = false;
	
	private ArrayList<DataFieldNode> highlightedFields;
	private ArrayList<DataFieldNode> copiedToClipboardFields = new ArrayList<DataFieldNode>();
	
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
		
		DataFieldNode newNode = new DataFieldNode(this);// make a new default-type field
		newNode.setParent(rootNode);
		rootNode.addChild(newNode);
	}
	
	private void buildTreeFromDOM(DataFieldNode dfNode, Element inputElement) {
		
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
					 dfNode.getDataField().setAttribute(DataField.TEXT_NODE_VALUE, node.getTextContent(), false);
					 // dfNode.getDataField().changeDataFieldInputType(DataField.TEXT_ENTRY_STEP);
					 // System.out.println("Tree.buildTreeFromDom: Text Node value is " + node.getTextContent());
				 }
			 }
		}
		
	}
	
	// make a copy of the currently highlighted fields
	private void copyHighlightedFieldsToClipboard() {
		copiedToClipboardFields = new ArrayList<DataFieldNode>(highlightedFields);
		}
	// paste the clipboard fields (after the last currently selected field)
	private void pasteClipboardFields() {
		copyAndInsertDataFields(copiedToClipboardFields);
	}
	
	public void multiplyValueOfSelectedFields(float factor) {
		
		for (DataFieldNode numberField: highlightedFields) {
			DataField dataField = numberField.getDataField();
			
			try {
				FormFieldNumber formFieldNumber = (FormFieldNumber)dataField.getFormField();
				formFieldNumber.multiplyCurrentValue(factor);
			} catch (Exception ex) {
				// cast failed: formField is not a Number field
			}
		}
	}
	
	private void parseElementToMap(Element element, LinkedHashMap<String, String> allAttributes) {
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
	private void duplicateAndInsertDataFields() {
		
		if (highlightedFields.isEmpty()) return;
		
		// highlighted fields change while adding. Make a copy first
		ArrayList<DataFieldNode> tempArray = new ArrayList<DataFieldNode>(highlightedFields);
		
		copyAndInsertDataFields(tempArray);
	}

	// duplicates all branches below oldNode, adding them to newNode
	private void duplicateDataFieldTree(DataFieldNode oldNode, DataFieldNode newNode) {
		
		ArrayList<DataFieldNode> children = oldNode.getChildren();
		if (children.size() == 0) return;
			
			for (DataFieldNode child: children){
			 
				DataFieldNode newChild = new DataFieldNode(child, this);
				newChild.setParent(newNode);
				
				newNode.addChild(newChild);
				
				duplicateDataFieldTree(child, newChild);
			}
	}
	
	public void editTree(int treeCommand) {
		
		switch (treeCommand) {
		
			case ADD_NEW_FIELD: {
				addDataField();
				return;
			}
			case DELTE_FIELDS: {
				deleteDataFields(false);
				return;
			}
			case DELETE_FIELDS_SAVE_CHILDREN: {
				deleteDataFields(true);
				return;
			}
			case MOVE_FIELDS_UP: {
				moveFieldsUp();
				return;
			}
			case MOVE_FIELDS_DOWN: {
				moveFieldsDown();
				return;
			}
			case PROMOTE_FIELDS: {
				promoteDataFields();
				return;
			}
			case DEMOTE_FIELDS: {
				demoteDataFields();
				return;
			}
			case DUPLICATE_FIELDS: {
				duplicateAndInsertDataFields();
				return;
			}
			case COPY_FIELDS: {
				copyHighlightedFieldsToClipboard();
				return;
			}
			case PASTE_FIELDS: {
				pasteClipboardFields();
				return;
			}
		}
	}

	// add a blank dataField
	private void addDataField() {
		DataFieldNode newNode = new DataFieldNode(this);// make a new default-type field
		addDataField(newNode);
		
		setTreeEdited(true);
	}

	//	 add a new dataField after the last highlighted dataField
	private void addDataField(DataFieldNode newNode) {
		
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
		}
		else {
		// otherwise, add after the dataField.
			DataFieldNode parentNode = lastDataField.getParentNode();
			int indexToInsert = lastDataField.getMyIndexWithinSiblings() + 1;
	
			newNode.setParent(parentNode);
			parentNode.addChild(indexToInsert, newNode);
		}
		nodeSelected(newNode, true); // select the new node
	}

	
	// copy and add new dataFields
	// used by import, paste, and duplicate functions
	public void copyAndInsertDataFields(ArrayList<DataFieldNode> dataFieldNodes) {
		
		if (dataFieldNodes.isEmpty()) return;
		
		//remember the first node added, so all new nodes can be selected when done
		DataFieldNode firstNewNode = null;
		
		for (int i=0; i< dataFieldNodes.size(); i++){
			
			DataFieldNode newNode = new DataFieldNode(dataFieldNodes.get(i), this);
			duplicateDataFieldTree(dataFieldNodes.get(i), newNode);
			
			addDataField(newNode);	// adds after last selected field.
			
			if (i == 0) firstNewNode = newNode;
		}
		
		setTreeEdited(true);
		nodeSelected(firstNewNode, false);   // will select the range 
	}
	
	
	private void demoteDataFields() {
		
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
		
		setTreeEdited(true);
	}
	
	private void promoteDataFields() {
		
		if (highlightedFields.isEmpty()) return;
		
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
		
		setTreeEdited(true);
	}
	
	// promotes a dataField to become a sibling of it's parent
	private void promoteDataField(DataFieldNode node) {
		
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
	private void moveFieldsUp() {
		
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
		
		setTreeEdited(true);
	}
	
//	 if the highlighted fields have a preceeding sister, move it below the highlighted fields
	private void moveFieldsDown() {
		
		if (highlightedFields.size() == 0) return;
		
		int numFields = highlightedFields.size();

		DataFieldNode lastNode = highlightedFields.get(numFields-1);
		DataFieldNode parentNode = lastNode.getParentNode();
		
		int lastNodeIndex = lastNode.getMyIndexWithinSiblings();
		if (lastNodeIndex == parentNode.getChildren().size() - 1) return;	// can't move fields down.
	
		DataFieldNode succeedingNode = parentNode.getChild(lastNodeIndex + 1);
		// add the succeeding node before the first node
		int indexToMoveTo = lastNodeIndex - numFields + 1;
		parentNode.addChild(indexToMoveTo, succeedingNode);
		// remove the succeeding node (now 1 more position down the list - after inserting above)
		parentNode.removeChild(lastNodeIndex + 2);
		
		setTreeEdited(true);
	}
	
	// used to export the tree to DOM document
	public void buildDOMfromTree(Document document, boolean saveExpValues) {

		//DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			// DocumentBuilder db = dbf.newDocumentBuilder();
			//document = db.newDocument();
			DataField rootField = rootNode.getDataField();
			
			String elementName = ELEMENT;
		
			
			boolean customElement = false;
			if ((rootField.getInputType() == null)) customElement = true;
			else if (rootField.getInputType().equals(DataField.CUSTOM)) customElement = true;
			
			// if custom XML element, use the elementName attribute as the element Name
			if (customElement) elementName = rootField.getName();
			if (elementName == null) elementName = ELEMENT; 	// just in case!
			
			Element element = document.createElement(elementName);  
			
			// get all attributes of the datafiel
			LinkedHashMap<String, String> allAttributes = rootField.getAllAttributes();
			parseAttributesMapToElement(allAttributes, element);
			
			document.appendChild(element);
			// System.out.println("Tree.buildDOMfromTree appendedChild: " + element.getNodeName());
			
			buildDOMchildrenFromTree(document, rootNode, element, saveExpValues);
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	// recursive function to build DOM from tree
	private void buildDOMchildrenFromTree(Document document, DataFieldNode rootNode, Element rootElement, boolean saveExpValues) {
		
		ArrayList<DataFieldNode> childNodes = rootNode.getChildren();
		if (childNodes.size() == 0) return;

		
		for (DataFieldNode child: childNodes) {
			
			DataField dataField = child.getDataField();
			
			boolean customElement = false;
			if ((dataField.getInputType() == null)) customElement = true;
			else if (dataField.getInputType().equals(DataField.CUSTOM)) customElement = true;
					
			String elementName = ELEMENT;
			
			// if custom XML element, use the elementName attribute as the element Name
			if (customElement) elementName = dataField.getName();
			if (elementName == null) elementName = ELEMENT; 	// just in case!
			
			Element element = document.createElement(elementName);
			
			LinkedHashMap<String, String> allAttributes = dataField.getAllAttributes();
			parseAttributesMapToElement(allAttributes, element);
			
			// if saving Protocol, don't save exp values. 
			if(!saveExpValues) {
				if (allAttributes.get(DataField.VALUE) != null) {  // if there is a Value attribute
					element.setAttribute(DataField.VALUE, "");		// clear exp value
				}
			}
			
			// if custom xml Element that has a text node value, save it! 
			if (customElement) {
				String text = dataField.getAttribute(DataField.TEXT_NODE_VALUE);
				if (text != null)
					element.setTextContent(text);
			}
			
			rootElement.appendChild(element);
			
			buildDOMchildrenFromTree(document, child, element, saveExpValues);
		}  // end for
	}
	
	// copies each dataField's attribute Hash Map into element's attributes
	private void parseAttributesMapToElement(LinkedHashMap<String, String> allAttributes, Element element) {
		
		boolean customElement = false;
		if ((allAttributes.get(DataField.INPUT_TYPE) == null)) customElement = true;
		else if (allAttributes.get(DataField.INPUT_TYPE).equals(DataField.CUSTOM)) customElement = true;
				
		Iterator keyIterator = allAttributes.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String key = (String)keyIterator.next();
			String value = allAttributes.get(key);
			
			// if you want to recreate original xml, don't include "extra" attributes
			if (customElement) {
				if (key.equals(DataField.ELEMENT_NAME) || key.equals(DataField.SUBSTEPS_COLLAPSED) || key.equals(DataField.TEXT_NODE_VALUE))
					continue;
			}
			
			if ((value != null) && (value.length() > 0)) {
				element.setAttribute(key, value);
				// System.out.println("Tree.parseAttributesMapToElement key = " + key + ", value = " + value);
			}
		}
	}
	
//	 delete the highlighted dataFields (option to save children by promoting them first)
	public void deleteDataFields(boolean saveChildren) {
		for (DataFieldNode node: highlightedFields) {
			
			if (saveChildren) promoteAllChildrenToSiblings(node);

			DataFieldNode parentNode = node.getParentNode();
			parentNode.removeChild(node);
		}
		highlightedFields.clear();
		setTreeEdited(true);
	}
	
	private void promoteAllChildrenToSiblings(DataFieldNode node) {
		
		ArrayList<DataFieldNode> children = node.getChildren();
		DataFieldNode parentNode = node.getParentNode();
		int nodeIndex = node.getMyIndexWithinSiblings();
		
		for (int i=children.size() -1; i >=0; i--) {
			parentNode.addChild(nodeIndex + 1, children.get(i));
			children.get(i).setParent(parentNode);
			node.removeChild(children.get(i));
		}
		
	}
	
	// called (via dataField) by clicking on FormField to highlight it
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
	private void addToHighlightedFields(DataFieldNode dataFieldNode) {
		
		// if empty, just add
		if (highlightedFields.size() == 0)  {
			highlightedFields.add(dataFieldNode);
			dataFieldNode.setHighlighted(true);
		}
		
		// need to highlight all fields between currently selected fields and newly selected field
		else {
			int siblingIndex = dataFieldNode.getMyIndexWithinSiblings();
			
			// get the max and minimum indexes of highlighted fields
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
	
	// called by button on Edit Experiment tab
	public void copyDefaultValuesToInputFields() {
		
		class vis implements DataFieldVisitor {
			public void visit(DataField dataField) {
				dataField.copyDefaultValueToInputField();
			}
		}
		this.acceptVistor(new vis());
	}
	
	public void collapseAllChildren(boolean collapsed) {
		Iterator<DataFieldNode> iterator = rootNode.createIterator();
		
		while (iterator.hasNext()) {
			DataFieldNode node = (DataFieldNode)iterator.next();
			node.getDataField().collapseChildren(collapsed);
		}
	}

	public DataFieldNode getRootNode() {
		return rootNode;
	}
	
	// called when the UI needs to display the FieldEditor
	// if only one field is currently selected, return it. Else return blank
	public JPanel getFieldEditorToDisplay() {
		
		JPanel currentFieldEditor;
		
		if (highlightedFields.size() == 1) {
			currentFieldEditor = highlightedFields.get(0).getFieldEditor();
		}
		else if (rootNode.getHighlighted()) {
			currentFieldEditor = rootNode.getFieldEditor();
		}
		else
			currentFieldEditor = new FieldEditor();
		
		return currentFieldEditor;
	}
	
	// called by dataField (via Node) to notify UI of changes
	public void dataFieldUpdated() {
		setTreeEdited(true);
		if (xmlUpdateObserver != null) xmlUpdateObserver.xmlUpdated();
	}
	
	public ArrayList<DataFieldNode> getHighlightedFields() {
		return highlightedFields;
	}
	
	// keep a note of the file that corresponds to this tree
	public void setFile (File file) {
		this.file = file;
	}
	public File getFile () {
		return file;
	}
	
	// when the data structure changes, edited = true. When saved, edited = false
	public void setTreeEdited(boolean edited) {
		treeEdited = edited;
	}
	
	public boolean isTreeEdited() {
		return treeEdited;
	}

	// used for visiting all dataFields (via Nodes) to call some method
	public void acceptVistor(DataFieldVisitor visitor) {
		Iterator<DataFieldNode> iterator = rootNode.createIterator();
		
		while (iterator.hasNext()) {
			DataFieldNode node = (DataFieldNode)iterator.next();
			node.acceptVistor(visitor);
		}
	}
}
