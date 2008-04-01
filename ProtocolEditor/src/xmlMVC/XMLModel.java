
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
		"omero" + File.separator+ "Editor";
	
	/**
	 * These strings are used to add a "version" attribute to the XML documents saved by this application.
	 * But this scheme has not been strictly adhered to yet (no breaking changes to XML schema yet).
	 */
	public static final String VERSION = "version";
	public static final String XML_VERSION_NUMBER = "1.0";
			
	public static final String RELEASE_VERSION_NAME = "ProtocolEditor-Jan08";
	
	/**
	 * DOM Document used to pass XML files between methods such as readXMLtoDOM() and openXMLFile()
	 */
	private Document document; 
	
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
		
		/**
		 * This instantiates the DB required for calendar and alarm functions. 
		 * Throws an exception if another instance of this application is 
		 * running (will be trying to use the same DB). 
		 * 
		 * When this application quits, DBConnectionSingleton.shutDownConnection()
		 * should be called. This will be done under the quit dialog in ui.XMLView. 
		 */
		omeroEditorCalendar = new CalendarMain(this);
		
		
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
		
		try {
			readXMLtoDOM(xmlFile); // overwrites document
		} catch (SAXException e) {
			
			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("File failed to open.",
					"XML was not read correctly. XML may be 'badly-formed'", e);
			
			e.printStackTrace();
			return;
		}	
		
		currentTree = new Tree(document, this, this);
		currentTree.setFile(xmlFile);
		
		openFiles.add(currentTree);
		
		//System.out.println("XMLModel openXMLFile getCurrentFileIndex() = " + getCurrentFileIndex());
		
		document = null;

		xmlUpdated();
		//selectionChanged();
		
		return;
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

	/*
	public void addXMLObserver(XMLUpdateObserver newXMLObserver) {
		xmlObservers.add(newXMLObserver);
	}
	public void addSelectionObserver(SelectionObserver newSelectionObserver) {
		selectionObservers.add(newSelectionObserver);
	}
	*/
	
	/* convert from an XML file into a DOM document */
	public void readXMLtoDOM(File xmlFile) throws SAXException{
		document = XMLMethods.readXMLtoDOM(xmlFile);
	}
	
	public Tree getTreeFromNewFile(File xmlFile) {
		
		try {
			readXMLtoDOM(xmlFile); // overwrites document
		} catch (SAXException e) {
			
			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("File failed to open.",
					"XML was not read correctly. XML may be 'badly-formed'", e);
			
			e.printStackTrace();
			return null;
		}	
		
		Tree tree = new Tree(document);
		
		document = null; 	// release the memory
		
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
	public void writeTreeToDOM() {
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			outputDocument = db.newDocument();
		} catch (Exception ex) { 
			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("File failed to export to XML.",
					"Cannot create new output DOM Document", ex);
			
		}
		Tree tree = getCurrentTree();
		Tree.buildDOMfromTree(tree.getRootNode(), outputDocument);
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
	 */
	public void saveTreeToXmlFile(File outputFile) {
		saveToXmlFile(outputFile);
		
		// try to over-write an existing file in the database (returns false if none found).
		boolean fileInDB = omeroEditorCalendar.updateCalendarFileInDB(outputFile);
		
		if (!fileInDB) {
			omeroEditorCalendar.addCalendarFileToDB(outputFile);
		}
		
		selectionChanged();		// updates View with any changes in file names
	}
	
	private void saveToXmlFile(File outputFile) {
//		 always note the xml version (unless this is custom element)
		if (!getRootNode().getDataField().isCustomInputType()) {
			getRootNode().getDataField().setAttribute(VERSION, XML_VERSION_NUMBER, false);
			getRootNode().getDataField().setAttribute(DataFieldConstants.PROTOCOL_FILE_NAME, outputFile.getName(), true);
		}
		
		// now you can save
		writeTreeToDOM();
			
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			Source source = new DOMSource(outputDocument);
			Result output = new StreamResult(outputFile);
			transformer.transform(source, output);
			
			setCurrentFile(outputFile);	// remember the current file. 
			getCurrentTree().setTreeEdited(false);
			
		} catch (TransformerException e) {
			
			// show error and give user a chance to submit error
			ExceptionHandler.showErrorDialog("XML Transformer Exception",
					"Error in saving file.", e);
			
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

}
