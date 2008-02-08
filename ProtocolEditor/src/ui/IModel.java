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

package ui;

import java.io.File;
import java.util.List;

import tree.DataFieldNode;
import tree.Tree.Actions;

/**
 * Methods implemented by <code>Controller</code> that are mostly delegated to model. 
 * @author will
 *
 */

public interface IModel {
	
	public void openBlankProtocolFile();
	
	public void openThisFile(File file);
	
	public File getCurrentFile();
	
	public void saveTreeToXmlFile(File file);

	public String[] getOpenFileList();
	
	public DataFieldNode getRootNode();
	
	public List<DataFieldNode> getHighlightedFields();
	
	public void editCurrentTree(Actions newAction);
	
	public void multiplyValueOfSelectedFields(float factor);
	
	// undo/redo
	public boolean canUndo();
	public boolean canRedo();
	public String getUndoCommand();
	public String getRedoCommand();
	
	public void closeCurrentFile();
	
	public boolean isCurrentFileEdited();
}
