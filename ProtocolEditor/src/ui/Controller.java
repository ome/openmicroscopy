package ui;

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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import actions.AddFieldAction;
import actions.CalendarOpenAction;
import actions.CalendarRepopulateAction;
import actions.ClearFieldsAllAction;
import actions.ClearFieldsHighltdAction;
import actions.CloseFileAction;
import actions.CopyFieldAction;
import actions.DeleteFieldAction;
import actions.DemoteFieldAction;
import actions.DuplicateFieldsAction;
import actions.ExportAllTextAction;
import actions.ExportCalendarAction;
import actions.ExportHighltdTextAction;
import actions.HelpOnlineUserGuideAction;
import actions.ImportFieldsAction;
import actions.ImportTableAction;
import actions.ImportTextAction;
import actions.IndexFilesAction;
import actions.LoadDefaultsAllAction;
import actions.LoadDefaultsHighltdAction;
import actions.LockFieldsAction;
import actions.MoveFieldDownAction;
import actions.MoveFieldUpAction;
import actions.MultiplyValuesAction;
import actions.NewFileAction;
import actions.OpenFileAction;
import actions.OpenWwwFileAction;
import actions.PasteFieldAction;
import actions.ExportAllHtmlAction;
import actions.ExportHighltdHtmlAction;
import actions.PromoteFieldAction;
import actions.RedoAction;
import actions.RedoActionNoNameRefresh;
import actions.RequiredFieldAction;
import actions.SaveFileAction;
import actions.SaveFileAsAction;
import actions.UndoAction;
import actions.UndoActionNoNameRefresh;
import actions.ValidateXMLAction;

import tree.DataFieldNode;
import tree.Tree.Actions;
import xmlMVC.XMLModel;

