 /*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserControl 
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
package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.tree.TreeNode;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.actions.AddFieldAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.DeleteFieldsAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.EditAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.IndentLeftAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.IndentRightAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.RedoEditAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.UndoEditAction;
import org.openmicroscopy.shoola.agents.editor.browser.undo.ObservableUndoManager;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddParamEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AttributeEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AttributesEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.ChangeParamEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.FieldContentEdit;



/** 
 *	The Controller in the Browser MVC. 
 *	Also manages undo/redo queue.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BrowserControl 
	implements ChangeListener
{

	/** Identifies the <code>Edit</code> action. */
	static final Integer    EDIT = new Integer(0);
	
	/** Identifies the <code>Undo</code> action. */
	static final Integer    UNDO_ACTION = new Integer(1);
	
	/** Identifies the <code>Redo</code> action. */
	static final Integer    REDO_ACTION = new Integer(2);
	
	/** Identifies the <code>Add Field</code> action. */
	static final Integer    ADD_FIELD_ACTION = new Integer(3);
	
	/** Identifies the <code>Add Field</code> action. */
	static final Integer    DELETE_FIELD_ACTION = new Integer(4);
	
	/** Identifies the <code>Indent Right</code> action. */
	static final Integer    INDENT_RIGHT_ACTION = new Integer(5);
	
	/** Identifies the <code>Indent Left</code> action. */
	static final Integer    INDENT_LEFT_ACTION = new Integer(6);
	
	/** 
     * Reference to the {@link Browser} component, which, in this context,
     * is regarded as the Model.
     */
    private Browser     				model;
    
    /** 
     * Reference to the View.
     */
    private BrowserUI   				view;
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, Action>		actionsMap;
    
    /**
     * An undo manager to handle undo/redo queue.
     */
    private UndoManager 				undoManager;
	
    /**
     * Support for the undo/redo.
     */
	private UndoableEditSupport 		undoSupport;
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
       actionsMap.put(EDIT, new EditAction(model));
       actionsMap.put(UNDO_ACTION, new UndoEditAction(
    		   undoManager, undoSupport, model));
       actionsMap.put(REDO_ACTION, new RedoEditAction(
    		   undoManager, undoSupport, model));
       actionsMap.put(ADD_FIELD_ACTION, new AddFieldAction(undoSupport, model));
       actionsMap.put(DELETE_FIELD_ACTION, new DeleteFieldsAction
    		   (undoSupport, model));
       actionsMap.put(INDENT_RIGHT_ACTION, new IndentRightAction
    		   (undoSupport, model));
       actionsMap.put(INDENT_LEFT_ACTION, new IndentLeftAction
    		   (undoSupport, model));
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserUI) initialize} method 
     * should be called straight after to link this Controller to the other 
     * MVC components.
     * 
     * @param model  Reference to the {@link Browser} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    BrowserControl(Browser model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        actionsMap = new HashMap<Integer, Action>();
        
     // initialize the undo.redo system
	      undoManager = new ObservableUndoManager();
	      undoSupport = new UndoableEditSupport();
	      undoSupport.addUndoableEditListener(new UndoAdapter());
	      
	      createActions();
    }
    
    /**
     * Links this Controller to its Model and its View.
     * 
     * @param view   Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(BrowserUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        this.view = view;
        model.addChangeListener(this);
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    public Action getAction(Integer id) { return actionsMap.get(id); }
	
    /**
     * Detects when the {@link Browser} is ready and then registers for
     * property change notification.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
       	//int state = model.getState();
 
    	view.onStateChanged();
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
			String displayName, JTree tree, TreeNode node) 
	{	
		UndoableEdit edit = new AttributeEdit(attributes, name, value, 
				displayName, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * This method adds an attributesEdit to the undo/redo queue and then
	 * update the JTree UI.
	 * JTree update (optional) requires that JTree and TreeNode are not null.
	 * But they are not required for editing of the data.
	 * TODO   Would be better for changes to the data to notify the TreeModel
	 * in which the data is held (without the classes modifying the data
	 * having to manually call DefaultTreeModel.nodeChanged(node);
	 * 
	 * @param attributes		The collection of attributes to edit
	 * @param displayName		A name for display on undo/redo
	 * @param newValues		The new values in an attribute map
	 * @param tree		The JTree displaying the data. This can be null
	 * @param node		The node in the JTree that holds data. Can be null. 
	 */
	public void editAttributes(IAttributes attributes, String displayName, 
			HashMap<String,String> newValues, JTree tree, TreeNode node) 
	{	
		UndoableEdit edit = new AttributesEdit(attributes, displayName, 
				newValues, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Edits a field by creating and adding a new Parameter.
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param paramType		A string defining the type of parameter to add.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void addParamToField(IField field, String paramType, 
			JTree tree, TreeNode node) {
		
		UndoableEdit edit = new AddParamEdit(field, paramType, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Edits a field by adding text content
	 * 
	 * @param text		The text to add as text content. 
	 * @param field		The field to add a new parameter to.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void addParamToField(String text, IField field, 
			JTree tree, TreeNode node) {
		
		UndoableEdit edit = new AddParamEdit(text, field, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Edits a field by changing the content, and name
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param name		The new name of the field
	 * @param content 		The new content, as a list.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void editFieldContent(IField field, String name,
			List<IFieldContent> content, JTree tree, TreeNode node) {
		
		UndoableEdit edit = new FieldContentEdit(field, name, 
				content, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	  * An undo/redo adpater. The adpater is notified when
	  * an undo edit occur(e.g. add or remove from the list)
	  * The adptor extract the edit from the event, add it
	  * to the UndoManager, and refresh the GUI
	  * http://www.javaworld.com/javaworld/jw-06-1998/jw-06-undoredo.html
	  */
	private class UndoAdapter implements UndoableEditListener 
	{
	     public void undoableEditHappened (UndoableEditEvent evt) {
	     	UndoableEdit edit = evt.getEdit();
	     	undoManager.addEdit( edit );
	     }
	  }
	
	/**
	 * Edits a field by creating and adding a new Parameter.
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param paramType		A string defining the type of parameter to add.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void changeParam(IField field, IParam param, int index,
			JTree tree, TreeNode node) {
		
		UndoableEdit edit = new ChangeParamEdit(param, field, index, 
			 tree, node);
		undoSupport.postEdit(edit);
	}
}
