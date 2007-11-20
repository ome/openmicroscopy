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

public class EditAddField extends AbstractUndoableEdit {
	
	DataFieldNode newNode;
	DataFieldNode parentNode;
	int index;
	
	public EditAddField(DataFieldNode newNode) {
		
		this.newNode = newNode;
		parentNode = newNode.getParentNode();
		
	}
	
	public void undo() {
		//need ref to new field (will have been added after last highlighted field)
		index = newNode.getMyIndexWithinSiblings();
		System.out.println("TreeAction.ADD_NEW_FIELD indexToRemove = " + index);
		parentNode.removeChild(index);
	}
	
	public void redo() {
		Tree.addDataField(newNode, parentNode, index);
	}
	

	public String getPresentationName() {
		return "Add Field";
	}

	  public boolean canUndo() {
	         return true;
	  }

	  public boolean canRedo() {
	         return true;
	  }
}
