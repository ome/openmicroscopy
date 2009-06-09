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
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoableEditSupport;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.FileAnnotationLoader;
import org.openmicroscopy.shoola.agents.editor.browser.actions.AddExpInfoAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.AddFieldAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.AddTextBoxFieldAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.ClearValuesAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.CopyFieldsAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.DeleteFieldsAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.IndentLeftAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.IndentRightAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.MoveDownAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.MoveUpAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.PasteFieldsAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.RedoEditAction;
import org.openmicroscopy.shoola.agents.editor.browser.actions.UndoEditAction;
import org.openmicroscopy.shoola.agents.editor.browser.undo.ObservableUndoManager;
import org.openmicroscopy.shoola.agents.editor.browser.undo.UndoRedoListener;
import org.openmicroscopy.shoola.agents.editor.model.DataReference;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.Note;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddDataRefEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddExpInfoEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddFieldTableEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddParamEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AddStepNoteEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AttributeEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.AttributesEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.ChangeParamEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.FieldContentEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.FieldSplitEdit;
import org.openmicroscopy.shoola.agents.editor.model.undoableEdits.RemoveExpInfo;
import org.openmicroscopy.shoola.agents.editor.preview.AnnotationHandler;

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
	implements ChangeListener,
	UndoableEditListener,
	UndoRedoListener
{
	/**
	 * A reference to the editing mode/state of the Browser. 
	 * 
	 */
	private int 					editingView;
	
	public static final int 		TREE_VIEW = 0;
	
	public static final int 		TEXT_VIEW = 1;
	
	
	/** Identifies the <code>Undo</code> action. */
	static final Integer    UNDO_ACTION = Integer.valueOf(1);
	
	/** Identifies the <code>Redo</code> action. */
	static final Integer    REDO_ACTION = Integer.valueOf(2);
	
	/** Identifies the <code>Add Field</code> action. */
	static final Integer    ADD_FIELD_ACTION = Integer.valueOf(3);
	
	/** Identifies the <code>Add Field</code> action. */
	static final Integer    DELETE_FIELD_ACTION = Integer.valueOf(4);
	
	/** Identifies the <code>Indent Right</code> action. */
	static final Integer    INDENT_RIGHT_ACTION = Integer.valueOf(5);
	
	/** Identifies the <code>Indent Left</code> action. */
	static final Integer    INDENT_LEFT_ACTION = Integer.valueOf(6);
	
	/** Identifies the <code>Move Up</code> action. */
	static final Integer    MOVE_UP_ACTION = Integer.valueOf(7);
	
	/** Identifies the <code>Move Down</code> action. */
	static final Integer    MOVE_DOWN_ACTION = Integer.valueOf(8);
	
	/** Identifies the <code>Copy Fields</code> action. */
	static final Integer    COPY_FIELDS_ACTION = Integer.valueOf(9);
	
	/** Identifies the <code>Paste Fields</code> action. */
	static final Integer    PASTE_FIELDS_ACTION = Integer.valueOf(10);
	
	/** Identifies the <code>Add Text-Box Field</code> action. */
	static final Integer    ADD_TEXTBOX_FIELD_ACTION = Integer.valueOf(11);
	
	/** Identifies the <code>Add Exp-Info</code> action. */
	static final Integer    ADD_EXP_INFO_ACTION = Integer.valueOf(12);
	
	/** Identifies the <code>Clear Values</code> action. */
	static final Integer    CLEAR_VALUES_ACTION = Integer.valueOf(13);
	
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
	
	/**
	 * This is called when an edit is made to the current file.
	 */
	private void fileEdited() 
	{
		model.setEdited(true);
	}
    
    /** Helper method to create all the UI actions. */
    private void createActions()
    {
       // actionsMap.put(EDIT, new EditAction(model));
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
       actionsMap.put(MOVE_UP_ACTION, new MoveUpAction(undoSupport, model));
       actionsMap.put(MOVE_DOWN_ACTION, new MoveDownAction(undoSupport, model));
       actionsMap.put(COPY_FIELDS_ACTION, new CopyFieldsAction(model));
       actionsMap.put(PASTE_FIELDS_ACTION, new PasteFieldsAction
    		   											(undoSupport, model));
       actionsMap.put(ADD_TEXTBOX_FIELD_ACTION, new AddTextBoxFieldAction
														(undoSupport, model));
       
       actionsMap.put(ADD_EXP_INFO_ACTION, new AddExpInfoAction
														(undoSupport, model));
       actionsMap.put(CLEAR_VALUES_ACTION, new ClearValuesAction
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
	      ((ObservableUndoManager)undoManager).addUndoRedoListener(this);
	      undoSupport = new UndoableEditSupport();
	      undoSupport.addUndoableEditListener(new UndoAdapter());
	      undoSupport.addUndoableEditListener(this);
	      
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
     * Allows classes to change the view mode, which may change the UI in
     * several places? 
     * E.g.	{@link #TREE_VIEW} or {@link #TEXT_VIEW}.
     * 
     * @param viewMode		The new view mode. 
     */
    void setViewingMode(int viewMode) 
    {
    	switch (viewMode) {
		case TREE_VIEW:
			editingView = TREE_VIEW;
			break;
		case TEXT_VIEW:
			editingView = TEXT_VIEW;
			break;
		default:
			break;
		}
    	stateChanged(new ChangeEvent(this));
    }
    
    /**
     * Allows UI classes to determine the current editing mode. 
     * E.g.	{@link #TREE_VIEW} or {@link #TEXT_VIEW}.
     * 
     * @return		The current viewing mode {@link #editingView}
     */
    int getViewingMode() { return editingView; }
    

	/**
	 * Delegates to the {@link Browser} to set lock to prevent file editing.
	 * @param locked
	 */
	void setFileLocked(boolean locked) {
		model.setFileLocked(locked);
	}
	
	/**
	 * Delegates to the {@link Browser} to determine whether file is locked.
	 * @return	true if file is locked. 
	 */
	boolean isFileLocked() 
	{
		return model.isFileLocked();
	}
	
	/**
	 * Sets the Editing mode of the Browser. Either
	 * {@link #EDIT_EXPERIMENT} or {@link #EDIT_PROTOCOL}
	 * @param editingMode	see above
	 */
	void setEditingMode(int editingMode) 
	{
		model.setEditingMode(editingMode);
	}
	
	/**
	 * Gets the Editing mode of the Browser. Either
	 * {@link #EDIT_EXPERIMENT} or {@link #EDIT_PROTOCOL}
	 * @return		see above. 
	 */
	int getEditingMode() 
	{
		return model.getEditingMode();
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
     * Allows UI components (that don't have access to the model) to determine
     * whether we are currently editing a Protocol or Experiment.
     * 
     * @return	True if we are editing an experiment. 
     */
    public boolean isModelExperiment() 
    { 
    	return model.isModelExperiment();
    }
    
    /**
     * Adds Experimental-Info to a Protocol, to create an Experiment. 
     * 
     * @param treeUI	The JTree source of TreeModel to edit. 
     */
    public void addExperimentalInfo(JTree treeUI)
    {
    	AddExpInfoEdit edit = new AddExpInfoEdit(treeUI);
		edit.doEdit();
		
		undoSupport.postEdit(edit);
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
	 * Edits a field by creating and adding a Data reference to a field/step
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void addDataRefToField(IField field, JTree tree, TreeNode node) {
		UndoableEdit edit = new AddDataRefEdit(field, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Edits a field by DELETING the specified data reference
	 * 
	 * @param field			The field to add a new parameter to.
	 * @param dataRef		The data reference to delete
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node			The node to highlight / refresh with undo/redo. 
	 */
	public void addDataRefToField(IField field, DataReference dataRef,
												JTree tree, TreeNode node) {
		UndoableEdit edit = new AddDataRefEdit(field, dataRef, tree, node);
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
	 * Adds a Step note to the specified field(step), and adds edit to undo/redo
	 * 
	 * @param field		The field/step to add a note to 
	 * @param tree		The JTree that the field is in
	 * @param node		The node that holds the step .
	 */
	public void addStepNote(IField field, JTree tree, TreeNode node)
	{
		UndoableEdit edit = new AddStepNoteEdit(field, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Deletes a Step note from the specified field(step), 
	 * and adds edit to undo/redo
	 * 
	 * @param field		The field/step to add a note to 
	 * @param tree		The JTree that the field is in
	 * @param node		The node that holds the step .
	 * @param index		The index of the note to delete
	 */
	public void deleteStepNote(IField field, JTree tree, 
													TreeNode node, Note note)
	{
		UndoableEdit edit = new AddStepNoteEdit(field, note, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Deletes the Experiment Info from the model held by JTree. 
	 * This includes Step Notes. 
	 * 
	 * @param tree
	 */
	public void deleteExperimentInfo(JTree tree) 
	{
		UndoableEdit edit = new RemoveExpInfo(tree);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Deletes the Experiment Info from the tree model. 
	 * Will Also remove Step Notes. 
	 * 
	 * @param tree
	 */
	public void deleteExperimentInfo(TreeModel treeModel) 
	{
		UndoableEdit edit = new RemoveExpInfo(treeModel);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Split a field into 2, adding content to the 2 daughters. New daughter
	 * is added as a sibling after the parent. 
	 * 
	 * @param field			The field to split.
	 * @param name			The new name of the split field.
	 * @param content1		The new content of the split field.
	 * @param content2		The new content of the new daughter
	 * @param tree			The JTree in which field is.
	 * @param node			The node containing the split field. 
	 */
	public void splitField(IField field, String name,
			List<IFieldContent> content1, List<IFieldContent> content2,
			JTree tree, TreeNode node) {
		
		UndoableEdit edit = new FieldSplitEdit(field, name, 
				content1, content2, tree, node);
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
	 * Edits a field by changing the content (not name). 
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param content 		The new content, as a list.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void editFieldContent(IField field,List<IFieldContent> content, 
			JTree tree, TreeNode node) {
		
		UndoableEdit edit = new FieldContentEdit(field, content, tree, node);
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
	 * Edits a field by changing one parameter for another.
	 * If param is null, this simply removes the param at defined index. 
	 * 
	 * @param field		The field to add a new parameter to.
	 * @param param		The new parameter to add. 
	 * @param index		The index of the parameters to swap. 
	 * @param tree		The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void changeParam(IField field, IParam param, int index,
			JTree tree, TreeNode node) {
		
		UndoableEdit edit = new ChangeParamEdit(param, field, index, 
			 tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Edits a field by changing one parameter for another.
	 * If newParam is null, this simply removes the oldParam. 
	 * 
	 * @param newParam	The new parameter
	 * @param oldParam 	The old parameter to replace.
	 * @param field		The field to add a new parameter to.
	 * @param tree		The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void changeParam(IParam newParam, IParam oldParam, IField field,
			JTree tree, TreeNode node) {
		
		UndoableEdit edit = new ChangeParamEdit(newParam, oldParam, field, 
			 tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Adds a new tableModel to the field, allowing multiple values to be
	 * set for each parameter. 
	 * 
	 * @param field		The field to add a new tableModel to.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void addFieldTable(IField field, JTree tree, TreeNode node)
	{
		UndoableEdit edit = new AddFieldTableEdit(field, tree, node);
		undoSupport.postEdit(edit);
	}
	
	/**
	 * Removes tableModel from the field, but doesn't delete the data 
	 * held by parameters. 
	 * 
	 * @param field		The field to add a new tableModel to.
	 * @param tree			The JTree to refresh with undo/redo
	 * @param node		The node to highlight / refresh with undo/redo. 
	 */
	public void removeFieldTable(IField field, JTree tree, TreeNode node)
	{
		UndoableEdit edit = new AddFieldTableEdit(field, tree, node, null);
		undoSupport.postEdit(edit);
	}

	/**
	 * Creates a {@link FileAnnotationLoader} and calls load. 
	 * 
	 * @param fileID  The fileID you want the annotation for. 
	 * @param handler The handler used when the annotation is returned
	 */
	public void getFileAnnotation(long fileID, AnnotationHandler handler)
	{
		if (handler == null) return;
		FileAnnotationLoader fal = new FileAnnotationLoader(model, fileID);
		fal.setAnnotationHandler(handler);
		fal.load();
	}

	/**
	 * Implemented as specified by the {@link UndoableEditListener} interface.
	 * Listens to the {@link #undoSupport} for edits, and calls 
	 * {@link #fileEdited()}
	 * 
	 * @see UndoableEditListener#undoableEditHappened(UndoableEditEvent)
	 */
	public void undoableEditHappened(UndoableEditEvent e) {
		fileEdited();
	}

	/**
	 * Implemented as specified by the {@link UndoRedoListener} interface.
	 * Listens to the {@link #undoManager} for undo & redo events, and calls 
	 * {@link #fileEdited()}
	 * 
	 * @see UndoRedoListener#undoRedoPerformed()
	 */
	public void undoRedoPerformed() {
		fileEdited();
	}
	
}
