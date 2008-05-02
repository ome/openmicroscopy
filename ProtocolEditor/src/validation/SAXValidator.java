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

package validation;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
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
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import tree.DataFieldConstants;
import util.XMLMethods;
import xmlMVC.XMLModel;

public class SAXValidator {
	
	private static ArrayList<String> errorMessages = new ArrayList<String>();
	
	/**
	 * This method attempts to validate the XML (xmlFile) against it's schema
	 * (as referenced in the root element). 
	 * Need to be on-line for this!
	 * 
	 * @param xmlFile	The file to be validated
	 * @return	True if XML file is valid wrt it's schema (no errors). 
	 */
	public static boolean isFileValidEditorFile(File xmlFile) {
		
		try {
			Document document = XMLMethods.readXMLtoDOM(xmlFile);
			List<String> validationErrMsgs = SAXValidator.validate(document);
			
			return (validationErrMsgs.size() == 0);
		} catch (SAXException e) {
			return false;
		}
	}
	
	/**
	 * This is a lightweight method to determine whether a file is an OMERO.editor file. 
	 * It simply checks whether the root element of the file is called "ProtocolTitle".
	 * It does not check whether the file is valid against it's XSD schema.
	 * To do that, use isFileValidEditorFile(xmlFile)
	 * 
	 * @param xmlFile	The file to be checked 
	 * @return		True if the file is an XML document that begins with "ProtocolTitle" element.
	 */
	public static boolean isFileEditorFile(File xmlFile) {
		
		try {
			Document document = XMLMethods.readXMLtoDOM(xmlFile);
			
			Element rootElement = document.getDocumentElement();
			String rootName = rootElement.getNodeName();
			
			return (rootName.equals(DataFieldConstants.PROTOCOL_TITLE));
			
		} catch (SAXException e) {
			return false;
		}
	}
	
	
	
	/**
	 * This method attempts to validate the XML (xmlFile) against it's schema
	 * (as referenced in the root element). 
	 * Need to be on-line for this!
	 * Returns a list of error messages (empty if no messages - XML is valid vv schema).
	 * 
	 * @param xmlFile	The file to be validated
	 * @return		A list of messages (strings). 
	 */
	public static List<String> validateXML(File xmlFile) {
		
		try {
			Document document = XMLMethods.readXMLtoDOM(xmlFile);
			return SAXValidator.validate(document);
			
		} catch (SAXException e) {
			
			List <String> errMsg = new ArrayList<String>();
			errMsg.add("SAXException thrown during validation: " + e.getMessage());
			return errMsg;
		}
	}
	
	// validate a DOM document. 
	// need to convert to SAX first....
	static public ArrayList<String> validate(Document document) throws SAXException {
		Transformer transformer;
		File tempFile = new File(XMLModel.OMERO_EDITOR_FILE + "/xmlValidation.xml");
		try {
			// transform to SAX by outputting to temp file...
			transformer = TransformerFactory.newInstance().newTransformer();
			Source source = new DOMSource(document);
			Result output = new StreamResult(tempFile);
			transformer.transform(source, output);
			
			// now validate file via SAX
			validate(tempFile.getAbsolutePath());
			
			// and delete the temp file
			//tempFile.delete();
			
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return errorMessages;
	}
	
	static public void validate(String filePath) throws SAXException {
		
		errorMessages.clear();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		factory.setNamespaceAware(true);
		
		SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			saxParser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
			
			XMLReader xmlReader = saxParser.getXMLReader();
			
			xmlReader.setContentHandler(new SAXValidator.MyXmlHandler());
			xmlReader.setErrorHandler(new SAXValidator.MyXmlHandler());
			
			xmlReader.parse(new File(filePath).toURL().toString());
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	

	public static class MyXmlHandler extends DefaultHandler{

			public void startDocument() {
				 System.out.println("STARTING");
				 System.out.println();
			}
			
			public void startElement(String uri, String localName, String qualName, Attributes attribs) {
				 System.out.println("Start tag: " + qualName);
			}
			
			public void endDocument() {
				 System.out.println();
				 System.out.println("ENDING NORMALLY!");
			}
			
			public void warning(SAXParseException e) {
				// System.out.println("Warning:");
				if (e.getMessage().startsWith("schema_reference.4:")) {
					// don't want to add this message from every element, when no internet connection
				}
				else
					showSpecifics(e);
			}
			
			public void error(SAXParseException e) {
				 System.out.println("Error:");
				showSpecifics(e);
			}
			
			public void fatalError(SAXParseException e) {
				 System.out.println("Fatal Error:");
				showSpecifics(e);
			}
			
			public void showSpecifics(SAXParseException e) {
				 System.out.println(e.getMessage());
				 System.out.println(" Line " + e.getLineNumber());
				 System.out.println(" Column " + e.getColumnNumber());
				 System.out.println(" Document " + e.getSystemId());
				 System.out.println();
				errorMessages.add(e.getMessage());
			}


	}

}