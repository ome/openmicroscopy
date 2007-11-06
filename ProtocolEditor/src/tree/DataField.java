package tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.undo.UndoableEdit;

import ui.FieldEditor;
import ui.FieldEditorFormFieldFactory;
import ui.FormField;


// the in-memory form of an xml element
// has hash map of attributes, plus FormField and FieldEditor panels to display them

public class DataField {
	
	// attribute types
	// changes to the attributes are reflected in XML element saving.
	// any significant changes should be given a new XML version number (in XMLModel)

	public static final String ELEMENT_NAME = "elementName";
	public static final String DESCRIPTION = "description";
	public static final String VALUE = "value";
	public static final String TEXT_NODE_VALUE ="textNodeValue";
	public static final String DEFAULT = "default";
	public static final String INPUT_TYPE = "inputType";
	public static final String DROPDOWN_OPTIONS = "dropdownOptions";
	public static final String TABLE_COLUMN_NAMES = "tableColumnNames";
	public static final String UNITS = "units";
	public static final String KEYWORDS = "keywords";
	public static final String SUBSTEPS_COLLAPSED ="substepsCollapsed"; // "true" or "false"
	public static final String URL = "url";
	
	// attribute values
	public static final String TRUE = "true";
	public static final String FALSE = "false";
	
	// old input types
	// need to be able to read elements that use attribute: inputType="Fixed Step" 
	// but should be saved as elements named  <FixedStep elementName="etc">  see input types below
	public static final String OLD_PROTOCOL_TITLE = "Protocol Title";
	public static final String OLD_FIXED_PROTOCOL_STEP = "Fixed Step";
	public static final String OLD_TEXT_ENTRY_STEP = "Text";
	public static final String OLD_MEMO_ENTRY_STEP = "Text Box";
	public static final String OLD_NUMBER_ENTRY_STEP = "Number";
	public static final String OLD_DROPDOWN_MENU_STEP = "Drop-down Menu";
	public static final String OLD_DATE = "Date";
	public static final String OLD_TABLE = "Table";
	public static final String OLD_CUSTOM = "Custom";
	
	// input types
	public static final String PROTOCOL_TITLE = "ProtocolTitle";
	public static final String FIXED_PROTOCOL_STEP = "FixedStep";
	public static final String TEXT_ENTRY_STEP = "TextField";
	public static final String MEMO_ENTRY_STEP = "TextBox";
	public static final String NUMBER_ENTRY_STEP = "NumberField";
	public static final String DROPDOWN_MENU_STEP = "DropDownMenu";
	public static final String DATE = "DateField";
	public static final String TABLE = "TableField";
	public static final String CUSTOM = "CustomField";
	public static final String[] INPUT_TYPES = 
	{FIXED_PROTOCOL_STEP, TEXT_ENTRY_STEP,
	MEMO_ENTRY_STEP, DROPDOWN_MENU_STEP, NUMBER_ENTRY_STEP, DATE, TABLE};
	
//	 the names used for the UI - MUST be in SAME ORDER as INPUT_TYPES they correspond to 
	// this means you can change the UI names without changing INPUT_TYPES.
	public static final String[] UI_INPUT_TYPES = 	
	{ "Fixed", "Text", "Text Box", "Drop-down Menu", "Number", "Date", "Table"};
	
	// Datafield has attributes stored in LinkedHashMap
	LinkedHashMap<String, String> allAttributesMap;

	// the two JPanels that display the dataField, and hold optional attributes
	FormField formField;
	FieldEditor fieldEditor;
	
	ArrayList<DataFieldObserver> dataFieldObservers = new ArrayList<DataFieldObserver>();
	
	// the node of the dataField tree structure that holds this datafield
	DataFieldNode node;
	
