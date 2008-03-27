
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.swing.undo.AbstractUndoableEdit;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;


public class EditLockFields extends AbstractUndoableEdit {
	
	Iterator<DataFieldNode> iterator;
	ArrayList<EditDataFieldAttribute> lockedFields;
	
	public EditLockFields (DataFieldNode rootNode) {
		
		lockedFields = new ArrayList<EditDataFieldAttribute>();

		populateEditedFields(rootNode);
		
		redo();		// this locks the fields in lockFields
	}
	
	
	public EditLockFields (ArrayList<DataFieldNode> rootNodes) {
		
		lockedFields = new ArrayList<EditDataFieldAttribute>();

		for (DataFieldNode rootNode: rootNodes) {
			populateEditedFields(rootNode);
		}
		
		redo();		// this locks the fields in lockFields list
	}
	
	
	public void populateEditedFields(DataFieldNode node) {
		
		DataField field = node.getDataField();
		
		String oldValue = field.getAttribute(DataFieldConstants.FIELD_LOCKED_UTC); // null if field is not locked
		
		Calendar now = new GregorianCalendar();
		String newValue = now.getTimeInMillis() + "";
		
		lockedFields.add(new EditDataFieldAttribute(field, DataFieldConstants.FIELD_LOCKED_UTC, oldValue, newValue));	// keep a reference to fields that have been edited
	}
	
	
	public void undo() {
		for (EditDataFieldAttribute field: lockedFields) {
			field.undoNoHighlight();
			// children of this field are now locked. Need to update
			field.getDataField().notifyObserversOfChildFields();
		}
	}
	
	public void redo() {
		for (EditDataFieldAttribute field: lockedFields) {
			field.redoNoHighlight();
			// children of this field are now locked. Need to update
			field.getDataField().notifyObserversOfChildFields();
		}
	}
	
	public String getPresentationName() {
		return "Lock Fields";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}
}