public class Controller 
	extends AbstractComponent
	implements ChangeListener {

	protected XMLModel model;
	protected XMLView view;
	protected JFrame frame = null;
	
	/** Identifies the <code>New File action</code>. */
    static final Integer     NEW_FILE = new Integer(0);
    
	/** Identifies the <code>Open File action</code>. */
    static final Integer     OPEN_FILE = new Integer(1);
    
	/** Identifies the <code>Open File action</code>. */
    static final Integer     OPEN_WWW_FILE = new Integer(2);
    
	/** Identifies the <code>Save File action</code>. */
    static final Integer     SAVE_FILE = new Integer(3);
    
    /** Identifies the <code>Save-File-As action</code>. */
    static final Integer     SAVE_FILE_AS = new Integer(4);
    
    /** Identifies the <code>ExportAllHtml action</code>. */
    static final Integer     EXPORT_ALL_HTML = new Integer(5);
    
    /** Identifies the <code>ExportHighltdHtml action</code>. */
    static final Integer     EXPORT_HIGHLT_HTML = new Integer(6);
    
    /** Identifies the <code>Load Defaults action</code>. */
    static final Integer     LOAD_DEFAULTS_ALL = new Integer(7);
    
    /** Identifies the <code>Load Defaults Highltd action</code>. */
    static final Integer     LOAD_DEFAULTS_HIGHLT = new Integer(8);
    
    /** Identifies the <code>Clear Fields All action</code>. */
    static final Integer     CLEAR_FIELDS_ALL = new Integer(9);
    
    /** Identifies the <code>Clear Fields Highltd action</code>. */
    static final Integer     CLEAR_FIELDS_HIGHLT = new Integer(10);
    
    /** Identifies the <code>Multiply values action</code>. */
    static final Integer     MULTIPLY_VALUES = new Integer(11);
    
    /** Identifies the <code>Undo action</code>. */
    static final Integer     UNDO = new Integer(12);
    /** Identifies the <code>Undo action </code>. - Name is not shown (use for button) */
    static final Integer     UNDO_NO_NAME = new Integer(13);
    
    /** Identifies the <code>Redo action</code>. */
    static final Integer     REDO = new Integer(14);
    /** Identifies the <code>Redo action</code>. - Name is not shown (use for button) */
    static final Integer     REDO_NO_NAME = new Integer(15);
    
    /** Identifies the <code>CloseFile action</code>. */
    static final Integer     CLOSE_FILE = new Integer(16);
    
    /** Identifies the <code>AddFieldAction</code>. */
    static final Integer     ADD_FIELD = new Integer(17);
    
    /** Identifies the <code>DuplicateFieldsAction</code>. */
    static final Integer     DUPLICATE_FIELD = new Integer(18);
    
    /** Identifies the <code>DeleteFieldAction</code>. */
    static final Integer     DELETE_FIELD = new Integer(19);
    
    /** Identifies the <code>MoveFieldUpAction</code>. */
    static final Integer     MOVE_FIELD_UP = new Integer(20);
    
    /** Identifies the <code>MoveFieldDownAction</code>. */
    static final Integer     MOVE_FIELD_DOWN = new Integer(21);
    
    /** Identifies the <code>PromoteFieldAction</code>. */
    static final Integer     PROMOTE_FIELD = new Integer(22);
    
    /** Identifies the <code>DemoteFieldAction</code>. */
    static final Integer    DEMOTE_FIELD = new Integer(23);
    
    /** Identifies the <code>CopyFieldAction</code>. */
    static final Integer    COPY_FIELD = new Integer(24);
    
    /** Identifies the <code>PasteFieldAction</code>. */
    static final Integer    PASTE_FIELD = new Integer(25);
    
    /** Identifies the <code>ImportFieldsAction</code>. */
    static final Integer    IMPORT_FIELD = new Integer(26);
    
    /** Identifies the <code>IndexFilesAction</code>. */
    static final Integer    INDEX_FILES = new Integer(27);
    
    /** Identifies the <code>ExportAllHtml action</code>. */
    static final Integer     EXPORT_ALL_TEXT = new Integer(28);
    
    /** Identifies the <code>ExportHighltdHtml action</code>. */
    static final Integer     EXPORT_HIGHLTD_TEXT = new Integer(29);
    
    /** Identifies the <code>CalendarOpen action</code>. */
    static final Integer     OPEN_CALENDAR = new Integer(30);
    
    /** Identifies the <code>CalendarRepopulate action</code>. */
    static final Integer     REPOPULATE_CALENDAR = new Integer(31);
    
    /** Identifies the <code>CalendarRepopulate action</code>. */
    static final Integer     LOCK_FIELDS = new Integer(32);
    
    /** Identifies the <code>ImportTextAction</code>. */
    static final Integer     IMPORT_TEXT = new Integer(33);
    
    /** Identifies the <code>ImportTableAction</code>. */
    static final Integer     IMPORT_TABLE = new Integer(34);
    
    /** Identifies the <code>RequiredFieldAction</code>. */
    static final Integer     REQUIRED_FIELD = new Integer(35);
    
    /** Identifies the <code>ExportCalendarAction</code>. */
    static final Integer     EXPORT_CALENDAR = new Integer(36);

    /** Identifies the <code>ValidateXMLAction</code>. */
    static final Integer     VALIDATE_XML = new Integer(37);
    
    /** Identifies the <code>HelpOnlineUserGuide</code>. */
    static final Integer     HELP_USER_GUIDE = new Integer(38);
    
    
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, Action>	actionsMap;
	
	public Controller (XMLModel model, XMLView view) {
	
		this.model = model;
		// listen for changes in selection AND changes in XML (should only be observing one really!)
	
		if (model instanceof ObservableComponent) {
			((ObservableComponent)model).addChangeListener(this);
		}
		this.view = view;
		actionsMap = new HashMap<Integer, Action>();
		createActions();
	}
	
	
	/** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(NEW_FILE, new NewFileAction(model));
        actionsMap.put(OPEN_FILE, new OpenFileAction(model));
        actionsMap.put(OPEN_WWW_FILE, new OpenWwwFileAction(model));
        actionsMap.put(SAVE_FILE, new SaveFileAction(model));
        actionsMap.put(SAVE_FILE_AS, new SaveFileAsAction(model));
        actionsMap.put(EXPORT_ALL_HTML, new ExportAllHtmlAction(model));
        actionsMap.put(EXPORT_HIGHLT_HTML, new ExportHighltdHtmlAction(model));
        actionsMap.put(LOAD_DEFAULTS_ALL, new LoadDefaultsAllAction(model));
        actionsMap.put(LOAD_DEFAULTS_HIGHLT, new LoadDefaultsHighltdAction(model));
        actionsMap.put(CLEAR_FIELDS_ALL, new ClearFieldsAllAction(model));
        actionsMap.put(CLEAR_FIELDS_HIGHLT, new ClearFieldsHighltdAction(model));
        actionsMap.put(MULTIPLY_VALUES, new MultiplyValuesAction(model));
        actionsMap.put(UNDO, new UndoAction(model));
        actionsMap.put(UNDO_NO_NAME, new UndoActionNoNameRefresh(model));
        actionsMap.put(REDO, new RedoAction(model));
        actionsMap.put(REDO_NO_NAME, new RedoActionNoNameRefresh(model));
        actionsMap.put(CLOSE_FILE, new CloseFileAction(model));
        actionsMap.put(ADD_FIELD, new AddFieldAction(model));
        actionsMap.put(DUPLICATE_FIELD, new DuplicateFieldsAction(model));
        actionsMap.put(DELETE_FIELD, new DeleteFieldAction(model));
        actionsMap.put(MOVE_FIELD_UP, new MoveFieldUpAction(model));
        actionsMap.put(MOVE_FIELD_DOWN, new MoveFieldDownAction(model));
        actionsMap.put(PROMOTE_FIELD, new PromoteFieldAction(model));
        actionsMap.put(DEMOTE_FIELD, new DemoteFieldAction(model));
        actionsMap.put(COPY_FIELD, new CopyFieldAction(model));
        actionsMap.put(PASTE_FIELD, new PasteFieldAction(model));
        actionsMap.put(IMPORT_FIELD, new ImportFieldsAction(model));
        actionsMap.put(INDEX_FILES, new IndexFilesAction(model));
        actionsMap.put(EXPORT_ALL_TEXT, new ExportAllTextAction(model));
        actionsMap.put(EXPORT_HIGHLTD_TEXT, new ExportHighltdTextAction(model));
        actionsMap.put(OPEN_CALENDAR, new CalendarOpenAction(model));
        actionsMap.put(REPOPULATE_CALENDAR, new CalendarRepopulateAction(model));
        actionsMap.put(LOCK_FIELDS, new LockFieldsAction(model));
        actionsMap.put(IMPORT_TEXT, new ImportTextAction(model));
        actionsMap.put(IMPORT_TABLE, new ImportTableAction(model));
        actionsMap.put(REQUIRED_FIELD, new RequiredFieldAction(model));
        actionsMap.put(EXPORT_CALENDAR, new ExportCalendarAction(model));
        actionsMap.put(VALIDATE_XML, new ValidateXMLAction(model));
        actionsMap.put(HELP_USER_GUIDE, new HelpOnlineUserGuideAction(model));
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return actionsMap.get(id); }

    
    /**
	 * called by the model when it changes.
	 * Notifies all observers of a change.
	 */
	public void stateChanged(ChangeEvent e) {
		this.fireStateChange();
	}
	
}
