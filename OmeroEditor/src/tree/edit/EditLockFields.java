
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
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;


public class EditLockFields extends AbstractUndoableEdit {
	
	Iterator<DataFieldNode> iterator;
	ArrayList<EditDataFieldAttributes> lockedFields;
	
	/**
	 * A map of attributes that define the lock to be placed on these fields.
	 * eg. User Name, UTC-timeStamp, password, locking "level" (lock-all vv lock-template);
	 */
	Map<String, String> newLockingAttributes;
	
	
	public EditLockFields (DataFieldNode rootNode, Map<String, String> lockingAttributes) {
		
		lockedFields = new ArrayList<EditDataFieldAttributes>();
		this.newLockingAttributes = lockingAttributes;
		
		populateEditedFields(rootNode);
	}
	
	public EditLockFields (ArrayList<DataFieldNode> rootNodes, Map<String, String> lockingAttributes) {
		
		lockedFields = new ArrayList<EditDataFieldAttributes>();
		this.newLockingAttributes = lockingAttributes;

		for (DataFieldNode rootNode: rootNodes) {
			populateEditedFields(rootNode);
		}
	}
	
	
	public void populateEditedFields(DataFieldNode node) {
		
		DataField field = node.getDataField();
		
		/*
		 * Set the new values of the lock. And remember the old values...
		 */
		Map<String, String> oldValues = field.setAttributes("Lock", newLockingAttributes, false);
		field.notifyObserversOfChildFields();	// refresh locked status of field & children
		
		/*
		 * Add to the list of locked fields, for undo & redo. 
		 */
		lockedFields.add(new EditDataFieldAttributes(field, "Lock", oldValues, newLockingAttributes));	// keep a reference to fields that have been edited
	}
	
	
	public void undo() {
		for (EditDataFieldAttributes field: lockedFields) {
			field.undoNoHighlight();
			// children of this field are now locked. Need to update
			field.getDataField().notifyObserversOfChildFields();
		}
	}
	
	public void redo() {
		for (EditDataFieldAttributes field: lockedFields) {
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
