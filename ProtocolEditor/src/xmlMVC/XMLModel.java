
package xmlMVC;

import java.util.ArrayList;
import java.io.*;

import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.xml.sax.SAXParseException;

import validation.SAXValidator;

// import test.TreeCompare;

// main class. 
// first class to be instantiated 
// responsible for opening and saving xml files 
// and communication between Tree class and xmlView class

public class XMLModel implements XMLUpdateObserver, SelectionObserver{
	
	public static final String VERSION = "version";
	public static final String XML_VERSION_NUMBER = "1.0";
			
	private Document document; 
	private Document outputDocument;
	
	private ArrayList<Tree> openFiles = new ArrayList<Tree>();
	private Tree currentTree;			// tree being currently edited and displayed
	
	ArrayList<String> errorMessages = new ArrayList<String>();	// xmlValidation messages
	
	private Tree importTree;	// tree of a file used for importing fields
	
	private ArrayList<XMLUpdateObserver> xmlObservers;
	private ArrayList<SelectionObserver> selectionObservers = new ArrayList<SelectionObserver>();;
	
	
	public static void main(String args[]) {
		new XMLModel();
	}
	
	
	// default constructor, instantiates empty Tree, then creates new View. 
	public XMLModel() {
		
		xmlObservers = new ArrayList<XMLUpdateObserver>();
		
		currentTree = null;

		new XMLView(this);
	}	
	
	// return true if all OK - even if file is open already. (false if failed to open)
	public boolean openXMLFile(File xmlFile) {
		
		// need to check if the file is already open 
		for (Tree tree: openFiles) {
			System.out.println("XMLModel openXMLFile tree.getFile().getAbsolutePath() = " + tree.getFile().getAbsolutePath() );
			if (tree.getFile().getAbsolutePath().equals(xmlFile.getAbsolutePath())) {
				currentTree = tree;
				notifyXMLObservers();
				return true;
			}
		}
		
		try {
			readXMLtoDOM(xmlFile); // overwrites document
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		}	
		
		currentTree = new Tree(document, this, this);
		currentTree.setFile(xmlFile);
		
		openFiles.add(currentTree);
		
		//System.out.println("XMLModel openXMLFile getCurrentFileIndex() = " + getCurrentFileIndex());
		
		document = null;

		notifyXMLObservers();
		
		return true;
	}
	
	// any change in xml that needs the display of xml to be re-drawn
	public void notifyXMLObservers() {
		
		for (XMLUpdateObserver xmlObserver: xmlObservers) {
			xmlObserver.xmlUpdated();
		}
		// when xml validation is switched on, validate every change in xml
		if (getXmlValidation())
			saxValidateCurrentXmlFile();
	}
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
	}
	
	public void addXMLObserver(XMLUpdateObserver newXMLObserver) {
		xmlObservers.add(newXMLObserver);
	}
	public void addSelectionObserver(SelectionObserver newSelectionObserver) {
		selectionObservers.add(newSelectionObserver);
	}
	
	
	public void readXMLtoDOM(File xmlFile) throws SAXException{
		DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);   
        //factory.setNamespaceAware(true);
        
        try {
           DocumentBuilder builder = factory.newDocumentBuilder();

           builder.setErrorHandler(
                   new org.xml.sax.ErrorHandler() {
                       // ignore fatal errors (an exception is guaranteed)
                       public void fatalError(SAXParseException exception)
                       throws SAXException {
                       }

                       // treat validation errors as fatal
                       public void error(SAXParseException e)
                       throws SAXParseException
                       {
                         throw e;
                       }

                       // dump warnings too
                       public void warning(SAXParseException err)
                       throws SAXParseException
                       {
                         System.out.println("** Warning"
                            + ", line " + err.getLineNumber()
                            + ", uri " + err.getSystemId());
                         System.out.println("   " + err.getMessage());
                       }
                   }
                 ); 

           document = builder.parse( xmlFile );
           
        } catch (SAXException sxe) {
            throw sxe;

         } catch (ParserConfigurationException pce) {
             // Parser with specified options can't be built
             pce.printStackTrace();

         } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
         }
	}
	
	public Tree getTreeFromNewFile(File xmlFile) {
		
		try {
			readXMLtoDOM(xmlFile); // overwrites document
		} catch (SAXException e) {
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
			ex.printStackTrace();
		}
		Tree tree = getCurrentTree();
		tree.buildDOMfromTree(outputDocument);
	} 
	
	
	// not used currently due to problems packaging the xsl into .jar
	public void transformXmlToHtml() {
		
		File outputXmlFile = new File("file");
		
		saveTreeToXmlFile(outputXmlFile);
		
		// opens the HTML in a browser window
		XmlTransform.transformXMLtoHTML(outputXmlFile);
	}
	
	
	public void saxValidateCurrentXmlFile() {
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
		// now update the display of messages...
		selectionChanged();
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
		if (!getRootNode().getDataField().isCustomInputType())
			getRootNode().getDataField().setAttribute(VERSION, XML_VERSION_NUMBER, false);
		
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
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
	}
	
	// calls method in each dataField, to setValue to default and display
	public void copyDefaultValuesToInputFields() {
		Tree tree = getCurrentTree();
		tree.copyDefaultValuesToInputFields();
	}
	
	// delegates commands from xmlView to Tree. int Tree.editCommand is a list of known commands
	public void editCurrentTree(int editCommand) {
		Tree tree = getCurrentTree();
		tree.editTree(editCommand);
		notifyXMLObservers();
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
	
	// used to tell if there are any changes to the current file that need saving
	public boolean isCurrentFileEdited() {
		if ((getCurrentTree() != null) && (getCurrentTree().isTreeEdited())) {
			System.out.println("XMLModel isCurrentFileEdited = true");
			return true;
		}
		else {
			System.out.println("XMLModel isCurrentFileEdited = false");
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
	
	public Tree getCurrentTree() {
		return currentTree;
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
