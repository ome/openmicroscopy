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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import tree.DataFieldConstants;
import tree.IAttributeSaver;

// class to hold some useful XML methods

public class XMLMethods {

	ArrayList<HashMap<String, String>> elementList = new ArrayList<HashMap<String, String>>();
		
	public static final String ELEMENT_SEPARATOR = "/";
	
	/**
	 * Given an xml file, this method returns an arrayList of elements, 
	 * each one as a hashmap of attributes (elementPath+attributeName, value)
	 * 
	 * This is used for displaying the results of file searches: Need to highlight the 
	 * context of the search keyword by showing which element it is in etc. 
	 */
	public ArrayList<HashMap<String, String>> getAllXmlFileAttributes(File file) throws FileNotFoundException, SAXParseException {
	
		if (elementList == null) {
			elementList = new ArrayList<HashMap<String, String>>();
		}
		elementList.clear();
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(new ElementPathHashMapHandler());
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
	
	
	/**
	 * Given an xml file, this method returns an arrayList of elements, 
	 * each one as a hashmap of attributes (elementPath+attributeName, value)
	 * 
	 * This is used for displaying the results of file searches: Need to highlight the 
	 * context of the search keyword by showing which element it is in etc. 
	 */
	public ArrayList<HashMap<String, String>> getAllXmlFileAttributes(File file, ContentHandler handler) 
		throws FileNotFoundException, SAXParseException {
	
		if (elementList == null) {
			elementList = new ArrayList<HashMap<String, String>>();
		}
		elementList.clear();
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(handler);
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
	
	
	/**
	 * This is the content handler that builds a hashMap from each XML element, and adds
	 * each hashMap to the elementList. 
	 * 
	 * The element path is built-up as the XML hierarchy is traversed, then shortened at the 
	 * end of each element. 
	 * The element path is made up of elementName attribute from each element in the hierarchy,
	 * separated by ELEMENT_SEPARATOR.
	 * 
	 * For each element the attributes are converted to a hashMap, where
	 * key is elementPath (including name of current element) + ELEMENT_SEPARATOR + attributeName
	 * and value is the attribute value. 
	 * 
	 * @author will
	 *
	 */
	public class ElementPathHashMapHandler extends DefaultHandler {
		
		String elementPath = "";
		
		public void startElement(String uri, String localName, String qualName, Attributes attribs) {
			
			String elementName = attribs.getValue(DataFieldConstants.ELEMENT_NAME);
			if (elementName != null) elementName = elementName.replace("/", "-");	// don't add any confusing / into path
			
			elementPath = elementPath + ELEMENT_SEPARATOR + elementName;
			
			HashMap attributeMap = new HashMap();
			
			for (int i=0; i<attribs.getLength(); i++) {
				
				attributeMap.put(elementPath + ELEMENT_SEPARATOR + attribs.getQName(i), attribs.getValue(i));
				
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
	
	
	/**
	 * This is the content handler that builds a hashMap from each XML element, and adds
	 * each hashMap to the elementList. However, the hashMap contains no reference to the
	 * position of that element in the XML document. 
	 * 
	 * For each element the attributes are converted to a hashMap, where
	 * key is attributeName
	 * and value is the attribute value. 
	 * 
	 * @author will
	 *
	 */
	public class ElementAttributesHashMapHandler extends DefaultHandler {
		
		public void startElement(String uri, String localName, String qualName, Attributes attribs) {

			HashMap attributeMap = new HashMap();
			
			for (int i=0; i<attribs.getLength(); i++) {
				
				attributeMap.put(attribs.getQName(i), attribs.getValue(i));
				
			}
			elementList.add(attributeMap);
			
		}	
	}
	
	public static class ElementSearchHandler extends DefaultHandler {
		
		String tagName; 
		String[] attsToCheck; 
		String[] attValues;
		int hits;
		
		public ElementSearchHandler(String tagName, String[] attsToCheck, 
				IAttributeSaver map) {
			this.tagName = tagName;
			this.attsToCheck = attsToCheck;
			/*
			 * Make a copy of the values that need matching.
			 */
			attValues = new String[attsToCheck.length];
			for (int i=0; i<attsToCheck.length; i++) {
				attValues[i] = map.getAttribute(attsToCheck[i]);
			}
		}
		
		public void startElement(String uri, String localName, String qualName, 
				Attributes attribs) {
		
			//System.out.println("XMLMethods ElementSearchHandler " + qualName);
			
			if ((tagName == null) || (tagName.equals(qualName))) {
				for (int i=0; i< attsToCheck.length; i++) {
					// if any attributes don't match, this isn't a hit. continue..
					String xmlValue = attribs.getValue(attsToCheck[i]);
					String attValue = attValues[i];
					if (xmlValue == null) {
						if (attValue != null) {		// one null = no match!
							return;
						}			
					}
					if (! xmlValue.equals(attValue)) {
						return;		// values don't match
					}
				}
				// checked all attributes. All match! 
				hits++;
			}
		
		}
		
		public int getHits() {
			return hits;
		}
		
	}
	
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
 			ExceptionHandler.showErrorDialog("Parser Confuguration Exception",
 					"Parser with specified options can't be built.", pce);

         } catch (IOException ioe) {
            // I/O error
        	 ExceptionHandler.showErrorDialog("I/O error",
  					"File could not be read", ioe);
         } 
         return document;
	}
	
	
	public static int getFieldMatches(File xmlFile, List<IAttributeSaver> fields,
			String[] attsToMatch) { 		
		
		int hits = 0;
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			XMLReader xmlReader = saxParser.getXMLReader();
			
			for (IAttributeSaver field : fields) {
				String tagName = field.getAttribute(DataFieldConstants.INPUT_TYPE);
			
				ElementSearchHandler searcher = new ElementSearchHandler(tagName,
						attsToMatch, field);
				xmlReader.setContentHandler(searcher);
				xmlReader.parse(xmlFile.toURL().toString());
				
				hits = hits + searcher.getHits();
			}
		
		} catch (SAXParseException spEx) {
			spEx.printStackTrace();
		} catch (SAXException sEx) {
			sEx.printStackTrace();
		} catch (ParserConfigurationException pcEx) {
			pcEx.printStackTrace();
		} catch (FileNotFoundException fnfEx) {
			fnfEx.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return hits;
		
	}

}
