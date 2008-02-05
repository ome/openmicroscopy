package ui;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeListener;

import actions.ClearFieldsAllAction;
import actions.ClearFieldsHighltdAction;
import actions.LoadDefaultsAllAction;
import actions.LoadDefaultsHighltdAction;
import actions.MultiplyValuesAction;
import actions.NewFileAction;
import actions.OpenFileAction;
import actions.OpenWwwFileAction;
import actions.PrintExportAllAction;
import actions.PrintExportHighltd;
import actions.RedoAction;
import actions.RedoActionNoNameRefresh;
import actions.SaveFileAction;
import actions.SaveFileAsAction;
import actions.UndoAction;
import actions.UndoActionNoNameRefresh;

import tree.DataFieldNode;
import tree.Tree.Actions;
import xmlMVC.XMLModel;

public class Controller 
	extends AbstractComponent
	implements IModel, SelectionObserver, XMLUpdateObserver {

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
    
    /** Identifies the <code>Print-Export all action</code>. */
    static final Integer     PRINT_EXPORT_ALL = new Integer(5);
    
    /** Identifies the <code>Print-Export action</code>. */
    static final Integer     PRINT_EXPORT_HIGHLT = new Integer(6);
    
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
    
    
    /** Maps actions ids onto actual <code>Action</code> object. */
    private Map<Integer, Action>	actionsMap;
	
	public Controller (XMLModel model, XMLView view) {
	
		this.model = model;
		// listen for changes in selection AND changes in XML (should only be observing one really!)
		model.addSelectionObserver(this);
		model.addXMLObserver(this);
		this.view = view;
		actionsMap = new HashMap<Integer, Action>();
		createActions();
	}
	
	
	/** Helper method to create all the UI actions. */
    private void createActions()
    {
        actionsMap.put(NEW_FILE, new NewFileAction(this));
        actionsMap.put(OPEN_FILE, new OpenFileAction(this));
        actionsMap.put(OPEN_WWW_FILE, new OpenWwwFileAction(this));
        actionsMap.put(SAVE_FILE, new SaveFileAction(this));
        actionsMap.put(SAVE_FILE_AS, new SaveFileAsAction(this));
        actionsMap.put(PRINT_EXPORT_ALL, new PrintExportAllAction(this));
        actionsMap.put(PRINT_EXPORT_HIGHLT, new PrintExportHighltd(this));
        actionsMap.put(LOAD_DEFAULTS_ALL, new LoadDefaultsAllAction(this));
        actionsMap.put(LOAD_DEFAULTS_HIGHLT, new LoadDefaultsHighltdAction(this));
        actionsMap.put(CLEAR_FIELDS_ALL, new ClearFieldsAllAction(this));
        actionsMap.put(CLEAR_FIELDS_HIGHLT, new ClearFieldsHighltdAction(this));
        actionsMap.put(MULTIPLY_VALUES, new MultiplyValuesAction(this));
        actionsMap.put(UNDO, new UndoAction(this));
        actionsMap.put(UNDO_NO_NAME, new UndoActionNoNameRefresh(this));
        actionsMap.put(REDO, new RedoAction(this));
        actionsMap.put(REDO_NO_NAME, new RedoActionNoNameRefresh(this));
        
    }
    
    /**
     * Returns the action corresponding to the specified id.
     * 
     * @param id One of the flags defined by this class.
     * @return The specified action.
     */
    Action getAction(Integer id) { return actionsMap.get(id); }

    
    /**
	 * called by the model when selection is changed.
	 * Notifies all observers of a change.
	 */
	public void selectionChanged() {
		System.out.println("Controller selectionChanged()");
		this.fireStateChange();
	}

	public void xmlUpdated() {
		selectionChanged();
	}
	
	
    
    public void openBlankProtocolFile() {
    	model.openBlankProtocolFile();
    }
  
	
	public void openThisFile(File file) {
		 boolean openOK = model.openXMLFile(file);
		 
		 if (!openOK) {
//			custom title, error icon
			 JOptionPane.showMessageDialog(view.getFrame(),
			     "Problem reading file.",
			     "XML error",
			     JOptionPane.ERROR_MESSAGE);
			 return;
		 }
	}
	


	// delegate to model
	public File getCurrentFile() {
		return	model.getCurrentFile();
	}

	// delegate to model
	public void saveTreeToXmlFile(File file) {
		model.saveTreeToXmlFile(file);
	}
	
	// delegate to model
	public String[] getOpenFileList() {
		return model.getOpenFileList();
	}
	
	// delegate to model
	public DataFieldNode getRootNode() {
		return model.getRootNode();
	}
	
	// delegate to model
	public List<DataFieldNode> getHighlightedNodes() {
		return model.getHighlightedFields();
	}

	// delegate to model
	public void editCurrentTree(Actions newAction) {
		model.editCurrentTree(newAction);
	}
	
	// delegate to model
	public void multiplyValueOfSelectedFields(float factor) {
		model.multiplyValueOfSelectedFields(factor);
	}

	// delegate to model
	public boolean canRedo() {
		return model.canRedo();
	}

	// delegate to model
	public boolean canUndo() {
		return model.canUndo();
	}

	// delegate to model
	public String getRedoCommand() {
		return model.getRedoCommand();
	}

	// delegate to model
	public String getUndoCommand() {
		return model.getUndoCommand();
	}

	
}
