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

package ui;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.Tree;
import tree.Tree.Actions;

/**
 * Methods implemented by <code>Controller</code> that are mostly delegated to model. 
 * @author will
 *
 */

public interface IModel {
	
	/**
	 * This checks to see if any highlighted field that has a default value, also has a value that 
	 * would be over-written if defaults were loaded. 
	 * Used to give users a warning that loading defaults (highlighted fields) would over-write stuff. 
	 * 
	 * @return	True if any highlighted field with a default value is not empty.
	 */
	public boolean isAnyHighlightedDefaultFieldFilled();
	
	/**
	 * This checks to see if any field that has a default value, also has a value that 
	 * would be over-written if defaults were loaded. 
	 * Used to give users a warning that loading defaults (whole tree) would over-write stuff. 
	 * 
	 * @return	True if any field with a default value is not cleared.
	 */
	public boolean isAnyDefaultFieldFilled();
	
	/**
	 * This checks to see if any field marked as "Required" (DataFieldConstants.REQUIRED_FIELD = 'true')
	 * is also not filled out (ie dataField.isFieldFilled() is false). 
	 * Used for ensuring that "required" fields are not left blank when the form is saved.
	 * This is delegated to Tree. 
	 * 
	 * @return	True if any required field is not filled out. 
	 */
	public boolean isAnyRequiredFieldEmpty();
	
	/**
	 * This method tests to see if any of the currently highlighted fields 
	 * are locked. 
	 * If so, various editing actions are disabled. 
	 * 
	 * @return	true if any of the fields are locked. 
	 * Fields are considered locked if their ancestors are locked.
	 */
	public boolean areHighlightedFieldsLocked();
	
	/**
	 * This method is used to get details of the highlighted fields that are locked. 
	 * Each highlighted field that is locked is represented by a HashMap, 
	 * containing "locking attributes" of the field, such as:
	 * DataFieldConstants.ELEMENT_NAME
	 * DataFieldConstants.LOCKED_FIELD_UTC
	 * DataFieldConstants.LOCKED_FIELD_USER_NAME
	 * DataFieldConstants.LOCK_LEVEL
	 * 
	 * @return		A list of hashMaps, corresponding to the list of highlighted locked fields. 
	 */
	public List<HashMap<String, String>> getLockedFieldsAttributes();
	
	/**
	 * This checks whether ancestors of the highlighted fields
	 * have the attribute FIELD_LOCKED_UTC.
	 * This method IGNORES the highlighted fields themselves. 
	 * Presence of this attribute indicates that ancestors of the highlighted fields
	 * are "Locked" and editing is not allowed. 
	 * 
	 * @return	true if any of the highlighted field ancestors has the attribute FIELD_LOCKED_UTC.
	 */
	public boolean areAncestorFieldsLocked();
	
	/**
	 * This checks whether the highlighted fields are locked (ie have the attribute LOCK_LEVEL)
	 * and returns the "max" level of locking for all the highlighted fields.
	 * ie LOCKED_ALL_ATTRIBUTES is a 'higher' level than LOCKED_TEMPLATE. Returns null if no
	 * fields are locked. 
	 * Editing actions that apply to the currently highlighted fields can setEnabled(), based
	 * on this locking level. 
	 * 
	 * @return	the max "lockLevel" of highlighted fields, or null if none are locked.
	 */
	public String getMaxHighlightedLockingLevel();
	
	/**
	 * This checks whether ANY fields in this tree are locked (ie have the attribute LOCK_LEVEL)
	 * and returns the "max" level of locking for the tree.
	 * ie LOCKED_ALL_ATTRIBUTES is a 'higher' level than LOCKED_TEMPLATE. Returns null if no
	 * fields are locked. Editing actions that 
	 * apply to the whole tree (eg Clear All Fields or Load Defaults All Fields) should be disabled 
	 * if all attributes locked, but enabled if only the template is locked. 
	 * 
	 * @return	the max "lockLevel" if any fields in the tree are locked, or null if no fields locked.
	 */
	public String getMaxLockingLevel();
	
	/**
	 * This adds a time-stamp (UTCmillisecs) to each field, to indicate
	 * that they are locked (and when). 
	 * Other attributes in the lockingAttributes map will also be added, 
	 * to describe the User, Locking Level etc. 
	 * 
	 * @param lockingAttributes		A map of additional attributes that define the lock
	 */
	public void lockHighlightedFields(Map<String, String> lockingAttributes);
	
	public void openBlankProtocolFile();
	
	/**
	 * Allows classes that have created their own trees to open them as new
	 * files. 
	 * 
	 * @param tree
	 */
	public void openTree(Tree tree);
	
	public void openThisFile(File file);
	
	public File getCurrentFile();
	
	/**
	 * This Saves the data in the current Tree, into an XML file, as
	 * defined by "file";
	 * This method is used by "Save" and "Save-As" actions.
	 * 
	 * @return 	true if saving went OK (no exceptions etc). 
	 */
	public boolean saveTreeToXmlFile(File file);
	
	
	/**
	 * This provides the same functionality as 
	 * saveTreeToXmlFile(file) except:
	 * The tree's file is not updated with the xmlFile - Still references the file the tree came from.
	 * The tree method setEdited(false) is not called.
	 * The calendar database is not updated with the newly saved file. 
	 * 
	 * This method is simply for exporting the tree, as a file, without changing the tree. 
	 * 
	 * @param xmlFile
	 * @return	true if successful
	 * @see  saveToXmlFile(file);
	 */
	public boolean exportTreeToXmlFile(File xmlFile);

	public String[] getOpenFileList();
	
	public DataFieldNode getRootNode();
	
	public List<DataFieldNode> getHighlightedFields();
	
	public void editCurrentTree(Actions newAction);
	
	public void multiplyValueOfSelectedFields(float factor);
	
	// undo/redo
	public boolean canUndo();
	public boolean canRedo();
	public String getUndoCommand();
	public String getRedoCommand();
	
	// file selection
	public int getCurrentFileIndex();
	
	public void changeCurrentFile(int index);
	
	public void closeCurrentFile();
	
	// flag for updating 
	public boolean treeNeedsRefreshing();
	
	// find
	public List<DataField> getSearchResults(String searchWord);
	
	// copy - paste
	public void copyHighlightedFieldsToClipboard();
	
	public void pasteHighlightedFieldsFromClipboard();
	
	// import file
	public void setImportFile(File xmlFile);
	
	public DataFieldNode getImportTreeRoot();
	
	public void importFieldsFromImportTree();
	
	public boolean isCurrentFileEdited();
	
	/*
	 * calendar functions
	 */ 
	
	/**
	 * Delegate calendar display etc to the calendar main class. 
	 * This has already has been instantiated (connection and alarm-checker setup)
	 * so now just need to display the calendar. 
	 */
	public void displayCalendar();
	
	/**
	 * Asks the user to confirm that it is OK to overwrite the database (clear tables)
	 * and then asks for a root directory for all Omero.editor files. 
	 * Iterates through all files, adding those that contain dates to the calendar. 
	 */
	public void repopulateCalendarDB();
	
	/**
	 * Exports DateTime fields in the current File(tree) to an iCalendar(ics) format file,
	 * specified by the exportFilePath. 
	 * 
	 * @param exportFilePath	The path and file name to export to.
	 */
	public void exportFileEventsToICalendar(String exportFilePath);
}
