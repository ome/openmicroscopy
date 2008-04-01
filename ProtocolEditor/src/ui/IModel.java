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
import java.util.List;

import tree.DataField;
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
	 * This method tests to see if any of the currently highlighted fields 
	 * are locked. 
	 * If so, various editing actions are disabled. 
	 * 
	 * @return	true if any of the fields are locked. 
	 * Fields are considered locked if their ancestors are locked.
	 */
	public boolean areHighlightedFieldsLocked();
	
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
	
	public void saveTreeToXmlFile(File file);

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
}
