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

package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import tree.DataField;

// class to hold some useful XML methods

public class XMLMethods {

	ArrayList<HashMap> elementList = new ArrayList<HashMap>();
	

// given an xml file, this method returns an arrayList of elements, 
// each one as a hashmap of attributes (elementPath+attributeName, value)
public ArrayList<HashMap> getAllXmlFileAttributes(File file) throws FileNotFoundException, SAXParseException {
	
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new XMLContentHandler());
			xmlReader.parse(file.toURL().toString());
		
		} catch (SAXParseException spEx) {
			throw spEx;
		} catch (SAXException sEx) {
			sEx.printStackTrace();
		} catch (ParserConfigurationException pcEx) {
			pcEx.printStackTrace();
		} catch (FileNotFoundException fnfEx) {
			throw fnfEx;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return elementList;
	}
	
	
	public class XMLContentHandler extends DefaultHandler {
		
		String elementPath = "";
		
		public void startElement(String uri, String localName, String qualName, Attributes attribs) {
			
			String elementName = attribs.getValue(DataField.ELEMENT_NAME);
			if (elementName != null) elementName = elementName.replace("/", "-");	// don't add any confusing / into path
			
			elementPath = elementPath + "/" + elementName;
			
			HashMap attributeMap = new HashMap();
			
			for (int i=0; i<attribs.getLength(); i++) {
				
				attributeMap.put(elementPath + "/" + attribs.getQName(i), attribs.getValue(i));
				
			}
			elementList.add(attributeMap);
			
		}
		
		public void endElement(String uri, String localName, String qualName) {
			
			// remove the last /elementName from the path
			
			int lastSlashIndex = elementPath.lastIndexOf("/");
			if (lastSlashIndex > 1)
				elementPath = elementPath.substring(0, lastSlashIndex);
		}
		
	}
	
	public static Document readXMLtoDOM(File xmlFile) throws SAXException{
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
             pce.printStackTrace();

         } catch (IOException ioe) {
            // I/O error
            ioe.printStackTrace();
         }
         return document;
	}

}
