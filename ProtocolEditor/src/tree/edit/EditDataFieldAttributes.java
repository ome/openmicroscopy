
/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package tree.edit;

import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;

import tree.DataField;


public class EditDataFieldAttributes extends AbstractUndoableEdit {
	
	private DataField dataField;
	private String title;
	private Map oldValues;
	private Map newValues;
	
	public EditDataFieldAttributes(DataField dataField, String title, Map oldValues, Map newValues) {
		this.dataField = dataField;
		this.title = title;
		this.oldValues = oldValues;
		this.oldValues = oldValues;
	}
	
	public void undo() {
		dataField.setAttributes(title, oldValues, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
		dataField.dataFieldSelected(true);	// highlight this field
	}
	
	public void redo() {
		dataField.setAttributes(title, newValues, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
		dataField.dataFieldSelected(true);	// highlight this field
	}
	
	public void undoNoHighlight() {
		dataField.setAttributes(title, oldValues, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	public void redoNoHighlight() {
		dataField.setAttributes(title, newValues, false);
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	// used to highlight a range of fields, when this is the first field in a range
	public void selectField() {
		dataField.dataFieldSelected(false);
	}
	
	// used for classes to get a reference to the dataField
	// eg. EditLockFields needs to call dataField.notifyObserversOfChildFields()
	public DataField getDataField() {
		return dataField;
	}
	
	public String getPresentationName() {
		return "Edit " + title;
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}
}
