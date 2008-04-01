
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

package tree;

public interface ITreeModel {
	
	/**
	 * Creates a new root node. 
	 * This will effectively delete the existing tree. 
	 * Removes all references to existing fields (clears highlighted fields). 
	 * This action is not added to undo/redo.
	 * ITreeModel interface. 
	 * 
	 * @param node
	 */
	public void setRootNode(DataFieldNode node);
	
	
	/**
	 * Gets a reference to the root node of the tree. 
	 * 
	 * @return		The root node of the tree. 
	 */
	public DataFieldNode getRootNode();

}
