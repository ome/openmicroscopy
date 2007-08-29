package xmlMVC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.io.*;

import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMLModel implements XMLUpdateObserver, SelectionObserver{
	
	public static final String INPUT = "input";
	public static final String PROTOCOL = "protocol";
		
	private Document document; 
	private Document outputDocument;
	private Tree tree;			// tree model of template
	private Tree importTree;	// tree of a file used for importing fields
	
	private File currentFile;
	private boolean currentProtocolEdited;
	
	private ArrayList<XMLUpdateObserver> xmlObservers;
	private ArrayList<SelectionObserver> selectionObservers = new ArrayList<SelectionObserver>();;
	
	
	public static void main(String args[]) {
		new XMLModel();
	}
	
	
	// default constructor, instantiates empty Tree, then creates new View. 
	public XMLModel() {
		currentFile = new File("file");
		
		xmlObservers = new ArrayList<XMLUpdateObserver>();
		
		tree = new Tree(this, this);

		new XMLView(this);
	}	
	
	public void openXMLFile(File xmlFile) {

		currentFile = xmlFile;
		
		readXMLtoDOM(xmlFile);	// overwrites document
		
		tree = new Tree(document, this, this);
		
		document = null;

		notifyXMLObservers();
	}
	
	public void notifyXMLObservers() {
		for (XMLUpdateObserver xmlObserver: xmlObservers) {
			xmlObserver.xmlUpdated();
		}
	}
	public void xmlUpdated() {
		currentProtocolEdited = true;
		notifyXMLObservers();
	}
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
	
	
	public void readXMLtoDOM(File xmlFile) {
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
            // Error generated during parsing)
            Exception  x = sxe;
            if (sxe.getException() != null)
                x = sxe.getException();
            x.printStackTrace();

         } catch (ParserConfigurationException pce) {
             // Parser with specified options can't be built
             pce.printStackTrace();

         } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
         }
	}
	
	
	// 
	public void insertProtocolFromNewFile(File xmlFile) {
		
		readXMLtoDOM(xmlFile);	// overwrites document
		
		tree.insertProtocolFromNewFile(document);

		notifyXMLObservers();
	}
	
	public Tree getTreeFromNewFile(File xmlFile) {
		
		readXMLtoDOM(xmlFile);	// overwrites document
		
		Tree tree = new Tree(document);
		
		document = null; 	// release the memory
		
		return tree;
	}
	
	public void setImportTree(Tree tree) {
		
		importTree = tree;
		
	}
	
	// import the selected nodes of the tree, or if none selected, import it all!
	public void importFieldsFromImportTree() {
		if (importTree.getHighlightedFields().size() > 0) {
			tree.copyAndInsertDataFields(importTree.getHighlightedFields());
		}
			
		else {
			tree.copyAndInsertDataField(importTree.getRootNode()); 
		}
		
		notifyXMLObservers();
		}
	
	public void writeTreeToDOM(boolean saveExpValues) {
		
		// don't save experiment if protocol has been edited
		if ((saveExpValues) && (currentProtocolEdited))
			return;
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			outputDocument = db.newDocument();
			Element protocol = outputDocument.createElement(PROTOCOL);
		} catch (Exception ex) { 
			ex.printStackTrace();
		}
		
		tree.buildDOMfromTree(outputDocument, saveExpValues);
	} 
	
	public void transformXmlToHtml() {
		
		File outputXmlFile = new File("file");
		
		saveTreeToXmlFile(outputXmlFile, true);
		
		// opens the HTML in a browser window
		XmlTransform.transformXMLtoHTML(outputXmlFile);
	}

	public void saveTreeToXmlFile(File outputFile, boolean saveExpValues) {
		
		// if you are saving experiment
		if (saveExpValues) {
			// and currentProtocol is edited
			if (currentProtocolEdited)
				return; 	// forget about it! - shouldn't get here. - save protocol first
			
			
			if (getCurrentFile().getName().endsWith(".pro")) {
				// protocol will have .exp derived from it, so make note of that in the current .pro file
				getRootNode().getDataField().setAttribute(DataField.PRO_HAS_EXP_CHILDREN, DataField.TRUE);
				saveTreeToXmlFile(currentFile, false);
			}			
		} else {
			// saving protocol, so first include the name into the file.. 
			getRootNode().getDataField().setAttribute(DataField.PROTOCOL_FILE_NAME, outputFile.getName());
			// saving protocol means that there are no .exp children of the .pro file..
			getRootNode().getDataField().setAttribute(DataField.PRO_HAS_EXP_CHILDREN, DataField.FALSE);
		}
		
		writeTreeToDOM(saveExpValues);
		
		try {
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Source source = new DOMSource(outputDocument);
			Result output = new StreamResult(outputFile);
			transformer.transform(source, output);
			
			// if you have saved the protocol..
			if (!saveExpValues) {
				currentProtocolEdited = false;
			}
			
			setCurrentFile(outputFile);	// remember the current file. 
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void printDOM( Document docToPrint) {
		try {
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			Source source = new DOMSource( docToPrint );
			Result output = new StreamResult( System.out );
			transformer.transform(source, output);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("");
	}
	
	// calls method in each dataField, to setValue to default and display
	public void copyDefaultValuesToInputFields() {
		tree.copyDefaultValuesToInputFields();
	}
	
	// start a blank protocol
	public void openBlankProtocolFile() {
		tree.openBlankProtocolFile();
		setCurrentFile(new File(""));	// no current file
		notifyXMLObservers();
	}
	
	// example with different field types
	public void openDemoProtocolFile() {
		tree.openDemoProtocolFile();
		setCurrentFile(new File(""));	// no current file
		notifyXMLObservers();	
	}

	
	// add a new dataField after the specified dataField
	public void addDataField() {
		tree.addDataField();
		notifyXMLObservers();
	}
	
	// duplicate a dataField and add it at specified index
	public void duplicateDataFields() {
		tree.duplicateDataFields();
		notifyXMLObservers();
	}
	
	
	
	// delete the highlighted dataFields
	public void deleteDataFields(boolean saveChildren) {
		tree.deleteDataFields(saveChildren);
		notifyXMLObservers();
	}
	
	public void demoteDataFields() {
		tree.demoteDataFields();
		notifyXMLObservers();
	}
	
	public void promoteDataFields() {
		tree.promoteDataFields();
		notifyXMLObservers();
	}
	
//	 if the highlighted fields have a preceeding sister, move it below the highlighted fields
	public void moveFieldsUp() {
		tree.moveFieldsUp();
		notifyXMLObservers();
	}
	
//	 if the highlighted fields have a preceeding sister, move it below the highlighted fields
	public void moveFieldsDown() {
		tree.moveFieldsDown();
		notifyXMLObservers();
	}
	
	public void setProtocolEdited(boolean protocolEdited) {
		currentProtocolEdited = protocolEdited;
	}
	public boolean isCurrentProtocolEdited() {
		return currentProtocolEdited;
	}

	// called when saving, then used to set protocolFileName attribute
	public void setCurrentFile(File file) {
		currentFile = file;
	}
	public File getCurrentFile() {
		return currentFile;
	}
	public boolean hasProtocolGotExpChildren() {
		
		String proHasExp = getRootNode().getDataField().getAttribute(DataField.PRO_HAS_EXP_CHILDREN);
		if ((proHasExp != null) && (proHasExp.equals("true"))) 
			return true;
		
		else return false;
	}
	
	public DataFieldNode getRootNode() {
		return tree.getRootNode();
	}
	public JPanel getFieldEditorToDisplay() {
		return tree.getFieldEditorToDisplay();
	}
}