	// the factory that makes fieldEditor and formField subclasses depending on input type
	static FieldEditorFormFieldFactory factory = FieldEditorFormFieldFactory.getInstance();

	
	public DataField(DataFieldNode node) {
		this.node = node;
		
		allAttributesMap = new LinkedHashMap<String, String>();
		
		// default type
		setAttribute(DataField.INPUT_TYPE, DataField.FIXED_PROTOCOL_STEP, false);

	}
	
	public DataField(LinkedHashMap<String, String> allAttributesMap, DataFieldNode node) {
		this.node = node;
		
		this.allAttributesMap = allAttributesMap;
	}
	
	// returns a duplicate dataField
	public DataField(DataField dataFieldToCopy, DataFieldNode node) {
		
		this.node = node;
		
//		 get all attributes of the original datafield, make a copy
		LinkedHashMap<String, String> allAttributes = dataFieldToCopy.getAllAttributes();
		this.allAttributesMap = new LinkedHashMap<String, String>(allAttributes);
	}
	
	public void setAttribute(String name, String value, boolean notifyDataFieldObservers) {
	
		String oldValue = allAttributesMap.get(name);
		
		allAttributesMap.put(name, value);
		
		if (notifyDataFieldObservers) {
			System.out.println("DataField.setAttribute(notifyObservers="+ notifyDataFieldObservers +"): " + name + "=" + value);
			// remember what change was made - add it to undo() history
			node.dataFieldUpdated(new EditDataFieldAttribute(this, name, oldValue, value));
			
			notifyDataFieldObservers();
		}
	}
	
	public String getAttribute(String name) {
		return allAttributesMap.get(name);
	}
	// used to access boolean attributes, eg SUBSTEPS_COLLAPSED
	public boolean isAttributeTrue(String attributeName) {
		String value = getAttribute(attributeName);
		if (value == null) return false;
		return (value.equals(TRUE));
	}
	// returns false if attribute is null
	public boolean isAttributeEqualTo(String attribute, String equalTo) {
		String value = getAttribute(attribute);
		if (value == null) return false;
		return (value.equals(equalTo));
	}
	// returns true if no InputType has been set, or if it is CUSTOM
	public boolean isCustomInputType() {
		boolean customElement = false;
		if ((getAttribute(DataField.INPUT_TYPE) == null)) customElement = true;
		else if (getAttribute(DataField.INPUT_TYPE).equals(DataField.CUSTOM)) customElement = true;
		return customElement;
	}
	
	public void changeDataFieldInputType(String newInputType) {
		
		// delete all attributes other than name & description. (not relevant to new input type - probably)
		// unless initializing to "Custom" field (from null), since Custom may have many attributes
		if (!newInputType.equals(DataField.CUSTOM)) {
			
			// remember undo 
			node.dataFieldUpdated(new EditDataFieldType(this, getAllAttributes()));
			
//			 keep the original name & description (may be null)
			String copyName = allAttributesMap.get(DataField.ELEMENT_NAME);
			String copyDescription = allAttributesMap.get(DataField.DESCRIPTION);
			
			allAttributesMap.clear();
			
			setAttribute(DataField.ELEMENT_NAME, copyName, false);
			setAttribute(DataField.DESCRIPTION, copyDescription, false);
		}
		
		setAttribute(DataField.INPUT_TYPE, newInputType, false);
		
		// new subclasses of formField and fieldEditor will get made when needed
		fieldEditor = null;
		formField = null;
		
		// reset the blue colour
		node.nodeClicked(true);
		
		// this only needed if call comes from FieldEditor (let fieldEditor do it)
		// notifyDataFieldObservers(); 
	}
	
	public void resetFieldEditorFormField() {
		fieldEditor = null;
		formField = null;
	}
	public void setAllAttributes(LinkedHashMap<String, String> attributes) {
		allAttributesMap = attributes;
	}
	
	// export method. return a LinkedHashMap with all name-value 
	public LinkedHashMap<String, String> getAllAttributes() {

		return allAttributesMap;
	}

