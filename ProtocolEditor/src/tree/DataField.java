/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package tree;

import java.util.ArrayList;
import java.util.Iterator;
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

public class DataField 
	implements IAttributeSaver, IDataFieldObservable, IDataFieldSelectable {
	
	// attribute types - see DataFieldConstants.java

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
	
	
	// Datafield has attributes stored in LinkedHashMap
	LinkedHashMap<String, String> allAttributesMap;
	
	// used for display purposes. Corresponds to list of selectedFields in Tree.
	boolean fieldSelected = false;
	public static final String FIELD_SELECTED = "fieldSelected";

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
		
		// default type and name
		setAttribute(DataFieldConstants.INPUT_TYPE, DataFieldConstants.FIXED_PROTOCOL_STEP);
		setAttribute(DataFieldConstants.ELEMENT_NAME, "untitled");

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
	
	/* setAttribute(name, value)
	 * the key method for setting values of the attribute Map.
	 * notifyObservers is true if the edit corresponds to a single undo() action. 
	 * This is passed to the tree (via node) to be added to the undoManager. 
	 */
	public void setAttribute(String name, String value, boolean rememberUndo) {
		System.out.println("DataField.setAttribute(notifyObservers="+ rememberUndo +"): " + name + "=" + value);
		
		if (name.equals(DataFieldConstants.INPUT_TYPE)) {
			this.changeDataFieldInputType(value, rememberUndo);
			return;
		}
		
		String oldValue = allAttributesMap.get(name);
		
		setAttribute(name, value);
		
		if (rememberUndo) {
			// remember what change was made - add it to undo() history
			node.dataFieldUpdated(new EditDataFieldAttribute(this, name, oldValue, value));
			notifyDataFieldObservers();
		}
	}
	
	public void setAttribute(String name, String value) {
		allAttributesMap.put(name, value);
	}
	
	public String getAttribute(String name) {
		return allAttributesMap.get(name);
	}
	
	// used to access boolean attributes, eg SUBSTEPS_COLLAPSED
	public boolean isAttributeTrue(String attributeName) {
		
		// check if attribute is fieldSelected (this not stored in attributeMap)
		if (attributeName.equals(FIELD_SELECTED)) {
			return fieldSelected;
		}
		String value = getAttribute(attributeName);
		if (value == null) return false;
		return (value.equals(DataFieldConstants.TRUE));
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
		if ((getAttribute(DataFieldConstants.INPUT_TYPE) == null)) customElement = true;
		else if (getAttribute(DataFieldConstants.INPUT_TYPE).equals(DataFieldConstants.CUSTOM)) customElement = true;
		return customElement;
	}
	
	public void changeDataFieldInputType(String newInputType, boolean rememberUndo) {
		
		// delete all attributes other than name & description. (not relevant to new input type - probably)
		// unless initializing to "Custom" field (from null), since Custom may have many attributes
		if (!newInputType.equals(DataFieldConstants.CUSTOM)) {
			
			// remember undo 
			if (rememberUndo) {
				node.dataFieldUpdated(new EditDataFieldType(this, getAllAttributes()));
			}
			
//			 keep the original name & description & colour (may be null)
			String copyName = allAttributesMap.get(DataFieldConstants.ELEMENT_NAME);
			String copyDescription = allAttributesMap.get(DataFieldConstants.DESCRIPTION);
			String copyColour = getAttribute(DataFieldConstants.BACKGROUND_COLOUR);
			
			allAttributesMap.clear();
			
			setAttribute(DataFieldConstants.ELEMENT_NAME, copyName);
			setAttribute(DataFieldConstants.DESCRIPTION, copyDescription);
			setAttribute(DataFieldConstants.BACKGROUND_COLOUR, copyColour);
		}
		
		setAttribute(DataFieldConstants.INPUT_TYPE, newInputType);
		
		// new subclasses of formField and fieldEditor will get made when needed
		fieldEditor = null;
		formField = null;
		
		// reset the blue colour
		node.nodeClicked(true);
		
		
		notifyXmlObservers(); 
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
		fieldSelected = highlighted;
		// refresh display 
		// (don't want to call notifyObservers() as this tries to display AttributesDialog, which adds
		// it to the list of listeners, at the same time as moving through the list!) concurrency error!
		formField.dataFieldUpdated();
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
		String name = getAttribute(DataFieldConstants.ELEMENT_NAME);
		if ((name != null) && (name.length() > 0))
			return name;
		else return "untitled";		// have to return SOME text or formField panel may be v.v.small!
	}
	//	 used to update dataField etc when fieldEditor panel is edited
	
	public String getDescription() {
		return getAttribute(DataFieldConstants.DESCRIPTION);
	}
	
	public String getURL() {
		return getAttribute(DataFieldConstants.URL);
	}
	
	public String getInputType() {
		return getAttribute(DataFieldConstants.INPUT_TYPE);
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
			setAttribute(DataFieldConstants.SUBSTEPS_COLLAPSED, (new Boolean(collapsed)).toString());
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
	public void dataFieldSelected(boolean clearOthers) {
		node.nodeClicked(clearOthers);
	}
	
	// used for finding words within an open document, eg to display
	public boolean attributesContainSearchWord(String searchWord) {
		Iterator keyIterator = allAttributesMap.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			String value = allAttributesMap.get(name);
			if ((value != null) && (value.contains(searchWord))) return true;
		}
		return false;
	}
	// used for positioning this field in the view-port of the scroll window
	public int getHeightOfFieldBottom() {
		return formField.getHeightOfPanelBottom();
	}

	// a method used by the Tree class to convert from old xml version to new
	public static String getNewInputTypeFromOldInputType(String oldInputType) {
		if (oldInputType.equals(OLD_PROTOCOL_TITLE)) return DataFieldConstants.PROTOCOL_TITLE;
		if (oldInputType.equals(OLD_FIXED_PROTOCOL_STEP)) return DataFieldConstants.FIXED_PROTOCOL_STEP;
		if (oldInputType.equals(OLD_TEXT_ENTRY_STEP)) return DataFieldConstants.TEXT_ENTRY_STEP;
		if (oldInputType.equals(OLD_MEMO_ENTRY_STEP)) return DataFieldConstants.MEMO_ENTRY_STEP;
		if (oldInputType.equals(OLD_NUMBER_ENTRY_STEP)) return DataFieldConstants.NUMBER_ENTRY_STEP;
		if (oldInputType.equals(OLD_DROPDOWN_MENU_STEP)) return DataFieldConstants.DROPDOWN_MENU_STEP;
		if (oldInputType.equals(OLD_DATE)) return DataFieldConstants.DATE;
		if (oldInputType.equals(OLD_TABLE)) return DataFieldConstants.TABLE;
		if (oldInputType.equals(OLD_CUSTOM)) return OLD_CUSTOM;
		
		return oldInputType;
	}
}
