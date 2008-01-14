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

import javax.swing.undo.AbstractUndoableEdit;

import ui.FormFieldNumber;

public class EditMultiplyValues extends AbstractUndoableEdit {
	
	ArrayList<EditDataFieldAttribute> editedFields;
	
	
	public EditMultiplyValues(ArrayList<DataFieldNode> highlightedFields, float factor) {
		
		editedFields = new ArrayList<EditDataFieldAttribute>();
		
		for (DataFieldNode node: highlightedFields) {
			DataField dataField = node.getDataField();
			String oldValue = dataField.getAttribute(DataFieldConstants.VALUE);
			
			try {
				FormFieldNumber formFieldNumber = (FormFieldNumber)dataField.getFormField();
				formFieldNumber.multiplyCurrentValue(factor);
				
				String newValue = dataField.getAttribute(DataFieldConstants.VALUE);
				editedFields.add(new EditDataFieldAttribute(dataField, DataFieldConstants.VALUE, oldValue, newValue));	// keep a reference to fields that have been edited
			} catch (Exception ex) {
				// cast failed: formField is not a Number field
			}
		}
		
	}
	
	public void undo() {
		for (EditDataFieldAttribute field: editedFields) {
			field.undo();
		}
		// will select the first field, highlighting the whole range (last is already highlighted)
		editedFields.get(0).selectField();
	}
	
	public void redo() {
		for (EditDataFieldAttribute field: editedFields) {
			field.redo();
		}
		editedFields.get(0).selectField();
	}

	public String getPresentationName() {
		return "Multiply Values";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
