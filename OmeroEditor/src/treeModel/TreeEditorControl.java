 /*
 * treeModel.TreeEditorControl 
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

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.TreeNode;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;


import treeModel.editActions.AddFieldAction;
import treeModel.editActions.DeleteFieldsAction;
import treeModel.editActions.DuplicateFieldsAction;
import treeModel.editActions.RedoEditAction;
import treeModel.editActions.UndoEditAction;
import treeModel.fields.IAttributes;
import treeModel.undoableTreeEdits.AttributeEdit;
import treeModel.undoableTreeEdits.ObservableUndoManager;

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
public class TreeEditorControl {

	private ITreeEditor model;
	
	private TreeEditorUI view;
	
	private Map<Integer, Action> actions;
	
	UndoManager undoManager;
	
	UndoableEditSupport undoSupport;
	
	public static final Integer DELETE_FIELD_ACTION = new Integer(1);
	
	public static final Integer UNDO_ACTION = new Integer(2);
	
	public static final Integer REDO_ACTION = new Integer(3);
	
	public static final Integer ADD_FIELD_ACTION = new Integer(4);
	
	public static final Integer DUPLICATE_FIELDS_ACTION = new Integer(5);
	
	
	public void initialise(ITreeEditor model, TreeEditorUI view) {
		
		this.model = model;
		
		this.view = view;
		
		// initialize the undo.redo system
	      undoManager = new ObservableUndoManager();	// implements UndoRedoObservable
	      undoSupport = new UndoableEditSupport();
	      undoSupport.addUndoableEditListener(new UndoAdapter());
		
		createActions();
	}
	
	private void createActions() {
		
		actions = new HashMap<Integer, Action>();
		
		actions.put(ADD_FIELD_ACTION, new AddFieldAction(undoSupport));
		actions.put(DELETE_FIELD_ACTION, new DeleteFieldsAction(undoSupport));
		actions.put(UNDO_ACTION, new UndoEditAction(undoManager, undoSupport));
		actions.put(REDO_ACTION, new RedoEditAction(undoManager, undoSupport));
		actions.put(DUPLICATE_FIELDS_ACTION, new DuplicateFieldsAction(undoSupport));
		
	}
	
	public Action getAction(int actionIndex) {
		
		return actions.get(actionIndex);
	}
	
	/**
	 * This method adds an attributeEdit to the undo/redo queue and then
	 * update the JTree UI.
	 * JTree update (optional) requires that JTree and TreeNode are not null.
	 * But they are not required for editing of the data.
	 * TODO   Would be better for changes to the data to notify the TreeModel
	 * in which the data is held (without the classes modifying the data
	 * having to manually call DefaultTreeModel.nodeChanged(node);
	 * 
	 * @param attributes		The collection of attributes to edit
	 * @param name		The name of the attribute to edit
	 * @param value		The new value for the named attribute. 
	 * @param tree		The JTree displaying the data. This can be null
	 * @param node		The node in the JTree that holds data. Can be null. 
	 */
	public void editAttribute(IAttributes attributes, String name, String value,
			JTree tree, TreeNode node) {
		
		UndoableEdit edit = new AttributeEdit(attributes, name, value, tree, node);
		undoSupport.postEdit(edit);
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
