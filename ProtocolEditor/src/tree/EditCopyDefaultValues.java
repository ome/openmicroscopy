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
import java.util.Iterator;

import javax.swing.undo.AbstractUndoableEdit;

public class EditCopyDefaultValues extends AbstractUndoableEdit {
	
	Iterator<DataFieldNode> iterator;
	ArrayList<EditDataFieldAttribute> editedFields;
	
	/**
	 * @param rootNode 	the root of the Tree, containing nodes to which the CopyDefault action will be applied.
	 * This tree will be iterated through, and nodes that have default values will be added to the editedFields list.
	 */
	public EditCopyDefaultValues (DataFieldNode rootNode) {
		editedFields = new ArrayList<EditDataFieldAttribute>();
		
		populateEditedFields(rootNode);
		
		redo();		// this sets value to newValue for all fields in the list
	}
	
	public EditCopyDefaultValues (ArrayList<DataFieldNode> nodes) {
		editedFields = new ArrayList<EditDataFieldAttribute>();
		
		for (DataFieldNode node: nodes) {
			populateEditedFields(node);
		}
		
		redo();		// this sets value to newValue for all fields in the list
	}
	
	private void populateEditedFields(DataFieldNode rootNode) {
		iterator = rootNode.iterator();
		
		while (iterator.hasNext()) {
			DataField field = (DataField)iterator.next().getDataField();
			String oldValue = field.getAttribute(DataFieldConstants.VALUE);	// may be null
			String newValue = field.getAttribute(DataFieldConstants.DEFAULT);
			
			if (newValue != null) {		// make a list of all fields that have a default value
				editedFields.add(new EditDataFieldAttribute(field, DataFieldConstants.VALUE, oldValue, newValue));	// keep a reference to fields that have been edited
			}
			
		}
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
		return "Load Default Values";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
