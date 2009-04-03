 /*
 * tree.ITreeManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 */

package tree;

//Java imports

//Third-party libraries

//Application-internal dependencies

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import tree.Tree.Actions;


/** 
 * All the methods needed to edit the tree.
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface ITreeManager
	extends ITreeModel{
	
	/**
	 * A command that performs editing on the Tree, according to the
	 * currently selected fields. 
	 * @param action
	 */
	public void editTree(Actions action);
	
	
	/**
	 * This adds a time-stamp (UTCmillisecs) to each field, to indicate
	 * that they are locked (and when). 
	 * Other attributes in the lockingAttributes map will also be added, 
	 * to describe the User, Locking Level etc. 
	 * 
	 * @param lockingAttributes		A map of additional attributes that define the lock
	 */
	public void lockHighlightedFields(Map<String, String> lockingAttributes);
	
	/**
	 * For all highlighted fields that are Number Fields, the numerical value
	 * they contain is multiplied by the factor argument. 
	 * Fields of other types are ignored. 
	 * 
	 * @param factor	The value to multiply numerical field values by. 
	 */
	public void multiplyValueOfSelectedFields(float factor);
	
	/**
	 *  getSearchResults(searchWord)
	 *  returns an ArrayList of DataField objects that contain the seachWord
	 */
	public ArrayList<DataField> getSearchResults(String searchWord);
	
	/**
	 * called when the UI needs to display the FieldEditor.
	 * If only one field is currently selected, return it. Else return
	 * a blank panel
	 */ 
	public JPanel getFieldEditorToDisplay();
	
	/**
	 * Used to take a list of Nodes, eg from clip-board, or another tree,
	 * to duplicate them, then insert them after the last highlighted
	 * field (or after last child of root if none highlighted)
	 * 
	 * @param dataFieldNodes
	 */
	public void copyAndInsertDataFields(ArrayList<DataFieldNode> dataFieldNodes);
	
	/**
	 * Gets a list of the currently highlighted nodes. 
	 * Used by other classes for example, to copy and paste.
	 * 
	 * @return		The highlighted nodes. 
	 */
	public ArrayList<DataFieldNode> getHighlightedFields();
	
	/**
	 * This checks whether the highlighted fields
	 * have the attribute FIELD_LOCKED_UTC.
	 * Presence of this attribute indicates that the highlighted fields 
	 * are "Locked" and editing is not allowed. 
	 * 
	 * @return	true if any of the highlighted fields has the attribute FIELD_LOCKED_UTC.
	 */
	public boolean areHighlightedFieldsLocked();
	
	/**
	 * This method is used to get details of the highlighted fields that are locked. 
	 * Each highlighted field that is locked is represented by a HashMap, 
	 * containing "locking attributes" of the field, such as timeStamp and userName. 
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
	 * This checks for the MAX locking level of any highlighted fields and 
	 * their children. 
	 * Used (for example) by "Load Defaults" and "Clear Fields" actions, which
	 * apply to all children of highlighted fields, and should be disabled
	 * if any highlighted fields (or their children) are fully locked. 
	 * 
	 * @return  the max "lockLevel" if any highlighted fields or children 
 	 * 			are locked, or null if none are locked.
	 */
	public String getMaxHighlightedChildLockingLevel();
	
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
	 * This checks to see if any field marked as 
	 * "Required" (DataFieldConstants.REQUIRED_FIELD = 'true')
	 * is also not filled out (ie dataField.isFieldFilled() is false). 
	 * Used for ensuring that "required" fields are not left blank when the form is saved.
	 * 
	 * @return	True if any required field is not filled out. 
	 */
	public boolean isAnyRequiredFieldEmpty();
	
	/**
	 * This checks to see if any field that has a default value, also has a value that 
	 * would be over-written if defaults were loaded. 
	 * Used to give users a warning that loading defaults (whole tree) would over-write stuff. 
	 * 
	 * @return	True if any field with a default value is not cleared.
	 */
	public boolean isAnyDefaultFieldFilled();
	
	/**
	 * This checks to see if any highlighted field that has a default value, also has a value that 
	 * would be over-written if defaults were loaded. 
	 * Used to give users a warning that loading defaults (highlighted fields) would over-write stuff. 
	 * 
	 * @return	True if any highlighted field with a default value is not empty.
	 */
	public boolean isAnyHighlightedDefaultFieldFilled();
	
	/**
	 * keep a note of the file that corresponds to this tree
	 */ 
	public void setFile (File file);
	
	/**
	 * Get the file that corresponds to this tree.
	 * @return		The file that this tree is read from/saved to.
	 */
	public File getFile ();
	
	/**
	 * This sets the Editor version attribute of the root node of the tree. 
	 * This can be used before saving the file to XML, so that the root element of the 
	 * XML file will have an up-to-date version number.
	 * 
	 * @param versionNumber	 	A string that represents the current version of the software. eg "3.0-3.1.2".
	 */
	public void setVersionNumber(String versionNumber);
	
	/**
	 * This returns the Editor version attribute from the root node of the tree. 
	 * This can be used to check that the file (tree) is not more recent that
	 * the current version of the software (meaning users should get a more recent version). 
	 * @return
	 */
	public String getVersionNumber();
	
	/** 
	 * when the data structure changes, edited = true. 
	 * When saved, edited = false
	 */
	public void setTreeEdited(boolean edited);
	
	/**
	 * Used (for example) for seeing whether you need to save before closing. 
	 */
	public boolean isTreeEdited();
	
	/**
	 * For UI - Undo button
	 * 
	 * @return	The name of the Undo Command
	 */
	public String getUndoCommand();
	
	/**
	 * For UI - Redo button
	 * 
	 * @return	The name of the Redo Command
	 */
	public String getRedoCommand();
	
	/**
	 * Can you undo a previous command?
	 * 
	 * @return	True if undo is possible.
	 */
	public boolean canUndo();
	
	/**
	 * Can you redo a previous command?
	 * 
	 * @return	True if redo is possible.
	 */
	public boolean canRedo();

}
