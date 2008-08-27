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

package org.openmicroscopy.shoola.agents.editor.util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.model.DataFieldConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

// class to hold some useful XML methods

public class XMLMethods {
	
	
	/**
	 * Static method to convert an XML file into a DOM Document. 
	 * 
	 * @param xmlFile
	 * @return	A DOM Document
	 * @throws SAXException
	 */
	public static Document readXMLtoDOM(File xmlFile) 
		throws SAXException {
		DocumentBuilderFactory factory =
            DocumentBuilderFactory.newInstance();
        //factory.setValidating(true);   
        //factory.setNamespaceAware(true);
        Document document = null;
		
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
        	// show error and give user a chance to submit error
        	 EditorAgent.getRegistry().getUserNotifier().notifyError(
        			 "Parser Confuguration Exception",
 					"Parser with specified options can't be built.", pce);

         } catch (IOException ioe) {
        	 EditorAgent.getRegistry().getUserNotifier().notifyError(
        			 "I/O error",
  					"File could not be read", ioe);
         } 
         return document;
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
			
			/*
			 * eg if file not found, above method will return null. 
			 */
			if (document == null) 
				return false;
			
			Element rootElement = document.getDocumentElement();
			String rootName = rootElement.getNodeName();
			
			return (rootName.equals(DataFieldConstants.PROTOCOL_TITLE));
			
		} catch (SAXException e) {
			return false;
		}
	}

}
