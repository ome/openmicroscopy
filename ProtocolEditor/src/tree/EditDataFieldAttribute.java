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

import javax.swing.undo.AbstractUndoableEdit;

public class EditDataFieldAttribute extends AbstractUndoableEdit {
	
	private DataField dataField;
	private String attribute;
	private String oldValue;
	private String newValue;
	
	public EditDataFieldAttribute(DataField dataField, String attribute, String oldValue, String newValue) {
		this.dataField = dataField;
		this.attribute = attribute;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public void undo() {
		dataField.setAttribute(attribute, oldValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
		dataField.formFieldClicked(true);	// highlight this field
	}
	
	public void redo() {
		dataField.setAttribute(attribute, newValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
		dataField.formFieldClicked(true);	// highlight this field
	}
	
	public void undoNoHighlight() {
		dataField.setAttribute(attribute, oldValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	public void redoNoHighlight() {
		dataField.setAttribute(attribute, newValue, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	// used to highlight a range of fields, when this is the first field in a range
	public void selectField() {
		dataField.formFieldClicked(false);
	}
	
	public String getPresentationName() {
		return "Edit " + attribute;
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}
}
