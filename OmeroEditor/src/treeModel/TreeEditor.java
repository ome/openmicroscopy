 /*
 * editorDynamicTree.TreeEditActions 
 *
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
 */
package treeModel;

import javax.swing.JTree;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

import treeModel.editActions.DeleteFields;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class TreeEditor {

	protected JTree tree;
	
	protected DefaultTreeModel treeModel;
	
	UndoManager undoManager;         // history list
	UndoableEditSupport undoSupport; // event support
	
	public TreeEditor(JTree tree) {
		
		this.tree = tree;
		treeModel = (DefaultTreeModel)tree.getModel();
		
		// initialize the undo.redo system
	      undoManager = new UndoManager();
	      undoSupport = new UndoableEditSupport();
	      undoSupport.addUndoableEditListener(new UndoAdapter());
	}
	
	 /** Remove the currently selected nodes. */
    public void deleteSelectedFields() {
    	UndoableEdit edit = new DeleteFields(treeModel, tree.getSelectionPaths());
		undoSupport.postEdit(edit);
    }
    
    
    
    
    public void undo() {
    	if (undoManager.canUndo())
			undoManager.undo();
    }
    
    public void redo() {
    	if (undoManager.canRedo())
			undoManager.redo();
    }
    
    /**
	 * For UI - Undo button
	 * 
	 * @return	The name of the Undo Command
	 */
	public String getUndoCommand() {
		if (undoManager.canUndo()) {
			return undoManager.getUndoPresentationName();
		}
		else 
			return "Cannot Undo";
	}
	
	/**
	 * For UI - Redo button
	 * 
	 * @return	The name of the Redo Command
	 */
	public String getRedoCommand() {
		if (undoManager.canRedo()) {
			return undoManager.getRedoPresentationName();
		}
		else 
			return "Cannot Redo";
	}
	
	/**
	 * Can you undo a previous command?
	 * 
	 * @return	True if undo is possible.
	 */
	public boolean canUndo() {
		return undoManager.canUndo();
	}
	
	/**
	 * Can you redo a previous command?
	 * 
	 * @return	True if redo is possible.
	 */
	public boolean canRedo() {
		return undoManager.canRedo();
	}
	
	/**
	  * An undo/redo adpater. The adpater is notified when
	  * an undo edit occur(e.g. add or remove from the list)
	  * The adptor extract the edit from the event, add it
	  * to the UndoManager, and refresh the GUI
	  * http://www.javaworld.com/javaworld/jw-06-1998/jw-06-undoredo.html
	  */
	private class UndoAdapter implements UndoableEditListener {
	     public void undoableEditHappened (UndoableEditEvent evt) {
	     	UndoableEdit edit = evt.getEdit();
	     	undoManager.addEdit( edit );
	     	//refreshUndoRedo();
	     }
	  }
}
