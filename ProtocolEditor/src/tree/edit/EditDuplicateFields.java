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

import javax.swing.undo.AbstractUndoableEdit;

import tree.DataFieldNode;
import tree.Tree;

public class EditDuplicateFields extends AbstractUndoableEdit {
	
	ArrayList<DataFieldNode> addedFields;
	DataFieldNode parentNode;
	int indexOfFirstHighlightedField;
	
	public EditDuplicateFields (ArrayList<DataFieldNode> addTheseFields) {
		
		addedFields = new ArrayList<DataFieldNode>(addTheseFields);
		
		DataFieldNode firstNode = addedFields.get(0);
		parentNode = firstNode.getParentNode();
		indexOfFirstHighlightedField = firstNode.getMyIndexWithinSiblings();
		
	}
	
	public void undo() {
		Tree.deleteDataFields(addedFields);
	}
	public void redo() {
		Tree.insertTheseDataFields(addedFields, parentNode, indexOfFirstHighlightedField);
	}
	
	public String getPresentationName() {
		return "Duplicate Fields";
	}

	public boolean canUndo() {
		return true;
	}

	public boolean canRedo() {
		return true;
	}

}
