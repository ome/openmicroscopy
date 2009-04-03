
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
import java.util.Iterator;

import javax.swing.undo.AbstractUndoableEdit;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;

/**
 * This action turns on/off the Required Field attribute = true/false, for the highlighted fields 
 * (not to their children). 
 * @see DataFieldConstants.REQUIRED_FIELD
 * 
 * @author will
 *
 */
public class EditRequiredField extends AbstractUndoableEdit {
	
	Iterator<DataFieldNode> iterator;
	ArrayList<EditDataFieldAttribute> editedFields;
	
	public EditRequiredField (DataFieldNode rootNode) {
		
		editedFields = new ArrayList<EditDataFieldAttribute>();

		populateEditedFields(rootNode);
		
		redo();	// needed to make change
	}
	
	
	public EditRequiredField (ArrayList<DataFieldNode> rootNodes) {
		
		editedFields = new ArrayList<EditDataFieldAttribute>();

		for (DataFieldNode rootNode: rootNodes) {
			populateEditedFields(rootNode);
		}
		
		redo(); // needed to make change
	}
	
	
	public void populateEditedFields(DataFieldNode rootNode) {
		
		DataField field = rootNode.getDataField();
		
		boolean isRequiredField = field.isAttributeTrue(DataFieldConstants.REQUIRED_FIELD);
		
		/*
		 * Don't want to save a value of "false". So, if not true, set attribute to null. 
		 */
		String oldValue = (isRequiredField ? "true" : null);
		String newValue = (isRequiredField ? null : "true");
		/*
		 * Add to the list of edited fields, for undo & redo. 
		 */
		editedFields.add(new EditDataFieldAttribute(field, DataFieldConstants.REQUIRED_FIELD, oldValue, newValue));
		
	}
	
	
	public void undo() {
		for (EditDataFieldAttribute field: editedFields) {
			field.undoNoHighlight();
		}
	}
	
	public void redo() {
		for (EditDataFieldAttribute field: editedFields) {
			field.redoNoHighlight();
		}
	}
	
	public String getPresentationName() {
		return "Set Required Fields";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
