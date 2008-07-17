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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.undo.UndoableEdit;

import fields.FieldPanel;

import tree.edit.EditDataFieldAttribute;
import tree.edit.EditDataFieldAttributes;
import tree.edit.EditDataFieldType;
import ui.FieldEditorFormFieldFactory;
import ui.fieldEditors.FieldEditor;


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
	HashMap<String, String> allAttributesMap;
	
	// used for display purposes. Corresponds to list of selectedFields in Tree.
	boolean fieldSelected = false;
	public static final String FIELD_SELECTED = "fieldSelected";

	// the two JPanels that display the dataField, and hold optional attributes
	FieldPanel formField;
	FieldEditor fieldEditor;
	
	// Observers of the datafield. eg formField and fieldEditor
	ArrayList<DataFieldObserver> dataFieldObservers = new ArrayList<DataFieldObserver>();
	
	// the node of the dataField tree structure that holds this dataField
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
	
	public DataField(HashMap<String, String> allAttributesMap, DataFieldNode node) {
		this.node = node;
		
		this.allAttributesMap = allAttributesMap;
	}
	
	// returns a duplicate dataField
	public DataField(DataField dataFieldToCopy, DataFieldNode node) {
		
		this.node = node;
		
//		 get all attributes of the original datafield, make a copy
		HashMap<String, String> allAttributes = dataFieldToCopy.getAllAttributes();
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
	
	/** 
	 * This method allows users to update several attributes at once. 
	 * The title is used for display purposes in the undo/redo queue.
	 * This is only added to undo/redo queue if rememberUndo is true;
	 * @return 
	 */
	public Map<String, String> setAttributes(String title, Map<String, String> keyValuePairs, boolean rememberUndo) {
		System.out.println("DataField.setAttributeS (notifyObservers="+ rememberUndo +"): " + title);
		
		/*
		 * Make a map of the old values, while setting the new ones
		 */
		HashMap<String, String> oldValues = new HashMap<String, String>();
		Iterator iterator = keyValuePairs.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			oldValues.put(key, getAttribute(key));
			setAttribute(key, (keyValuePairs.get(key) == null ? null : keyValuePairs.get(key).toString()));
		}
		
		if (rememberUndo) {
			// remember what change was made - add it to undo() history
			node.dataFieldUpdated(new EditDataFieldAttributes(this, title, oldValues, keyValuePairs));
			notifyDataFieldObservers();
		}
		
		/*
		 * Return the old values Map. Allows other classes to 
		 * remember them if they wish (if rememberUndo is false, eg EditLockFields)
		 */
		return oldValues;
	}
	
	public void setAttribute(String name, String value) {
		System.out.println("      setAttribute: " + name + "=" + value);
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
		// Also removes things like "TimeEditor". 
		clearDataFieldObservers();
		
		fieldEditor = null;
		formField = null;
		
		// reset the blue colour
		node.nodeClicked(true);
		
		// Causes UI fields to be re-drawn. 
		// (selectionChanged(), called by dataFieldUpdated(edit) is not 
		// sufficient to cause fields to be re-drawn). 
		notifyXmlObservers(); 
	}
	
	public void resetFieldEditorFormField() {
		fieldEditor = null;
		formField = null;
	}
	public void setAllAttributes(HashMap<String, String> attributes) {
		allAttributesMap = attributes;
	}
	
	// export method. return a LinkedHashMap with all name-value 
	public HashMap<String, String> getAllAttributes() {

		return allAttributesMap;
	}

	// called when changes are made to allAttributesMap (except trivial changes like substepsCollapsed=true)
	public void notifyDataFieldObservers() {
		// typically observers are formField, fieldEditor. 
		for (DataFieldObserver observer: dataFieldObservers) {
			observer.dataFieldUpdated();
		}
	}
	
	/**
	 * This method uses a TreeVisitor to iterate through the children of this field
	 * and call notifyDataFieldObservers() for each.
	 * Also calls notifyDataFieldObservers() for This field. 
	 */
	public void notifyObserversOfChildFields() {
		
		TreeVisitor treeVisitor = new TreeVisitor(new DataFieldUpdatedVisitor());
		treeVisitor.visitTree(node);
	}
	
	public void notifyXmlObservers() {
		node.xmlUpdated();
	}
	
	public void addDataFieldObserver(DataFieldObserver observer) {
		dataFieldObservers.add(observer);
	}
	public void removeDataFieldObserver(DataFieldObserver observer) {
		dataFieldObservers.remove(observer);
	}
	public void clearDataFieldObservers() {
		dataFieldObservers.clear();
	}
	
	public void setHighlighted(boolean highlighted) {
		//fieldSelected = highlighted;	// this idea doesn't work so well... see below
		// refresh display 
		// (don't want to call notifyObservers() as this tries to display AttributesDialog, which adds
		// it to the list of listeners, at the same time as moving through the list!) concurrency error!
		// Also, calling dataFieldUpdated when highlighting changes can refresh a field with old values
		// when it becomes un-highlighted following editing, instead of saving new values!
		if (formField == null)
			getFormField();
		
		formField.setSelected(highlighted);
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
	
	/**
	 * This method looks for the "highest" locking level (value of DataFieldConstants.LOCK_LEVEL attribute)
	 * possessed by any ancestor of this field. 
	 * Once the highest value is found, no more
	 * ancestors are checked and this value is returned.
	 * 
	 * @return		String representing the highest lock_level of any ancestors of this field or null if not locked. 
	 */
	public String getHighestAncestorLockedLevel() {
		
		String highestLockLevel = null;
		
		if (node != null) {
			// iterate through the ancestors of this field...
			Iterator<DataFieldNode> iterator = new AncestorIterator(node);
			
			while(iterator.hasNext()) {
				IAttributeSaver field = iterator.next().getDataField();
				/*
				 * If locked, remember lock level. 
				 * If it's the highest lock level (lock all attributes), return it.
				 */
				String lockLevel = field.getAttribute(DataFieldConstants.LOCK_LEVEL);
				if (lockLevel != null) {
					if (lockLevel.equals(DataFieldConstants.LOCKED_ALL_ATTRIBUTES)) {
						return lockLevel;
					}
					highestLockLevel = lockLevel;
				}
			}
		}
		return highestLockLevel;
	}
	
	/**
	 * This method looks for the locking level (value of DataFieldConstants.LOCK_LEVEL attribute)
	 * possessed by the closest ancestor of this field. 
	 * Once a value is found for the closest ancestor, no more
	 * ancestors are checked and this value is returned.
	 * 
	 * @return		String representing the lock_level of the closest locked ancestors of this field or null if not locked. 
	 */
	public String getAncestorLockedLevel() {
		String lockLevel = null;
		if (node != null) {
			// iterate through the ancestors of this field...
			Iterator<DataFieldNode> iterator = new AncestorIterator(node);
			while(iterator.hasNext()) {
				IAttributeSaver field = iterator.next().getDataField();
				/*
				 * Return the first locked level that is not null. 
				 */
				if (field.getAttribute(DataFieldConstants.LOCK_LEVEL) != null) {
					return field.getAttribute(DataFieldConstants.LOCK_LEVEL);
				}
			}
		}
		return lockLevel;
	}
	
	/**
	 * This method looks for the locking level (value of DataFieldConstants.LOCK_LEVEL attribute)
	 * possessed by this field or it's closest locked ancestor (if this field is not locked)
	 * Eg. this field may have TEMPLATE_LOCKED, so, even if an ancestor has LOCKED_ALL_ATTRIBUTES,
	 * the value 'TEMPLATE_LOCKED' should be returned since this applies to this field. 
	 * This allows 'nesting' of locking level, like CSS. 
	 * 
	 * @return		String representing the highest lock_level of this field or its ancestors or null if not locked.
	 */
	public String getLockedLevel() {
		
		String lockLevel = getAttribute(DataFieldConstants.LOCK_LEVEL);

		// if this field is locked, return this value. 
		if (lockLevel != null) {
			return lockLevel; 
		}
		
		// otherwise, get the lock level from the closest ancestor. 
		else {
			return getAncestorLockedLevel();
		}
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

	
	public void dataFieldSelected(boolean clearOthers) {
		node.nodeClicked(clearOthers);
	}
	
	// used for finding words within an open document, eg to display
	public boolean attributesContainSearchWord(String searchWord) {
		Iterator keyIterator = allAttributesMap.keySet().iterator();
		
		while (keyIterator.hasNext()) {
			String name = (String)keyIterator.next();
			String value = allAttributesMap.get(name);
			if (value != null) {
				value = value.toLowerCase();
				searchWord = searchWord.toLowerCase();
				if (value.contains(searchWord)) return true;
			}
		}
		return false;
	}
	// used for positioning this field in the view-port of the scroll window
	public int getHeightOfFieldBottom() {
		if (formField == null) getFormField();	// make sure there is one
		return formField.getHeightOfPanelBottom();
	}
	
	/**
	 * This method tests to see whether the field has been filled out. 
	 * ie, Has the user entered a "valid" value into the Form. 
	 * For fields that have a single 'value', this method will return true if 
	 * that value is filled (not null). 
	 * For fields with several attributes, it depends on what is considered 'filled'.
	 * This method can be used to check that 'Obligatory Fields' have been completed 
	 * when a file is saved. 
	 * Subclasses should override this method.
	 * 
	 * @return	True if the field has been filled out by user. Required values are not null. 
	 */
	public boolean isFieldFilled() {
		if (formField == null) getFormField();	// make sure there is one
		return formField.isFieldFilled();
	}
	
	// used for getting the destination attribute for storing value of this field
	// (eg where to copy the default value when loading defaults).
	public String[] getValueAttributes() {
		if (formField == null) getFormField();	// make sure there is one
		return formField.getValueAttributes();
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
