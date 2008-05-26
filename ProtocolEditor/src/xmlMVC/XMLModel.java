
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

package xmlMVC;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.io.*;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import ome.system.UpgradeCheck;
import omeroCal.model.AlarmChecker;
import omeroCal.model.DBConnectionSingleton;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import calendar.CalendarMain;

import tree.DataField;
import tree.DataFieldConstants;
import tree.DataFieldNode;
import tree.IAttributeSaver;
import tree.Tree;
import tree.Tree.Actions;
import ui.AbstractComponent;
import ui.IModel;
import ui.SelectionObserver;
import ui.XMLUpdateObserver;
import ui.XMLView;
import util.ExceptionHandler;
import util.VersionControlMethods;
import util.XMLMethods;
import util.XmlTransform;
import validation.SAXValidator;

/**
 * principle class of the application model.
 * Manages a list of open files, each represented by the <code>Tree</code> class.
 * Carries out file opening by converting XML documents into DOM Document objects and 
 * passing these to new <code>Tree</code> objects. 
 * Also does file saving via opposite route.
 * 
 * @author will
 *
 */

public class XMLModel 
	extends AbstractComponent
	implements 
	XMLUpdateObserver, 
	SelectionObserver,
	IModel {
	
	/**
	 * This is the main class for the Calendar package that provides a calendar for
	 * displaying DateTimeFields from editor files. 
	 * This class should be instantiated when the application starts up, so that the 
	 * database connection can be opened for this application and 
	 * the alarm checker can get working etc. 
	 */
	CalendarMain omeroEditorCalendar;
	
	/**
	 * The folder that should be used to store all temp. files, config files, logs etc. 
	 */
	public static final String OMERO_EDITOR_FILE = System.getProperty("user.home") + File.separator +
		"omero" + File.separator + "Editor";
	
	/**
	 * This string is used to add a "version" attribute to the XML documents saved by this application.
	 */
	public static final String VERSION = "version";
	
	/**
	 * Prior to OMERO 3.0-Beta-3.0, all documents have been assigned "version=1.0".
	 * The version number will contain both the milestone eg "3.0" and the version, eg "3.1.2"
	 * separated by a dash. So, 3.0-Beta-3.0 will be "3.0-3.0"
	 * 
	 * NB. There is also a reference to the current version in the 
	 * jar "client-3.0-Beta3.jar" etc. This is used at startup, right before the
	 * "splash-screen" is displayed, to check that this is the "current" version.
	 * 
	 */
	public static final String EDITOR_VERSION_NUMBER = "3.0-3.0";
	
	/**
	 * This is an identifier, eg. for Exception handler's bug reporter.
	 * It consists of the EDITOR_VERSION_NUMBER, as well as any release candidate id,
	 * So, 3.0-Beta-3.0, release candidate 2 will be "3.0-3.0rc2".
	 * For the Milestone releases, the editor release Id will be the same as 
	 * the EDITOR_VERSION_NUMBER.
	 */
	public static final String EDITOR_RELEASE_ID = EDITOR_VERSION_NUMBER + "rc2";
	
	/**
	 * DOM Document used to pass XML files between methods such as readXMLtoDOM() and openXMLFile()
	 */
	//private Document document; 
	
	/**
	 * DOM Document used to pass XML files between export/save methods
	 */
	private Document outputDocument;
	
	/**
	 * A list of open files, each represented by a <code>Tree</code> class. 
	 */
	private ArrayList<Tree> openFiles = new ArrayList<Tree>();
	private Tree currentTree;			// tree being currently edited and displayed
	
	ArrayList<String> errorMessages = new ArrayList<String>();	// xmlValidation messages
	
	private Tree importTree;	// tree of a file used for importing fields
	
	
	/* clipboard of fields, for copy and paste functionality (between files) */
	private ArrayList<DataFieldNode> copiedToClipboardFields = new ArrayList<DataFieldNode>();
	
	private boolean treeNeedsRefreshing = false;
	
	/**
	 * Used to increment new file names: eg untitled, untitled 1, untitled 2, etc
	 */
	private int newFileNamingIndex = 1;
	
	/**
	 * 
	 * @param args
	 */
	
	public static void main(String args[]) {
		
		try {
			
			new XMLModel(true);

			
		// catch any uncaught exceptions	
		} catch (Throwable se) {
			
			se.printStackTrace();
			// give users chance to submit bug.
			ExceptionHandler.showErrorDialog("Unknown Error", 
					"Abnormal termination due to an uncaught exception.", se);
		} 
	}
	
	
	// default constructor, instantiates empty Tree. View must be created elsewhere.  
	public XMLModel() {
		
		
		currentTree = null;

		// new XMLView(this);
	}	
	
	// alternative constructor, instantiates empty Tree, then creates new View. 
	public XMLModel(boolean showView) {
		
		StartupShutdown.runStartUp();
		
		/**
		 * This instantiates the DB required for calendar and alarm functions. 
		 * Throws an exception if another instance of this application is 
		 * running (will be trying to use the same DB). 
		 * 
		 * When this JVM quits, DBConnectionSingleton.shutDownConnection()
		 * is called from within that class. 
		 */
		 omeroEditorCalendar = new CalendarMain(this);
		
		/*
		 * Create a folder in the location of OMERO_EDITOR_FILE, if one does not exist already.
		 */
		File omeroEditorDir = new File(OMERO_EDITOR_FILE);
		if (!omeroEditorDir.exists()) {
			System.out.println(OMERO_EDITOR_FILE + " does not exist...");
			omeroEditorDir.getParentFile().mkdir();		// Make the /User/omero/  directory if needed
			omeroEditorDir.mkdir();					// Make the /User/omero/Editor directory
		}
		
		currentTree = null;

		if (showView) {
			XMLView view = new XMLView(this);
			view.buildFrame();
		}
			
	}	
	
	/**
	 * This method takes an XML file, converts it to a DOM document and passes this to 
	 * a new instance of the Tree data structure. 
	 * Then this Tree is added to the list of currently opened files. 
	 */
	public void openThisFile(File xmlFile) {
		
		// need to check if the file is already open 
		for (Tree tree: openFiles) {
			if (tree.getFile().getAbsolutePath().equals(xmlFile.getAbsolutePath())) {
				currentTree = tree;
				xmlUpdated();
				return;
			}
		}
		
		Document document = null;
		
		try {
			document = XMLMethods.readXMLtoDOM(xmlFile); // overwrites document
		} catch (SAXException e) {
			
			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("File failed to open.",
					"XML was not read correctly. XML may be 'badly-formed'", e);
			
			e.printStackTrace();
			return;
		}	
		
		currentTree = new Tree(document, this, this);
		currentTree.setFile(xmlFile);
		
		/*
		 * Check the version of the file, to see if it has been edited by a more
		 * recent version of the software than the current version. 
		 * If so, notify the user via pop-up message. 
		 */
		String fileVersion = currentTree.getVersionNumber();
		String softwareVersion = VersionControlMethods.getSoftwareVersionNumber();
		if(VersionControlMethods.isFileVersionFromFuture(softwareVersion, fileVersion)) {
			JOptionPane.showMessageDialog(null, "Warning. This file was edited by a more recent version \n" +
					"of the Editor software than this version. \n" +
					"You are advised to use the most recent version of OMERO.editor, \n" +
					"so that this file is displayed correctly.");
		}
		
		openFiles.add(currentTree);

		xmlUpdated();
		
	}
	
	
	/**
	 * a flag to tell whether observers need to re-layout any representation of the tree.
	 * @return
	 */
	public boolean treeNeedsRefreshing() {
		return treeNeedsRefreshing;
	}
	
	// fireStateChanged() notifies ChangeObservers eg Controller
	// any change in xml that needs the display of xml to be re-drawn
	public void xmlUpdated() {
		treeNeedsRefreshing = true;
		fireStateChange();
		treeNeedsRefreshing = false;
	}
	
	// display needs updating but no change in xml (don't need to re-draw xml display)
	public void selectionChanged() {
		fireStateChange();
	}

	
	
	public Tree getTreeFromNewFile(File xmlFile) {
		
		Document document = null;
		try {
			document = XMLMethods.readXMLtoDOM(xmlFile); // overwrites document
		} catch (SAXException e) {
			
			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("File failed to open.",
					"XML was not read correctly. XML may be 'badly-formed'", e);
			
			e.printStackTrace();
			return null;
		}	
		
		Tree tree = new Tree(document);
		
		return tree;
	}
	
	// The first step in importing fields from a file.
	public void setImportFile(File xmlFile) {
		if (xmlFile != null) {
			setImportTree(getTreeFromNewFile(xmlFile));
		} else {
			setImportTree(null);
		}
		// update changeListeners
		fireStateChange();
	}
	
	public void setImportTree(Tree tree) {
		importTree = tree;
	}
	
	// get a reference to the root of the import tree. 
	public DataFieldNode getImportTreeRoot() {
		if (importTree != null) {
			return importTree.getRootNode();
		} else
			return null;
	}
	
	// import the selected nodes of the tree, or if none selected, import it all!
	public void importFieldsFromImportTree() {
		Tree tree = getCurrentTree();
		if (importTree.getHighlightedFields().size() > 0) {
			tree.copyAndInsertDataFields(importTree.getHighlightedFields());
		}
			
		else {
			ArrayList<DataFieldNode> rootNodeList = new ArrayList<DataFieldNode>();
			rootNodeList.add(importTree.getRootNode());
			tree.copyAndInsertDataFields(rootNodeList); 
		}
		xmlUpdated();
	}
	
	// convert the tree data-structure to it's xml representation as a DOM document
	public boolean writeTreeToDOM() {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			outputDocument = db.newDocument();
			Tree tree = getCurrentTree();
			Tree.buildDOMfromTree(tree.getRootNode(), outputDocument);
			return true;
		} catch (Exception ex) { 
			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("File failed to export to XML.",
					"Cannot create new output DOM Document", ex);
			return false;
		}
		
	} 
	
	
	// not used currently due to problems packaging the xsl into .jar
	public void transformXmlToHtml() {
		
		File outputXmlFile = new File("file");
		
		saveTreeToXmlFile(outputXmlFile);
		
		// opens the HTML in a browser window
		XmlTransform.transformXMLtoHTML(outputXmlFile);
	}
	
	
	public void validateCurrentXmlFile() {
		// first save current Tree
		writeTreeToDOM();
		
		errorMessages.clear();
		
		try {
			errorMessages = SAXValidator.validate(outputDocument);
		} catch (SAXException e) {
			//e.printStackTrace();
			errorMessages.add(e.getMessage());
		}
		
		if (errorMessages.isEmpty()) {
			//System.out.println("Current XML is valid");
		}
	}
	public ArrayList<String> getErrorMessages() {
		return errorMessages;
	}

	/**
	 * This is used by SaveFileAs action as well as Save action (once user has confirmed over-write).
	 * Can't tell at this stage whether outputFile is new or being overwritten.
	 * This also saves (or updates) the file to the calendar database. 
	 * 
	 * @return 	true if saving went OK (no exceptions etc). 
	 */
	public boolean saveTreeToXmlFile(File outputFile) {
		boolean savedOK = saveToXmlFile(outputFile);
		
		if (savedOK) {
			
			setCurrentFile(outputFile);	// remember the current file. 
			getCurrentTree().setTreeEdited(false);
			
			// try to over-write an existing file in the calendar database (returns false if none found).
			boolean fileInDB = omeroEditorCalendar.updateCalendarFileInDB(outputFile);
			
			if (!fileInDB) {
				omeroEditorCalendar.addCalendarFileToDB(outputFile);
			}
		}
		
		selectionChanged();		// updates View with any changes in file names
		
		return savedOK;
	}
	
	
	/**
	 * This provides the same functionality as 
	 * saveTreeToXmlFile(file) except:
	 * The tree's file is not updated with the xmlFile - Still references the file the tree came from.
	 * The tree method setEdited(false) is not called.
	 * The calendar database is not updated with the newly saved file. 
	 * 
	 * This method is simply for exporting the tree, as a file, without changing the tree. 
	 * 
	 * @param xmlFile
	 * @return	true if successful
	 * @see  saveToXmlFile(file);
	 */
	public boolean exportTreeToXmlFile(File xmlFile) {
		return saveToXmlFile(xmlFile);
	}
	
	/**
	 * This method will work on the current Tree (currently opened file).
	 * It saves the content of the Tree to the XML file. 
	 * Each Node (or dataField) of the tree will become an XML element and the
	 * structure of the tree will be replicated in the XML file. 
	 * 
	 * @param outputFile	The XML file to which the current tree will be saved.
	 * @return 		true if the save action went OK 
	 */
	private boolean saveToXmlFile(File outputFile) {
//		 always note the xml version (unless this is custom element)
		if (!getRootNode().getDataField().isCustomInputType()) {
			
			/*
			 * If the file version is not from the future (ie if it is past or present)
			 * then update the version number with the current software version number. 
			 */
			String fileVersionNumber = getCurrentTree().getVersionNumber();
			if (! VersionControlMethods.isFileVersionFromFuture(fileVersionNumber))
				getCurrentTree().setVersionNumber(EDITOR_VERSION_NUMBER);
			
			getRootNode().getDataField().setAttribute(DataFieldConstants.PROTOCOL_FILE_NAME, outputFile.getName(), false);
			// don't add the protocolFileName change to undo/redo, but still want UI to update...
			getRootNode().getDataField().notifyDataFieldObservers();
		}
		
		/*
		 * Now you can save.
		 * If writeTreeToDOM() returns false, there was a problem creating the outputDocument,
		 * so DON'T overwrite the existing file
		 */
		boolean writtenToDOM = writeTreeToDOM();
		
		if ((!writtenToDOM) && (outputFile.exists())) {
			
			JOptionPane.showMessageDialog(null, "Error in saving file \n" +
					"(Problem creating DOM document)\n" +
					"It is not possible to save the complete file.\n" +
					"Use 'SAVE-AS' to save as much as possible without\n" +
					"over-writing the existing file.", "Save Error", JOptionPane.ERROR_MESSAGE);
			
			return false;
			
		} else {
			/*
			 * It is OK to save (won't overwrite since file does not exist) but still give warning if 
			 * there was a problem..
			 */
			if (!writtenToDOM) {
				JOptionPane.showMessageDialog(null, "Problem in saving file \n" +
						"(Problem creating DOM document)\n" +
						"Part of the file may not have been saved", 
						"Save Error", JOptionPane.ERROR_MESSAGE);
			}
			
			/*
			 * If outputDocument created OK, save to file...
			 */
			Transformer transformer;
			try {
				transformer = TransformerFactory.newInstance().newTransformer();
				Source source = new DOMSource(outputDocument);
				Result output = new StreamResult(outputFile);
				transformer.transform(source, output);
				
			} catch (TransformerException e) {
				
				// show error and give user a chance to submit error
				ExceptionHandler.showErrorDialog("XML Transformer Exception",
						"Error in saving file.", e);
			}
			
			return (writtenToDOM);
		}
	}
	
	// delegates commands from xmlView to Tree. int Tree.editCommand is a list of known commands
	public void editCurrentTree(Actions editCommand) {
		// System.out.println("XMLModel editCurrentTree: " + editCommand.toString());
		Tree tree = getCurrentTree();
		if (tree != null )tree.editTree(editCommand);
		xmlUpdated();
	}
	
	// get a reference to the highlighted fields of the current tree
	public ArrayList<DataFieldNode> getHighlightedFields() {
		if (getCurrentTree() == null) return new ArrayList<DataFieldNode>();
		return new ArrayList<DataFieldNode>(getCurrentTree().getHighlightedFields());
	}
	
	/**
	 * This method tests to see if any of the currently highlighted fields
	 * are locked. 
	 * If so, various editing actions are disabled. 
	 * 
	 * @return	true if any of the fields are locked. 
	 * Fields are considered locked if their ancestors are locked.
	 */
	public boolean areHighlightedFieldsLocked() {
		if (getCurrentTree() == null) 
			return false;
		return getCurrentTree().areHighlightedFieldsLocked();
	}
	
	/**
	 * This method is used to get details of the highlighted fields that are locked. 
	 * Each highlighted field that is locked is represented by a HashMap, 
	 * containing "locking attributes" of the field, such as timeStamp and userName. 
	 * 
	 * @return		A list of hashMaps, corresponding to the list of highlighted locked fields. 
	 */
	public List<HashMap<String, String>> getLockedFieldsAttributes() {
		if (getCurrentTree() == null) return null;
		return getCurrentTree().getLockedFieldsAttributes();
	}
	
	/**
	 * This checks whether ancestors of the highlighted fields
	 * have the attribute FIELD_LOCKED_UTC.
	 * This method IGNORES the highlighted fields themselves. 
	 * Presence of this attribute indicates that ancestors of the highlighted fields
	 * are "Locked" and editing is not allowed. 
	 * 
	 * @return	true if any of the highlighted field ancestors has the attribute FIELD_LOCKED_UTC.
	 */
	public boolean areAncestorFieldsLocked() {
		if (getCurrentTree() == null) 
			return false;
		return getCurrentTree().areAncestorFieldsLocked();
	}
	
	/**
	 * This checks whether the highlighted fields are locked (ie have the attribute LOCK_LEVEL)
	 * and returns the "max" level of locking for all the highlighted fields.
	 * ie LOCKED_ALL_ATTRIBUTES is a 'higher' level than LOCKED_TEMPLATE. Returns null if no
	 * fields are locked. 
	 * Editing actions that apply to the currently highlighted fields can setEnabled(), based
	 * on this locking level. 
	 * 
	 * @return	the max "lockLevel" of highlighted fields, or null if none are locked.
	 */
	public String getMaxHighlightedLockingLevel() {
		if (getCurrentTree() == null) 
			return null;
		return getCurrentTree().getMaxHighlightedLockingLevel();
	}
	
	/**
	 * This checks for the MAX locking level of any highlighted fields and 
	 * their children. 
	 * Used (for example) by "Load Defaults" and "Clear Fields" actions, which
	 * apply to all children of highlighted fields, and should be disabled
	 * if any highlighted fields (or their children) are fully locked. 
	 * 
	 * @return  the max "lockLevel" if any highlighted fields or children 
 	 * 			are locked, or null if none are locked.
	 */
	public String getMaxHighlightedChildLockingLevel() {
		if (getCurrentTree() == null) 
			return null;
		return getCurrentTree().getMaxHighlightedChildLockingLevel();
	}
	
	/**
	 * This checks whether ANY fields in this tree are locked (ie have the attribute LOCK_LEVEL)
	 * and returns the "max" level of locking for the tree.
	 * ie LOCKED_ALL_ATTRIBUTES is a 'higher' level than LOCKED_TEMPLATE. Returns null if no
	 * fields are locked. Editing actions that 
	 * apply to the whole tree (eg Clear All Fields or Load Defaults All Fields) should be disabled 
	 * if all attributes locked, but enabled if only the template is locked. 
	 * 
	 * @return	the max "lockLevel" if any fields in the tree are locked, or null if no fields locked.
	 */
	public String getMaxLockingLevel() {
		if (getCurrentTree() == null) 
			return null;
		return getCurrentTree().getMaxLockingLevel();
	}
	
	// make a copy of the currently highlighted fields
	public void copyHighlightedFieldsToClipboard() {
		if (getCurrentTree() == null) return;
		copiedToClipboardFields = new ArrayList<DataFieldNode>(getCurrentTree().getHighlightedFields());
	}
	/* paste the clipboard fields into the current Tree */
	public void pasteHighlightedFieldsFromClipboard() {
		if ((getCurrentTree() == null) || (copiedToClipboardFields.isEmpty())) return;
		getCurrentTree().copyAndInsertDataFields(copiedToClipboardFields);
		xmlUpdated();
	}
	
	// used for undo button tool tip etc
	public String getUndoCommand() {
		if (getCurrentTree() == null) return "";
		return getCurrentTree().getUndoCommand();
	}
	public String getRedoCommand() {
		if (getCurrentTree() == null) return "";
		return getCurrentTree().getRedoCommand();
	}
	public boolean canUndo() {
		if (getCurrentTree() == null) return false;
		return getCurrentTree().canUndo();
	}
	public boolean canRedo() {
		if (getCurrentTree() == null) return false;
		return getCurrentTree().canRedo();
	}
	
	/**
	 * This adds a time-stamp (UTCmillisecs) to each field, to indicate
	 * that they are locked (and when). 
	 * Other attributes in the lockingAttributes map will also be added, 
	 * to describe the User, Locking Level etc. 
	 * This method is delegated to current Tree. 
	 * 
	 * @param lockingAttributes		A map of additional attributes that define the lock
	 */
	public void lockHighlightedFields(Map<String, String> lockingAttributes) {
		if (getCurrentTree() == null) return;
		getCurrentTree().lockHighlightedFields(lockingAttributes);
	}
	
	/**
	 * This checks to see if any field marked as "Required" (DataFieldConstants.REQUIRED_FIELD = 'true')
	 * is also not filled out (ie dataField.isFieldFilled() is false). 
	 * Used for ensuring that "required" fields are not left blank when the form is saved.
	 * This is delegated to Tree. 
	 * 
	 * @return	True if any required field is not filled out. 
	 */
	public boolean isAnyRequiredFieldEmpty() {
		if (getCurrentTree() == null) return false;
		return getCurrentTree().isAnyRequiredFieldEmpty();
	}
	
	/**
	 * This checks to see if any field that has a default value, also has a value that 
	 * would be over-written if defaults were loaded. 
	 * Used to give users a warning that loading defaults (whole tree) would over-write stuff. 
	 * 
	 * @return	True if any field with a default value is not cleared.
	 */
	public boolean isAnyDefaultFieldFilled() {
		if (getCurrentTree() == null) return false;
		return getCurrentTree().isAnyDefaultFieldFilled();
	}
	
	/**
	 * This checks to see if any highlighted field that has a default value, also has a value that 
	 * would be over-written if defaults were loaded. 
	 * Used to give users a warning that loading defaults (highlighted fields) would over-write stuff. 
	 * 
	 * @return	True if any highlighted field with a default value is not empty.
	 */
	public boolean isAnyHighlightedDefaultFieldFilled() {
		if (getCurrentTree() == null) return false;
		return getCurrentTree().isAnyHighlightedDefaultFieldFilled();
	}
	
	// works on numerical fields only
	public void multiplyValueOfSelectedFields(float factor) {
		getCurrentTree().multiplyValueOfSelectedFields(factor);
	}
	
	// start a blank protocol
	public void openBlankProtocolFile() {
		currentTree = new Tree(this, this);
		openFiles.add(currentTree);
		// create new file(name)
		setCurrentFile(new File(OMERO_EDITOR_FILE + File.separator + "untitled" 
				+ (newFileNamingIndex < 2 ? "" : newFileNamingIndex) + ".tmp"));
		newFileNamingIndex++;
		
		xmlUpdated();
	}
	
	/**
	 * Allows classes that have created their own trees to open them as new
	 * files. 
	 * 
	 * @param tree
	 */
	public void openTree(Tree tree) {
		
		currentTree = tree;
		openFiles.add(currentTree);
		// create new file(name)
		setCurrentFile(new File("untitled" + (newFileNamingIndex < 2 ? "" : newFileNamingIndex)));
		newFileNamingIndex++;
		
		xmlUpdated();
	}
	
	public ArrayList<IAttributeSaver> getObservationFields() {
		if(getCurrentTree() == null) 
			return new ArrayList<IAttributeSaver>();
		return getCurrentTree().getObservationFields();
	}
	
	// used to tell if there are any changes to the current file that need saving
	public boolean isCurrentFileEdited() {
		if ((getCurrentTree() != null) && (getCurrentTree().isTreeEdited())) {
			return true;
		}
		else {
			return false;
		}
	}

	// called when saving
	public void setCurrentFile(File file) {
		getCurrentTree().setFile(file);
	}
	// used to display list of open files
	public File getCurrentFile() {
		if (getCurrentTree() != null)
			return getCurrentTree().getFile();
		else return null;
	}
	
	// for each file, can turn on/off xmlValidation
	public void setXmlValidation(boolean validationOn) {
		if (getCurrentTree() != null)
			getCurrentTree().setXmlValidation(validationOn);
	}
	public boolean getXmlValidation() {
		if (getCurrentTree() != null)
			return getCurrentTree().getXmlValidation();
		else return false;
	}
	
	
	public DataFieldNode getRootNode() {
		Tree tree = getCurrentTree();
		if (tree != null)
		return tree.getRootNode();
		
		else return null;
	}
	public JPanel getFieldEditorToDisplay() {
		Tree tree = getCurrentTree();
		if (tree == null) return new JPanel();
		else
			return tree.getFieldEditorToDisplay();
	}
	
	// used to find potential child names for OME elements
	public String getNameOfCurrentFieldsParent() {
		return getCurrentTree().getNameOfCurrentFieldsParent();
	}
	// used to add a child of defined name
	public void addNewChildToTree(String childName) {
		if (getCurrentTree() == null) return;
		getCurrentTree().addDataField(childName);
		xmlUpdated();
	}
	
	public Tree getCurrentTree() {
		return currentTree;
	}
	
	// getSearchResults() used to find text within current doc
	public ArrayList<DataField> getSearchResults(String searchWord) {
		if (getCurrentTree() == null)	return new ArrayList<DataField>();
		return getCurrentTree().getSearchResults(searchWord);
	}
	
	// close all files that are saved, leave others open.
	// return true if all were closed.
	public boolean tryClosingAllFiles() {
		
		for (int i=openFiles.size()-1 ;i>=0 ; i--) {
			Tree tree = openFiles.get(i);
			if (!tree.isTreeEdited()) {
				openFiles.remove(tree);
			}
		}
		if (openFiles.isEmpty()) {
			currentTree = null;
			return true;
		} 
		else currentTree = openFiles.get(openFiles.size()-1);
		return false;
	}
	
	public String[] getOpenFileList() {
		
		String[] openFileNames = new String[openFiles.size()];
		
		for (int i=0; i<openFileNames.length; i++) {
			String name = null;
			File file = openFiles.get(i).getFile();
			if (file != null) name = file.getName();
			// this shouldn't happen, but just in case...
			if (name == null) name = "untitled" + " " + i;
			openFileNames[i] = name;
		}
		return openFileNames;
	}
	
	public void changeCurrentFile(int fileIndex) {
		if (fileIndex >= 0 && fileIndex < openFiles.size())
			currentTree = openFiles.get(fileIndex);
		
		xmlUpdated();
	}
	public void closeCurrentFile() {
		openFiles.remove(currentTree);
		if (!openFiles.isEmpty())
			currentTree = openFiles.get(openFiles.size()-1);
		else currentTree = null;
		
		xmlUpdated();
	}
	public int getCurrentFileIndex() {
		return openFiles.indexOf(currentTree);
	}
	
	// compare the first two files in the list
	public void compareCurrentFiles() {
		if (openFiles.size() > 1) {
			// TreeCompare.compareTrees(openFiles.get(0), openFiles.get(1));
		}
	}
	
	/**
	 * Delegate calendar display etc to the calendar main class. 
	 * This has already has been instantiated (connection and alarm-checker setup)
	 * so now just need to display the calendar. 
	 */
	public void displayCalendar() {
		omeroEditorCalendar.openDBAndDisplayUI(false);		// false - not stand-alone app.
	}
	
	/**
	 * Asks the user to confirm that it is OK to overwrite the database (clear tables)
	 * and then asks for a root directory for all Omero.editor files. 
	 * Iterates through all files, adding those that contain dates to the calendar. 
	 */
	public void repopulateCalendarDB() {
		omeroEditorCalendar.repopulateDB();
	}
	
	/**
	 * Exports DateTime fields in the current File(tree) to an iCalendar(ics) format file,
	 * specified by the exportFilePath. 
	 * 
	 * @param exportFilePath	The path and file name to export to.
	 */
	public void exportFileEventsToICalendar(String exportFilePath) {
		File xmlFile = getCurrentTree().getFile();
		CalendarMain.exportToICalendar(xmlFile, exportFilePath);
	}

}
