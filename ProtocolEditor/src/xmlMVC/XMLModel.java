package xmlMVC;

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


import java.util.ArrayList;
import java.io.*;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
	
	private ArrayList<XMLUpdateObserver> xmlObservers;
	private ArrayList<SelectionObserver> selectionObservers = new ArrayList<SelectionObserver>();;
	
	/* clipboard of fields, for copy and paste functionality (between files) */
	private ArrayList<DataFieldNode> copiedToClipboardFields = new ArrayList<DataFieldNode>();
	
	
	public static void main(String args[]) {
		try {
			new XMLModel(true);
			
		// catch any uncaught exceptions	
		} catch (Exception se) {
			
			se.printStackTrace();
			// give users chance to submit bug.
			ExceptionHandler.showErrorDialog("Unknown Error", 
					"Abnormal termination due to an uncaught exception.", se);
		} 
	}
	
	
	// default constructor, instantiates empty Tree. View must be created elsewhere.  
	public XMLModel() {
		
		xmlObservers = new ArrayList<XMLUpdateObserver>();
		
		currentTree = null;

		// new XMLView(this);
	}	
	
	// alternative constructor, instantiates empty Tree, then creates new View. 
	public XMLModel(boolean showView) {
		
		xmlObservers = new ArrayList<XMLUpdateObserver>();
		
		currentTree = null;

		if (showView) {
			XMLView view = new XMLView(this);
			view.buildFrame();
		}
			
	}	
	
	// return true if all OK - even if file is open already. (false if failed to open)
	public void openThisFile(File xmlFile) {
		
		// need to check if the file is already open 
		for (Tree tree: openFiles) {
			if (tree.getFile().getAbsolutePath().equals(xmlFile.getAbsolutePath())) {
				currentTree = tree;
				notifyXMLObservers();
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

		notifyXMLObservers();
		//selectionChanged();
		
		return;
	}
	
	// any change in xml that needs the display of xml to be re-drawn
	public void notifyXMLObservers() {
		
		for (XMLUpdateObserver xmlObserver: xmlObservers) {
			xmlObserver.xmlUpdated();
		}
		fireStateChange();
	}
	
	// fireStateChanged() notifies ChangeObservers eg Controller
	// Need to replace xmlUpdated() and selectionChanged() with a single StateChanged()
	public void xmlUpdated() {
		notifyXMLObservers();
	}
	// display needs updating but no change in xml (don't need to re-draw xml display)
	public void selectionChanged() {
		notifySelectionObservers();
	}
	public void notifySelectionObservers(){
		for (SelectionObserver selectionObserver: selectionObservers) {
			selectionObserver.selectionChanged();
		}
		fireStateChange();
	}
	
	public void addXMLObserver(XMLUpdateObserver newXMLObserver) {
		xmlObservers.add(newXMLObserver);
	}
	public void addSelectionObserver(SelectionObserver newSelectionObserver) {
		selectionObservers.add(newSelectionObserver);
	}
	
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
	
	public void setImportTree(Tree tree) {
		
		importTree = tree;
		
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
		notifyXMLObservers();
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

	public void saveTreeToXmlFile(File outputFile) {
		saveToXmlFile(outputFile);
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
		notifyXMLObservers();
	}
	
	// get a reference to the highlighted fields of the current tree
	public ArrayList<DataFieldNode> getHighlightedFields() {
		if (getCurrentTree() == null) return new ArrayList<DataFieldNode>();
		return new ArrayList<DataFieldNode>(getCurrentTree().getHighlightedFields());
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
		notifyXMLObservers();
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
		setCurrentFile(new File("untitled"));	// no current file
		notifyXMLObservers();
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
		notifyXMLObservers();
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
			if (name == null) name = "untitled" + i;
			openFileNames[i] = name;
		}
		return openFileNames;
	}
	
	public void changeCurrentFile(int fileIndex) {
		if (fileIndex >= 0 && fileIndex < openFiles.size())
			currentTree = openFiles.get(fileIndex);
		
		notifyXMLObservers();
	}
	public void closeCurrentFile() {
		openFiles.remove(currentTree);
		if (!openFiles.isEmpty())
			currentTree = openFiles.get(openFiles.size()-1);
		else currentTree = null;
		
		notifyXMLObservers();
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

}
