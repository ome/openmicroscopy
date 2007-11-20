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

import java.util.LinkedHashMap;

import javax.swing.undo.AbstractUndoableEdit;

public class EditDataFieldType extends AbstractUndoableEdit {
	
	private DataField dataField;
	private LinkedHashMap<String, String> oldAttributes;
	private LinkedHashMap<String, String> newAttributes;

	
	public EditDataFieldType(DataField dataField, LinkedHashMap<String, String> allAttributes) {
		this.dataField = dataField;
		this.oldAttributes = new LinkedHashMap<String, String>(allAttributes);
	}
	
	public void undo() {
		// first, remember the new attributes to you can redo
		newAttributes = new LinkedHashMap<String, String>(dataField.getAllAttributes());
		
		dataField.setAllAttributes(oldAttributes);
		// set fieldEditor and formField to null, so that new ones are created for the new inputType
		dataField.resetFieldEditorFormField();
		
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();	
	}
	
	public void redo() {
		dataField.setAllAttributes(newAttributes);
		// set fieldEditor and formField to null, so that new ones are created for the new inputType
		dataField.resetFieldEditorFormField();	
		
		// need to display changes without adding this change to the undoActions history in Tree
		dataField.notifyDataFieldObservers();
	}
	
	public String getPresentationName() {
		return "Change Field Type";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}
}
