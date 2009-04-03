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

package tree.edit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.undo.AbstractUndoableEdit;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;

public class EditClearFields extends AbstractUndoableEdit {
	
	Iterator<DataFieldNode> iterator;
	ArrayList<EditDataFieldAttributes> editedFields;
	
	public EditClearFields (DataFieldNode rootNode) {
		
		editedFields = new ArrayList<EditDataFieldAttributes>();

		populateEditedFields(rootNode);
	}
	
	
	public EditClearFields (ArrayList<DataFieldNode> rootNodes) {
		
		editedFields = new ArrayList<EditDataFieldAttributes>();

		for (DataFieldNode rootNode: rootNodes) {
			populateEditedFields(rootNode);
		}
	}
	
	
	public void populateEditedFields(DataFieldNode rootNode) {
		
		iterator = rootNode.iterator();
		
		while (iterator.hasNext()) {
			DataField field = (DataField)iterator.next().getDataField();
			
			String[] valueAttributes = field.getValueAttributes();
			HashMap<String, String> nullValues = new HashMap<String, String>();
			for(int i=0; i<valueAttributes.length; i++) {
				nullValues.put(valueAttributes[i], null);
			}
			
			/*
			 * Overwrite all the value attributes for this field.
			 * Save the oldValues for the undo queue
			 */
			Map<String, String> oldValues = field.setAttributes("Clear", nullValues, false);
			field.notifyDataFieldObservers();
			
			/*
			 * Add to the list of locked fields, for undo & redo. 
			 */
			editedFields.add(new EditDataFieldAttributes(field, "Clear", oldValues, nullValues));
		}
	}
	
	
	public void undo() {
		for (EditDataFieldAttributes field: editedFields) {
			field.undoNoHighlight();
		}
	}
	
	public void redo() {
		for (EditDataFieldAttributes field: editedFields) {
			field.redoNoHighlight();
		}
	}
	
	public String getPresentationName() {
		return "Clear Fields";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