	// called when changes are made to allAttributesMap (except trivial changes like substepsCollapsed=true)
	public void notifyDataFieldObservers() {
		// typically observers are formField, fieldEditor. 
		for (DataFieldObserver observer: dataFieldObservers) {
			observer.dataFieldUpdated();
		}
	}
	public void notifyXmlObservers() {
		node.xmlUpdated();
	}
	
	public void addDataFieldObserver(DataFieldObserver observer) {
		dataFieldObservers.add(observer);
	}
	
	public void setHighlighted(boolean highlighted) {
		if (formField == null) getFormField();	// make sure there is one
		
		formField.setHighlighted(highlighted);
	}
	
	public JPanel getFieldEditor() {
		if (fieldEditor == null) {
			fieldEditor = factory.getFieldEditor(this);
		}
		return fieldEditor;
	}
	
	public JPanel getFormField() {
		if (formField == null) {
			formField = factory.getFormField(this);
		}
		return formField;
	}
	
	public String getName() {
		if ((getAttribute(DataField.ELEMENT_NAME) != null) && (getAttribute(DataField.ELEMENT_NAME).length() > 0))
			return getAttribute(DataField.ELEMENT_NAME);
		else return "Name";		// have to return SOME text or formField panel may be v.v.small!
	}
	//	 used to update dataField etc when fieldEditor panel is edited
	
	public String getDescription() {
		return getAttribute(DataField.DESCRIPTION);
	}
	
	public String getURL() {
		return getAttribute(DataField.URL);
	}
	
	public String getInputType() {
		return getAttribute(DataField.INPUT_TYPE);
	}
	
	public DataFieldNode getNode() {
		return node;
	}
	
	public boolean hasChildren() {
		boolean hasChildren = (!(getNode().children.isEmpty()));
		return hasChildren;
	}
	// if there are any children, set their collapsed (visible) state
	public void collapseChildren(boolean collapsed) {
		if (hasChildren()) {
			setAttribute(SUBSTEPS_COLLAPSED, (new Boolean(collapsed)).toString(), false);
			refreshTitleCollapsed();
		}
	}

	public void refreshTitleCollapsed() {
		if (formField == null) getFormField();	// make sure there is one
		formField.refreshTitleCollapsed();
	}
	public void setExperimentalEditing(boolean enabled) {
		if (formField == null) getFormField();	// make sure there is one
		formField.setExperimentalEditing(enabled);
	}
	public ArrayList<JComponent> getVisibleAttributes() {
		if (formField == null) getFormField();	// make sure there is one
		return formField.getVisibleAttributes();
	}
	public void formFieldClicked(boolean clearOthers) {
		node.nodeClicked(clearOthers);
	}
	public void hideChildren(boolean hidden) {
		node.hideChildren(hidden);
	}

	// a method used by the Tree class to convert from old xml version to new
	public static String getNewInputTypeFromOldInputType(String oldInputType) {
		if (oldInputType.equals(OLD_PROTOCOL_TITLE)) return PROTOCOL_TITLE;
		if (oldInputType.equals(OLD_FIXED_PROTOCOL_STEP)) return FIXED_PROTOCOL_STEP;
		if (oldInputType.equals(OLD_TEXT_ENTRY_STEP)) return TEXT_ENTRY_STEP;
		if (oldInputType.equals(OLD_MEMO_ENTRY_STEP)) return MEMO_ENTRY_STEP;
		if (oldInputType.equals(OLD_NUMBER_ENTRY_STEP)) return NUMBER_ENTRY_STEP;
		if (oldInputType.equals(OLD_DROPDOWN_MENU_STEP)) return DROPDOWN_MENU_STEP;
		if (oldInputType.equals(OLD_DATE)) return DATE;
		if (oldInputType.equals(OLD_TABLE)) return TABLE;
		if (oldInputType.equals(OLD_CUSTOM)) return OLD_CUSTOM;
		
		return oldInputType;
	}
}
